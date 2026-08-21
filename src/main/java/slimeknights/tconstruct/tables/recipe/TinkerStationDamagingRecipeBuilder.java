package slimeknights.tconstruct.tables.recipe;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;


/** Builder for tinker station damaging recipes */
@RequiredArgsConstructor(staticName = "damage")
public class TinkerStationDamagingRecipeBuilder extends AbstractRecipeBuilder<TinkerStationDamagingRecipeBuilder> {

  private final Ingredient ingredient;
  private final int damageAmount;

  @Override
  public void save(RecipeOutput consumer) {
    ItemStack[] stacks = ingredient.items().toArray(ItemStack[]::new);
    if (stacks.length == 0) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    save(consumer, BuiltInRegistries.ITEM.getKey(stacks[0].getItem()));
  }

  @Override
  public void save(RecipeOutput consumer, Identifier id) {
    if (ingredient.isEmpty()) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
    consumer.accept(key, new TinkerStationDamagingRecipe(id, ingredient, damageAmount), buildOptionalAdvancement(key, "tinker_station"));
  }

  @Override
  public void save(RecipeOutput consumer, ResourceKey<Recipe<?>> key) {
    save(consumer, key.identifier());
  }
}
