package slimeknights.tconstruct.shared;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.block.FoliageType;

@SuppressWarnings("WeakerAccess")
public final class TinkerFood {
  private TinkerFood() {}
  /** Bacon. What more is there to say? */
  public static final FoodProperties BACON = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.6F).build();

  /** Cheese is used for both the block and the ingot, eating the block returns 3 ingots */
  public static final FoodProperties CHEESE = (new FoodProperties.Builder()).nutrition(3).saturationModifier(0.4F).build();

  /** For the modifier */
  public static final FoodProperties JEWELED_APPLE = new FoodProperties.Builder().nutrition(4).saturationModifier(1.2F).alwaysEdible().build();
  /** Consumable effects matching the upstream Jewelled Apple behavior. */
  public static final Consumable JEWELED_APPLE_CONSUMABLE = Consumable.builder()
      .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HASTE, 1200, 0), 1.0F))
      .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.RESISTANCE, 2400, 0), 1.0F))
      .build();

  /* Cake block is set up to take food as a parameter */
  public static final FoodProperties EARTH_CAKE = new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).alwaysEdible().build();
  public static final FoodProperties SKY_CAKE   = new FoodProperties.Builder().nutrition(1).saturationModifier(0.2f).alwaysEdible().build();
  public static final FoodProperties ICHOR_CAKE = new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).alwaysEdible().build();
  public static final FoodProperties ENDER_CAKE = new FoodProperties.Builder().nutrition(1).saturationModifier(0.4f).alwaysEdible().build();
  public static final FoodProperties MAGMA_CAKE = new FoodProperties.Builder().nutrition(2).saturationModifier(0.2f).alwaysEdible().build();
  // regen is 50 ticks per half heart, so this heals 3 per slice
  public static final FoodProperties BLOOD_CAKE = new FoodProperties.Builder().nutrition(2).saturationModifier(0.2f).alwaysEdible().build();

  public static final FoodProperties EARTH_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();
  public static final FoodProperties SKY_BOTTLE   = new FoodProperties.Builder().alwaysEdible().build();
  public static final FoodProperties ICHOR_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();
  public static final FoodProperties ENDER_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();
  // 250 is 10 poison damage
  public static final FoodProperties VENOM_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();
  /** @deprecated no longer used */
  @Deprecated(forRemoval = true)
  public static final FoodProperties MAGMA_BOTTLE = new FoodProperties.Builder().alwaysEdible().build();

  public static final FoodProperties MEAT_SOUP = new FoodProperties.Builder().nutrition(8).saturationModifier(0.6f).build();

  /**
   * Gets the cake for the given slime type
   * @param slime  Slime type
   * @return  Cake food
   */
  public static FoodProperties getCake(FoliageType slime) {
    return switch (slime) {
      default -> EARTH_CAKE;
      case SKY -> SKY_CAKE;
      case ICHOR -> ICHOR_CAKE;
      case BLOOD -> BLOOD_CAKE;
      case ENDER -> ENDER_CAKE;
    };
  }

  /**
   * Gets the cake for the given slime type
   * @param slime  Slime type
   * @return  Cake food
   */
  public static FoodProperties getBottle(SlimeType slime) {
    return switch (slime) {
      default -> EARTH_BOTTLE;
      case SKY -> SKY_BOTTLE;
      case ICHOR -> ICHOR_BOTTLE;
      case ENDER -> ENDER_BOTTLE;
    };
  }
}
