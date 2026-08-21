package slimeknights.tconstruct.library.tools.item;

import net.minecraft.world.item.crafting.Ingredient;

/** Dummy tier implementation for piglin compat */
public enum TinkerTier {
  INSTANCE;
  public int getUses() {
    return 0;
  }
  public float getSpeed() {
    return 0;
  }
  public float getAttackDamageBonus() {
    return 0;
  }

  @Deprecated
  public int getLevel() {
    return 0;
  }
  public int getEnchantmentValue() {
    return 0;
  }
  public Ingredient getRepairIngredient() {
    return slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA;
  }
}
