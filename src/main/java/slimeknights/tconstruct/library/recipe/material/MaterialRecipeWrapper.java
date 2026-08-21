package slimeknights.tconstruct.library.recipe.material;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.recipe.data.ConsumerWrapperBuilder;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.util.List;

/**
 * Datagen-only recipe wrapper carrying the extra fields used by material crafting serializers.
 * The outer Mantle wrapper changes the serializer; this wrapper carries the fields to the codec.
 */
record MaterialRecipeWrapper<T extends RecipeInput>(
  Recipe<T> recipe,
  String parts,
  int partCount,
  List<MaterialVariantId> extraMaterials
) implements Recipe<T> {
  MaterialRecipeWrapper {
    extraMaterials = List.copyOf(extraMaterials);
  }

  @Override
  public boolean matches(T input, Level level) {
    return recipe.matches(input, level);
  }

  @Override
  public ItemStack assemble(T input) {
    return recipe.assemble(input);
  }

  @Override
  public boolean isSpecial() {
    return recipe.isSpecial();
  }

  @Override
  public boolean showNotification() {
    return recipe.showNotification();
  }

  @Override
  public String group() {
    return recipe.group();
  }

  @Override
  public RecipeSerializer<? extends Recipe<T>> getSerializer() {
    return recipe.getSerializer();
  }

  @Override
  public RecipeType<? extends Recipe<T>> getType() {
    return recipe.getType();
  }

  @Override
  public PlacementInfo placementInfo() {
    return recipe.placementInfo();
  }

  @Override
  public List<RecipeDisplay> display() {
    return recipe.display();
  }

  @Override
  public RecipeBookCategory recipeBookCategory() {
    return recipe.recipeBookCategory();
  }

  /** Adds material metadata before Mantle changes the serializer used by the recipe codec. */
  static RecipeOutput output(RecipeOutput consumer, RecipeSerializer<?> serializer, String parts, int partCount, List<MaterialVariantId> extraMaterials) {
    RecipeOutput materialOutput = new RecipeOutput() {
      @Override
      public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
        consumer.accept(key, new MaterialRecipeWrapper<>(recipe, parts, partCount, extraMaterials), advancement, conditions);
      }

      @Override
      public Advancement.Builder advancement() {
        return consumer.advancement();
      }

      @Override
      public void includeRootAdvancement() {
        consumer.includeRootAdvancement();
      }
    };
    return ConsumerWrapperBuilder.wrap(serializer).build(materialOutput);
  }

  /** Finds the material wrapper below Mantle's serializer override wrapper. */
  static MaterialRecipeWrapper<?> find(Recipe<?> recipe) {
    if (recipe instanceof ConsumerWrapperBuilder.WrappedRecipe wrapped) {
      return find(wrapped.original());
    }
    if (recipe instanceof MaterialRecipeWrapper<?> wrapped) {
      return wrapped;
    }
    return null;
  }

  /** Finds the original vanilla recipe below both datagen wrappers. */
  static Recipe<?> original(Recipe<?> recipe) {
    if (recipe instanceof ConsumerWrapperBuilder.WrappedRecipe wrapped) {
      return original(wrapped.original());
    }
    if (recipe instanceof MaterialRecipeWrapper<?> wrapped) {
      return original(wrapped.recipe());
    }
    return recipe;
  }
}
