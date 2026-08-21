package slimeknights.tconstruct.shared;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.registration.deferred.PotionDeferredRegister;
import slimeknights.mantle.registration.deferred.PotionDeferredRegister.PotionType;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerEffect;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.shared.effect.AntigravityEffect;
import slimeknights.tconstruct.shared.effect.ReturningEffect;
import slimeknights.tconstruct.tools.modifiers.effect.BleedingEffect;
import slimeknights.tconstruct.tools.modifiers.effect.MagneticEffect;
import slimeknights.tconstruct.tools.modifiers.effect.RepulsiveEffect;
import slimeknights.tconstruct.tools.modifiers.traits.skull.SelfDestructiveModifier.SelfDestructiveEffect;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;

/** Handles registration for all status effects and potions in the mod */
public class TinkerEffects extends TinkerModule {
  private static final PotionDeferredRegister POTIONS = new PotionDeferredRegister(TConstruct.MOD_ID);

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static Holder<MobEffect> holder(Holder<? extends MobEffect> effect) {
    return (Holder) effect;
  }



  // slimy potions
  public static final DeferredHolder<TinkerEffect, TinkerEffect> experienced = MOB_EFFECTS.register("experienced", () -> new TinkerEffect(MobEffectCategory.BENEFICIAL, 0x82c873, true).addAttributeModifier(TinkerAttributes.EXPERIENCE_MULTIPLIER, TConstruct.getResource("experienced"), 0.25f, Operation.ADD_MULTIPLIED_BASE));
  public static final DeferredHolder<TinkerEffect, TinkerEffect> ricochet = MOB_EFFECTS.register("ricochet", () -> new TinkerEffect(MobEffectCategory.NEUTRAL, 0x01cbcd, true).addAttributeModifier(TinkerAttributes.KNOCKBACK_MULTIPLIER, TConstruct.getResource("ricochet"), 0.5f, Operation.ADD_MULTIPLIED_BASE));
  public static final DeferredHolder<TinkerEffect, TinkerEffect> enderference = MOB_EFFECTS.register("enderference", () -> new TinkerEffect(MobEffectCategory.HARMFUL, 0xD37CFF, true));
  /** Projectile persistent data key to allow ranged modifiers to hit endermen. */
  public static final Identifier ENDERFERENCE_KEY = enderference.getId();

  // slimy cakes
  public static final DeferredHolder<TinkerEffect, TinkerEffect> bouncy = MOB_EFFECTS.register("bouncy", () -> new TinkerEffect(MobEffectCategory.BENEFICIAL, 0x71AC63, true).addAttributeModifier(TinkerAttributes.BOUNCY, TConstruct.getResource("bouncy"), 1, Operation.ADD_VALUE));
  public static final DeferredHolder<TinkerEffect, TinkerEffect> doubleJump = MOB_EFFECTS.register("double_jump", () -> new TinkerEffect(MobEffectCategory.BENEFICIAL, 0xA99B87, true).addAttributeModifier(TinkerAttributes.JUMP_COUNT, TConstruct.getResource("double_jump"), 1, Operation.ADD_VALUE));
  public static final DeferredHolder<AntigravityEffect, AntigravityEffect> antigravity = MOB_EFFECTS.register("antigravity", AntigravityEffect::new);
  public static final DeferredHolder<ReturningEffect, ReturningEffect> returning = MOB_EFFECTS.register("returning", ReturningEffect::new);

