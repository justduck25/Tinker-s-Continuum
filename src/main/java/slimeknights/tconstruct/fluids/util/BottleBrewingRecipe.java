package slimeknights.tconstruct.fluids.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

/** Recipe for transforming a bottle, depending on a vanilla brewing recipe to get the ingredient */
public class BottleBrewingRecipe extends BrewingRecipe {
  private final Item from;
  private final Item to;
  public BottleBrewingRecipe(Ingredient input, Item from, Item to, ItemStack output) {
    super(input, slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA, output);
    this.from = from;
    this.to = to;
  }

  @Override
  public boolean isIngredient(ItemStack stack) {
    return false;
  }

  @Override
  public Ingredient getIngredient() {
    return slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA;
  }
}
