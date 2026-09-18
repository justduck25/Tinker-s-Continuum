package slimeknights.tconstruct.library.modifiers.modules.armor;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.json.LevelingInt;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.technical.ArmorLevelModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.ComputableDataKey;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Module for armor modifiers that makes the wearer immune to a mob effect
 */
public record EffectImmunityModule(MobEffect effect, LevelingInt maxLevel, ModifierCondition<IToolStackView> condition) implements ModifierModule, EquipmentChangeModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MobDisguiseModule>defaultHooks(ModifierHooks.EQUIPMENT_CHANGE);
  public static final ComputableDataKey<Multiset<MobEffect>> EFFECT_IMMUNITY = TConstruct.createKey("effect_immunity", HashMultiset::create);
  private static final LevelingInt ANY_LEVEL = LevelingInt.flat(255);
  public static final RecordLoadable<EffectImmunityModule> LOADER = RecordLoadable.create(
    Loadables.MOB_EFFECT.requiredField("effect", EffectImmunityModule::effect),
    LevelingInt.LOADABLE.defaultField("max_level", ANY_LEVEL, false, EffectImmunityModule::maxLevel),
    ModifierCondition.TOOL_FIELD,
    EffectImmunityModule::new);

  /** @deprecated use {@link #EffectImmunityModule(MobEffect, LevelingInt, ModifierCondition)} */
  @Deprecated(forRemoval = true)
  public EffectImmunityModule(MobEffect effect, ModifierCondition<IToolStackView> condition) {
    this(effect, ANY_LEVEL, condition);
  }

  public EffectImmunityModule(MobEffect effect, LevelingInt maxLevel) {
    this(effect, maxLevel, ModifierCondition.ANY_TOOL);
  }

  public EffectImmunityModule(MobEffect effect) {
    this(effect, ANY_LEVEL);
  }

  public EffectImmunityModule(Supplier<? extends MobEffect> effect, LevelingInt maxLevel) {
    this(effect.get(), maxLevel);
  }

  public EffectImmunityModule(Supplier<? extends MobEffect> effect) {
    this(effect.get());
  }

  @Override
  public RecordLoadable<EffectImmunityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (!tool.isBroken() && ArmorLevelModule.validSlot(tool, context.getChangedSlot(), TinkerTags.Items.HELD_ARMOR) && condition.matches(tool, modifier)) {
      TinkerDataCapability.Holder data = TinkerDataCapability.getData(context.getEntity());
      if (data != null) {
        data.computeIfAbsent(EFFECT_IMMUNITY).add(effect, maxLevel.compute(modifier));
      }
    }
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (!tool.isBroken() && ArmorLevelModule.validSlot(tool, context.getChangedSlot(), TinkerTags.Items.HELD_ARMOR) && condition.matches(tool, modifier)) {
      TinkerDataCapability.Holder data = TinkerDataCapability.getData(context.getEntity());
      if (data != null) {
        Multiset<MobEffect> effects = data.get(EFFECT_IMMUNITY);
        if (effects != null) {
          effects.remove(effect, maxLevel.compute(modifier));
        }
      }
    }
  }

  /** Immunity copies stored for this effect, scanning equipped tools if the entity data is empty. */
  public static int getImmunity(LivingEntity entity, Holder<MobEffect> effect) {
    TinkerDataCapability.Holder data = TinkerDataCapability.getData(entity);
    if (data != null) {
      Multiset<MobEffect> multiset = data.get(EFFECT_IMMUNITY);
      if (multiset != null) {
        int count = count(multiset, effect);
        if (count > 0) {
          return count;
        }
      }
    }
    return scanEquipment(entity, effect);
  }

  private static int count(Multiset<MobEffect> multiset, Holder<MobEffect> effect) {
    MobEffect value = effect.value();
    int amount = multiset.count(value);
    if (amount > 0) {
      return amount;
    }
    Identifier key = BuiltInRegistries.MOB_EFFECT.getKey(value);
    for (MobEffect immune : multiset.elementSet()) {
      if (Objects.equals(key, BuiltInRegistries.MOB_EFFECT.getKey(immune))) {
        return multiset.count(immune);
      }
    }
    return 0;
  }

  private static int scanEquipment(LivingEntity entity, Holder<MobEffect> effect) {
    int total = 0;
    MobEffect value = effect.value();
    Identifier key = BuiltInRegistries.MOB_EFFECT.getKey(value);
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      ItemStack stack = entity.getItemBySlot(slot);
      if (stack.isEmpty() || !stack.is(TinkerTags.Items.MODIFIABLE)) {
        continue;
      }
      IToolStackView tool = ToolStack.from(stack);
      if (tool.isBroken() || !ArmorLevelModule.validSlot(tool, slot, TinkerTags.Items.HELD_ARMOR)) {
        continue;
      }
      for (ModifierEntry entry : tool.getModifierList()) {
        total += immunityFromHook(entry.getHook(ModifierHooks.EQUIPMENT_CHANGE), entry, value, key);
      }
    }
    return total;
  }

  private static int immunityFromHook(EquipmentChangeModifierHook hook, ModifierEntry entry, MobEffect value, Identifier key) {
    if (hook instanceof EffectImmunityModule module) {
      return matches(module.effect, value, key) ? module.maxLevel.compute(entry) : 0;
    }
    if (hook instanceof EquipmentChangeModifierHook.AllMerger merger) {
      int sum = 0;
      for (EquipmentChangeModifierHook nested : merger.modules()) {
        sum += immunityFromHook(nested, entry, value, key);
      }
      return sum;
    }
    return 0;
  }

  private static boolean matches(MobEffect stored, MobEffect value, Identifier key) {
    return stored == value || Objects.equals(key, BuiltInRegistries.MOB_EFFECT.getKey(stored));
  }
}
