package slimeknights.tconstruct.tools.recipe;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.library.recipe.worktable.AbstractSizedIngredientRecipeBuilder;
import slimeknights.tconstruct.library.recipe.worktable.AbstractWorktableRecipe;

/** Builder for {@link ToggleInteractionWorktableRecipe} */
@Accessors(fluent = true)
@Setter
@NoArgsConstructor(staticName = "builder")
public class ToggleInteractionWorktableRecipeBuilder extends AbstractSizedIngredientRecipeBuilder<ToggleInteractionWorktableRecipeBuilder> {
  private Ingredient tools = AbstractWorktableRecipe.DEFAULT_TOOLS;

  @Override
  public void save(RecipeOutput consumer) {
    save(consumer, tools.items().findFirst().map(holder -> Loadables.ITEM.getKey(holder.value())).orElseThrow());
  }

  @Override
  public void save(RecipeOutput consumer, Identifier id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one ingredient");
    }
    var key = recipeKey(id);
    consumer.accept(key, new ToggleInteractionWorktableRecipe(id, tools, inputs), buildOptionalAdvancement(key, "modifiers"));
  }
}
