package slimeknights.tconstruct.library.modifiers.modules.behavior;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.item.ItemPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.json.LevelingInt;
import slimeknights.tconstruct.library.json.LevelingValue;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.UsingToolModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.tools.modules.armor.CounterModule;

import java.util.List;

/**
 * Module that makes a tool edible.
 */
public class EdibleModule implements ModifierModule, GeneralInteractionModifierHook, UsingToolModifierHook, OnAttackedModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<EdibleModule>defaultHooks(ModifierHooks.GENERAL_INTERACT, ModifierHooks.TOOL_USING, ModifierHooks.ON_ATTACKED);
  public static final RecordLoadable<EdibleModule> LOADER = RecordLoadable.create(
    ItemStackLoadable.OPTIONAL_ITEM_NBT.defaultField("representative_item", ItemStack.EMPTY, EdibleModule::representativeItem),
    LevelingInt.LOADABLE.requiredField("duration", EdibleModule::duration),
    LevelingInt.LOADABLE.requiredField("durability_usage", EdibleModule::durabilityUsage),
    LevelingValue.LOADABLE.requiredField("counter_chance", EdibleModule::chance),
    ModifierCondition.TOOL_FIELD, EdibleModule::new);
  /** Tool stat for the amount of hunger restored upon eating this. */
  public static final FloatToolStat HUNGER = new FloatToolStat(new ToolStatId(TConstruct.MOD_ID, "hunger"), 0xFFF0A8A4, 0, 0, 200, ItemPredicate.or(ItemPredicate.tag(TinkerTags.Items.INTERACTABLE_CHARGE), ItemPredicate.tag(TinkerTags.Items.ARMOR)));
  /** Tool stat for the amount of saturation restored upon eating this. */
  public static final FloatToolStat SATURATION = new FloatToolStat(new ToolStatId(TConstruct.MOD_ID, "saturation"), 0xFFF0A8A4, 0, 0, 200, ItemPredicate.or(ItemPredicate.tag(TinkerTags.Items.INTERACTABLE_CHARGE), ItemPredicate.tag(TinkerTags.Items.ARMOR)));

  private final ItemStack representativeItem;
  private final LevelingInt duration;
  private final LevelingInt durabilityUsage;
  private final LevelingValue chance;
  private final ModifierCondition<IToolStackView> condition;

  public EdibleModule(ItemStack representativeItem, LevelingInt duration, LevelingInt durabilityUsage, LevelingValue chance, ModifierCondition<IToolStackView> condition) {
    this.representativeItem = representativeItem;
    this.duration = duration;
    this.durabilityUsage = durabilityUsage;
    this.chance = chance;
    this.condition = condition;
  }

  public EdibleModule(ItemLike representativeItem, LevelingInt duration, LevelingInt durabilityUsage, LevelingValue chance) {
    this(safeRepresentativeStack(representativeItem), duration, durabilityUsage, chance, ModifierCondition.ANY_TOOL);
  }

  /**
   * Creates the representative stack during datagen before registry components are bound.
   * Runtime-loaded modules still receive the normal component-complete stack from ItemStackLoadable.
   */
  private static ItemStack safeRepresentativeStack(ItemLike representativeItem) {
    Item item = representativeItem.asItem();
    Holder.Reference<Item> holder = item.builtInRegistryHolder();
    if (holder.areComponentsBound()) {
      return new ItemStack(holder);
    }
    return new ItemStack(Holder.direct(item, DataComponentMap.EMPTY));
  }

  public static EdibleModule create(ItemLike representativeItem, LevelingInt duration, LevelingInt durabilityUsage, LevelingValue chance) {
    return new EdibleModule(representativeItem, duration, durabilityUsage, chance);
  }

  public ItemStack representativeItem() { return representativeItem; }
  public LevelingInt duration() { return duration; }
  public LevelingInt durabilityUsage() { return durabilityUsage; }
  public LevelingValue chance() { return chance; }
  public ModifierCondition<IToolStackView> condition() { return condition; }

  @Override
  public RecordLoadable<EdibleModule> getLoader() { return LOADER; }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() { return DEFAULT_HOOKS; }

  @Override
  public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
    if (source == InteractionSource.RIGHT_CLICK && !tool.isBroken() && player.canEat(false) && tool.getStats().getInt(HUNGER) > 0) {
      GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
      return InteractionResult.CONSUME;
    }
    return InteractionResult.PASS;
  }

  @Override
  public ItemUseAnimation getUseAction(IToolStackView tool, ModifierEntry modifier) {
    return ItemUseAnimation.EAT;
  }

  @Override
  public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
    return duration.compute(modifier.getEffectiveLevel());
  }

  /** Takes a nibble of the tool. */
  private void eat(IToolStackView tool, ModifierEntry modifier, Player player, ItemStack representativeItem) {
    StatsNBT stats = tool.getStats();
    int hunger = stats.getInt(HUNGER);
    if (hunger > 0) {
      Level world = player.level();
      float saturation = stats.get(SATURATION);
      player.getFoodData().eat(hunger, saturation);
      ModifierUtil.foodConsumer.onConsume(player, representativeItem, hunger, saturation);
      world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F + (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.4F);
      world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);

      int damage = durabilityUsage.compute(modifier.getEffectiveLevel());
      ToolDamageUtil.directDamage(tool, damage, player, player.getUseItem());
    }
  }

  /** Gets the item for particles and Diet. */
  private ItemStack getRepresentativeItem(LivingEntity entity) {
    return !representativeItem.isEmpty() ? representativeItem : entity.getUseItem();
  }

  /** Plays effects for eating. */
  private static void eatEffects(LivingEntity entity, ItemStack representativeItem, int amount) {
    entity.spawnItemParticles(representativeItem, amount);
    RandomSource random = entity.getRandom();
    entity.playSound(SoundEvents.GENERIC_EAT.value(), 0.5f + 0.5f * random.nextInt(2), (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f);
  }

  @Override
  public void onUsingTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
    if (modifier != activeModifier && condition.matches(tool, modifier) && tool.getStats().getInt(HUNGER) > 0) {
      int useTime = useDuration - timeLeft;
      int modifierDuration = getUseDuration(tool, modifier);
      if (useTime == modifierDuration) {
        if (entity instanceof Player player && player.canEat(false)) {
          ItemStack representative = getRepresentativeItem(entity);
          eatEffects(entity, representative, 16);
          if (!entity.level().isClientSide()) {
            eat(tool, modifier, player, representative);
          }
        }
      } else if (useTime < modifierDuration && useTime % 4 == 0 && entity instanceof Player player && player.canEat(false)) {
        eatEffects(entity, getRepresentativeItem(entity), 5);
      }
    }
  }

  @Override
  public void beforeReleaseUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
    if (!tool.isBroken() && useDuration - timeLeft == getUseDuration(tool, modifier) && condition.matches(tool, modifier) && tool.getStats().getInt(HUNGER) > 0 && entity instanceof Player player && player.canEat(false)) {
      ItemStack representative = getRepresentativeItem(entity);
      if (modifier != activeModifier) {
        eatEffects(entity, representative, 5);
      }
      if (!entity.level().isClientSide()) {
        eat(tool, modifier, player, representative);
      }
    }
  }

  @Override
  public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
    if (!tool.isBroken() && tool.hasTag(TinkerTags.Items.ARMOR) && condition.matches(tool, modifier) && tool.getStats().getInt(HUNGER) > 0) {
      LivingEntity entity = context.getEntity();
      float level = CounterModule.getLevel(tool, modifier, slotType, entity);
      if (context.getLevel().getRandom().nextFloat() < chance.compute(level) && entity instanceof Player player && player.canEat(false)) {
        eat(tool, modifier, player, !representativeItem.isEmpty() ? representativeItem : entity.getItemBySlot(slotType));
      }
    }
  }
}