  // modifier effects
  public static final DeferredHolder<BleedingEffect, BleedingEffect> bleeding = MOB_EFFECTS.register("bleeding", BleedingEffect::new);
  public static final DeferredHolder<MagneticEffect, MagneticEffect> magnetic = MOB_EFFECTS.register("magnetic", MagneticEffect::new);
  public static final DeferredHolder<TinkerEffect, TinkerEffect> selfDestructing = MOB_EFFECTS.register("self_destructing", SelfDestructiveEffect::new);
  public static final DeferredHolder<RepulsiveEffect, RepulsiveEffect> repulsive = MOB_EFFECTS.register("repulsive", RepulsiveEffect::new);
  public static final DeferredHolder<TinkerEffect, TinkerEffect> pierce = MOB_EFFECTS.register("pierce", () -> new TinkerEffect(MobEffectCategory.HARMFUL, 0xD1D37A, true).addAttributeModifier(Attributes.ARMOR, TConstruct.getResource("pierce"), -1, Operation.ADD_VALUE));
  // damage boost
  public static final DeferredHolder<TinkerEffect, TinkerEffect> conductive = MOB_EFFECTS.register("conductive", () -> new TinkerEffect(MobEffectCategory.HARMFUL, 0xF2D500, true));
  public static final DeferredHolder<TinkerEffect, TinkerEffect> venom = MOB_EFFECTS.register("venom", () -> new TinkerEffect(MobEffectCategory.HARMFUL, 0xA2935E, true));

  // potions
  public static final EnumObject<PotionType,Potion> experiencedPotion = POTIONS.registerTypes(experienced).withStrong().withLong().build();
  public static final EnumObject<PotionType,Potion> ricochetPotion = POTIONS.registerTypes(ricochet).withStrong().withLong().build();
  public static final EnumObject<PotionType,Potion> levitationPotion = POTIONS.registerTypes("levitation", () -> MobEffects.LEVITATION.value(), 15 * 20, 0).withStrong().withLong(40 * 20, 0).build();
  public static final EnumObject<PotionType,Potion> enderferencePotion = POTIONS.registerTypes(enderference, 90 * 20, 0).withLong().build();

  @SuppressWarnings("removal")
  public TinkerEffects() {
    POTIONS.register(TConstruct.MOD_EVENT_BUS);
    NeoForge.EVENT_BUS.addListener(this::registerBrewing);
  }

  void registerBrewing(RegisterBrewingRecipesEvent event) {
    brewing(event, experiencedPotion,  Potions.AWKWARD, Ingredient.of(TinkerWorld.congealedSlime.get(SlimeType.EARTH)));
    brewing(event, ricochetPotion,     Potions.AWKWARD, Ingredient.of(TinkerWorld.congealedSlime.get(SlimeType.SKY)));
    brewing(event, levitationPotion,   Potions.AWKWARD, Ingredient.of(TinkerWorld.congealedSlime.get(SlimeType.ICHOR)));
    brewing(event, enderferencePotion, Potions.AWKWARD, Ingredient.of(TinkerWorld.congealedSlime.get(SlimeType.ENDER)));
  }

  /** Registers recipes for brewing, longer and stronger potions for the given object */
  private static void brewing(RegisterBrewingRecipesEvent event, EnumObject<PotionType,Potion> potion, Holder<Potion> base, Ingredient ingredient) {
    Holder<Potion> normal = BuiltInRegistries.POTION.wrapAsHolder(potion.get(PotionType.NORMAL));
    event.getBuilder().addMix(base, ingredient.items().findFirst().orElseThrow().value(), normal);
    Potion longer = potion.getOrNull(PotionType.LONG);
    if (longer != null) {
      event.getBuilder().addMix(normal, Items.REDSTONE, BuiltInRegistries.POTION.wrapAsHolder(longer));
    }
    Potion strong = potion.getOrNull(PotionType.STRONG);
    if (strong != null) {
      event.getBuilder().addMix(normal, Items.GLOWSTONE_DUST, BuiltInRegistries.POTION.wrapAsHolder(strong));
    }
  }

  /** Checks if the given entity can be hit considering enderman enderference */
  public static boolean canHitWithProjectile(@Nullable LivingEntity living) {
    return living == null || living.getType() != EntityType.ENDERMAN || living.hasEffect(holder(enderference));
  }

  /** Checks if the given entity needs special casing for enderference */
  public static boolean needsEnderferenceOverride(@Nullable Entity entity) {
    return entity != null && entity.getType() == EntityType.ENDERMAN && entity instanceof LivingEntity living && living.hasEffect(holder(enderference));
  }

  /** Checks if the given entity needs special casing for enderference */
  public static boolean needsEnderferenceOverride(@Nullable LivingEntity living) {
    return living != null && living.getType() == EntityType.ENDERMAN && living.hasEffect(holder(enderference));
  }
}
