package slimeknights.tconstruct.library.recipe.molding;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "molding")
public class MoldingRecipeBuilder extends AbstractRecipeBuilder<MoldingRecipeBuilder> {
  private final ItemOutput output;
  private final TypeAwareRecipeSerializer<MoldingRecipe> serializer;
  private Ingredient material = slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA;
  private Ingredient pattern = slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA;
  private boolean patternConsumed = false;

  /**
   * Creates a new builder of the given item
   * @param item  Item output
   * @return  Recipe
   */
  public static MoldingRecipeBuilder moldingTable(ItemLike item) {
    return molding(ItemOutput.fromItem(item), TinkerSmeltery.typeAware(TinkerRecipeTypes.MOLDING_TABLE, TinkerSmeltery.moldingTableSerializer));
  }

  /**
   * Creates a new builder of the given item
   * @param item  Item output
   * @return  Recipe
   */
  public static MoldingRecipeBuilder moldingBasin(ItemLike item) {
    return molding(ItemOutput.fromItem(item), TinkerSmeltery.typeAware(TinkerRecipeTypes.MOLDING_BASIN, TinkerSmeltery.moldingBasinSerializer));
  }

  /* Inputs */

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder setMaterial(Ingredient ingredient) {
    this.material = ingredient;
    return this;
  }

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder setMaterial(ItemLike item) {
    return setMaterial(Ingredient.of(item));
  }

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder setMaterial(TagKey<Item> tag) {
    return setMaterial(slimeknights.tconstruct.library.recipe.ingredient.LegacyIngredientType.ofTag(tag));
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder setPattern(Ingredient ingredient, boolean consumed) {
    this.pattern = ingredient;
    this.patternConsumed = consumed;
    return this;
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder setPattern(ItemLike item, boolean consumed) {
    return setPattern(Ingredient.of(item), consumed);
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder setPattern(TagKey<Item> tag, boolean consumed) {
    return setPattern(slimeknights.tconstruct.library.recipe.ingredient.LegacyIngredientType.ofTag(tag), consumed);
  }


  /* Building */

  @Override
  public void save(RecipeOutput consumer) {
    save(consumer, BuiltInRegistries.ITEM.getKey(output.get().getItem()));
  }

  @Override
  public void save(RecipeOutput consumer, Identifier id) {
    if (material == slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA) {
      throw new IllegalStateException("Missing material for molding recipe");
    }
    var key = recipeKey(id);
    consumer.accept(key, new MoldingRecipe(serializer, id, material, pattern, patternConsumed, output), buildOptionalAdvancement(key, "molding"));
  }

//  private class Finished implements RecipeOutput {
//    public Finished(Identifier ID, @Nullable Identifier advancementID) {
//    }
//
//    @Override
//    public void serializeRecipeData(JsonObject json) {
//      json.add("material", material.toJson());
//      if (pattern != Ingredient.EMPTY) {
//        json.add("pattern", pattern.toJson());
//        if (patternConsumed) {
//          json.addProperty("pattern_consumed", true);
//        }
//      }
//      json.add("result", output.serialize(false));
//    }
//
//    @Override
//    public RecipeSerializer<?> getType() {
//      return serializer;
//    }
//  }
}
