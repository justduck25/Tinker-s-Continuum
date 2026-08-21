package slimeknights.tconstruct.plugin.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialRecipe;
import slimeknights.tconstruct.plugin.jei.material.MaterialsCraftingExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/** Stateless JEI extension for the legacy shaped material recipe used by tool parts. */
public class ShapedMaterialExtension implements ICraftingCategoryExtension<ShapedMaterialRecipe> {
  @Override
  public int getWidth(RecipeHolder<ShapedMaterialRecipe> recipeHolder) {
    return recipeHolder.value().getWidth();
  }

  @Override
  public int getHeight(RecipeHolder<ShapedMaterialRecipe> recipeHolder) {
    return recipeHolder.value().getHeight();
  }

  @Override
  public void setRecipe(RecipeHolder<ShapedMaterialRecipe> recipeHolder, IRecipeLayoutBuilder builder,
                        ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    ShapedMaterialRecipe recipe = recipeHolder.value();
    ItemStack plainResult = recipe.assemble(CraftingInput.EMPTY);
    List<ItemStack> results = getResults(recipe, plainResult);
    List<Optional<Ingredient>> inputs = recipe.getIngredients();
    int[] materialSlots = IntStream.range(0, inputs.size())
      .filter(i -> inputs.get(i).map(ingredient -> ingredient.isCustom()
        && ingredient.getCustomIngredient() instanceof MaterialValueIngredient).orElse(false))
      .toArray();
    MaterialsCraftingExtension.setRecipe(this, builder, craftingGridHelper, recipe, results, plainResult, materialSlots);
  }

  @Override
  public List<SlotDisplay> getIngredients(RecipeHolder<ShapedMaterialRecipe> recipeHolder) {
    return recipeHolder.value().getIngredients().stream().map(Ingredient::optionalIngredientToDisplay).toList();
  }

  private static List<ItemStack> getResults(ShapedMaterialRecipe recipe, ItemStack plainResult) {
    MaterialValueIngredient materials = recipe.getMaterial();
    if (materials == null) {
      return List.of(plainResult);
    }
    return MaterialRecipeCache.getAllRecipes().stream().filter(materials::test).flatMap(material -> {
      ItemStack stack = plainResult.copy();
      recipe.setMaterial(stack, material.getMaterial().getVariant());
      int copies = (int) material.getIngredient().items().count();
      return IntStream.range(0, copies).mapToObj(i -> stack);
    }).toList();
  }
}
