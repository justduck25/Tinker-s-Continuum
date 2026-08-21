package slimeknights.tconstruct.library.recipe.tinkerstation.repairing;

import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;


/** Builds a recipe to repair a tool using a modifier */
@RequiredArgsConstructor(staticName = "repair")
public class ModifierRepairRecipeBuilder extends AbstractRecipeBuilder<ModifierRepairRecipeBuilder> {
  private final ModifierId modifier;
  private final Ingredient ingredient;
  private final int repairAmount;

  public static ModifierRepairRecipeBuilder repair(LazyModifier modifier, Ingredient ingredient, int repairAmount) {
    return repair(modifier.getId(), ingredient, repairAmount);
  }

  @Override
  public void save(RecipeOutput consumer) {
    save(consumer, modifier.getId());
  }

  /** Builds the recipe for the crafting table using a repair kit */
  public ModifierRepairRecipeBuilder buildCraftingTable(RecipeOutput consumer, Identifier id) {
    var key = recipeKey(id);
    consumer.accept(key, new ModifierRepairCraftingRecipe(id, modifier, ingredient, repairAmount), buildOptionalAdvancement(key, "tinker_station"));
    return this;
  }

  @Override
  public void save(RecipeOutput consumer, Identifier id) {
    var key = recipeKey(id);
    consumer.accept(key, new ModifierRepairTinkerStationRecipe(id, modifier, ingredient, repairAmount), buildOptionalAdvancement(key, "tinker_station"));
  }
}
