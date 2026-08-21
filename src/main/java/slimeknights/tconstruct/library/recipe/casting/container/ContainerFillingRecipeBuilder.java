package slimeknights.tconstruct.library.recipe.casting.container;

import lombok.AllArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
/**
 * Builder for a container filling recipe. Takes an arbitrary fluid for a specific amount to fill a NeoForge {@link net.neoforged.neoforge.fluids.capability.IFluidHandlerItem}
 */
@AllArgsConstructor(staticName = "castingRecipe")
@SuppressWarnings({"WeakerAccess", "unused"})
public class ContainerFillingRecipeBuilder extends AbstractRecipeBuilder<ContainerFillingRecipeBuilder> {
  private final Identifier result;
  private final int fluidAmount;
  private final TypeAwareRecipeSerializer<? extends ContainerFillingRecipe> recipeSerializer;

  /**
   * Creates a new builder instance using the given result, amount, and serializer
   * @param result            Recipe result
   * @param fluidAmount       Container size
   * @param recipeSerializer  Serializer
   * @return  Builder instance
   */
  public static ContainerFillingRecipeBuilder castingRecipe(ItemLike result, int fluidAmount, TypeAwareRecipeSerializer<? extends ContainerFillingRecipe> recipeSerializer) {
    return new ContainerFillingRecipeBuilder(BuiltInRegistries.ITEM.getKey(result.asItem()), fluidAmount, recipeSerializer);
  }

  /** Creates a new basin recipe builder using the given result, amount, and serializer. */
  public static ContainerFillingRecipeBuilder basinRecipe(Identifier result, int fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.typeAware(TinkerRecipeTypes.CASTING_BASIN, TinkerSmeltery.basinFillingRecipeSerializer));
  }

  /** Creates a new basin recipe builder using the given result, amount, and serializer. */
  public static ContainerFillingRecipeBuilder basinRecipe(ItemLike result, int fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.typeAware(TinkerRecipeTypes.CASTING_BASIN, TinkerSmeltery.basinFillingRecipeSerializer));
  }

  /** Creates a new table recipe builder using the given result, amount, and serializer. */
  public static ContainerFillingRecipeBuilder tableRecipe(Identifier result, int fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.typeAware(TinkerRecipeTypes.CASTING_TABLE, TinkerSmeltery.tableFillingRecipeSerializer));
  }

  /** Creates a new table recipe builder using the given result, amount, and serializer. */
  public static ContainerFillingRecipeBuilder tableRecipe(ItemLike result, int fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.typeAware(TinkerRecipeTypes.CASTING_TABLE, TinkerSmeltery.tableFillingRecipeSerializer));
  }

  @Override
  public void save(RecipeOutput consumer) {
    this.save(consumer, this.result);
  }

  @Override
  public void save(RecipeOutput consumer, Identifier id) {
    var key = recipeKey(id);
    consumer.accept(key, new ContainerFillingRecipe(recipeSerializer, id, group, fluidAmount, BuiltInRegistries.ITEM.getValue(result)), this.buildOptionalAdvancement(key, "casting"));
  }
}