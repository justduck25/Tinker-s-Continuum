package slimeknights.tconstruct.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.TConstruct;

/**
 * Shared logic for each module's recipe provider
 */
public abstract class BaseRecipeProvider extends RecipeProvider implements IRecipeHelper {
  public BaseRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
    super(provider, output);
    TConstruct.sealTinkersClass(this, "BaseRecipeProvider", "BaseRecipeProvider is trivial to recreate and directly extending can lead to addon recipes polluting our namespace.");
  }

  @Override
  protected abstract void buildRecipes();

  public abstract String getName();

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }

  /** Converts a string recipe identifier to the ResourceKey used by modern recipe datagen. */
  protected net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipeKey(String id) {
    return recipeKey(net.minecraft.resources.Identifier.parse(id));
  }
}
