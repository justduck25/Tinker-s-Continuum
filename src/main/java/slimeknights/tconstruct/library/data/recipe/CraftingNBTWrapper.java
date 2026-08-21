package slimeknights.tconstruct.library.data.recipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Helper to add NBT to vanilla recipes. */
public record CraftingNBTWrapper(RecipeOutput output, CompoundTag nbt) implements RecipeOutput {
  @Override
  public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
    // TODO: 1.21.2 - reimplement NBT injection for crafting recipes
    output.accept(key, recipe, advancement, conditions);
  }

  @Override
  public Advancement.Builder advancement() {
    return output.advancement();
  }

  @Override
  public void includeRootAdvancement() {
    output.includeRootAdvancement();
  }
}
