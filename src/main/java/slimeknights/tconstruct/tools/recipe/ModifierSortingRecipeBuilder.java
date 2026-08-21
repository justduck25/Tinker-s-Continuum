package slimeknights.tconstruct.tools.recipe;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import slimeknights.tconstruct.library.recipe.worktable.AbstractSizedIngredientRecipeBuilder;

/** Builder for modifier sorting recipes */
@RequiredArgsConstructor(staticName = "sorting")
public class ModifierSortingRecipeBuilder extends AbstractSizedIngredientRecipeBuilder<ModifierSortingRecipeBuilder> {
  @Override
  public void save(RecipeOutput consumer) {
    save(consumer, BuiltInRegistries.ITEM.getKey(inputs.get(0).getMatchingStacks().get(0).getItem()));
  }

  @Override
  public void save(RecipeOutput consumer, Identifier id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one ingredient");
    }
    var key = recipeKey(id);
    consumer.accept(key, new ModifierSortingRecipe(id, inputs), buildOptionalAdvancement(key, "modifiers"));
  }
}
