package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.Recipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Codec adapters for recipes wrapped by {@link MaterialRecipeWrapper}. */
final class MaterialRecipeMapCodecs {
  private static final Codec<List<Ingredient>> SHAPELESS_INGREDIENTS = Codec.lazyInitialized(
    () -> Ingredient.CODEC.listOf(1, 9)
  );

  static final MapCodec<Recipe<?>> SHAPED_MATERIALS = new MapCodec<>() {
    @Override
    public <T> DataResult<Recipe<?>> decode(com.mojang.serialization.DynamicOps<T> ops, MapLike<T> input) {
      return ShapedRecipe.MAP_CODEC.decode(ops, input).flatMap(base ->
        ItemStackTemplate.CODEC.fieldOf("result").decode(ops, input).flatMap(resultTemplate ->
          ShapedRecipePattern.Data.MAP_CODEC.decode(ops, input).flatMap(patternData ->
          Codec.STRING.fieldOf("parts").decode(ops, input).flatMap(partPattern ->
            MaterialRecipeCodecs.EXTRA_MATERIALS.optionalFieldOf("extra_materials", List.of()).decode(ops, input).flatMap(extraMaterials -> {
              List<Ingredient> parts = new ArrayList<>(partPattern.length());
              for (int i = 0; i < partPattern.length(); i++) {
                char symbol = partPattern.charAt(i);
                Ingredient ingredient = patternData.key().get(symbol);
                if (ingredient == null) {
                  return DataResult.error(() -> "Parts references symbol '" + symbol + "' but it's not defined in the key");
                }
                parts.add(ingredient);
              }
              return DataResult.success(new ShapedMaterialsRecipe(base, resultTemplate, List.copyOf(parts), extraMaterials));
            })
          )
        )
      ));
    }

    @Override
    public <T> RecordBuilder<T> encode(Recipe<?> value, com.mojang.serialization.DynamicOps<T> ops, RecordBuilder<T> prefix) {
      MaterialRecipeWrapper<?> wrapper = MaterialRecipeWrapper.find(value);
      Recipe<?> original = MaterialRecipeWrapper.original(value);
      if (wrapper == null || !(original instanceof ShapedRecipe shaped)) {
        return prefix.withErrorsFrom(DataResult.error(() -> "Expected a material-wrapped shaped recipe"));
      }
      RecordBuilder<T> result = ShapedRecipe.MAP_CODEC.encode(shaped, ops, prefix);
      result = result.add("parts", Codec.STRING.encodeStart(ops, wrapper.parts()));
      if (!wrapper.extraMaterials().isEmpty()) {
        result = result.add("extra_materials", MaterialRecipeCodecs.EXTRA_MATERIALS.encodeStart(ops, wrapper.extraMaterials()));
      }
      return result;
    }

    @Override
    public <T> Stream<T> keys(com.mojang.serialization.DynamicOps<T> ops) {
      return Stream.concat(
        ShapedRecipe.MAP_CODEC.keys(ops),
        Stream.of(ops.createString("parts"), ops.createString("extra_materials"))
      );
    }
  };

  static final MapCodec<Recipe<?>> SHAPELESS_MATERIALS = new MapCodec<>() {
    @Override
    public <T> DataResult<Recipe<?>> decode(com.mojang.serialization.DynamicOps<T> ops, MapLike<T> input) {
      return ShapelessRecipe.MAP_CODEC.decode(ops, input).flatMap(base ->
        ItemStackTemplate.CODEC.fieldOf("result").decode(ops, input).flatMap(resultTemplate ->
          SHAPELESS_INGREDIENTS.fieldOf("ingredients").decode(ops, input).flatMap(ingredients ->
          Codec.INT.fieldOf("parts").decode(ops, input).flatMap(partCount ->
            MaterialRecipeCodecs.EXTRA_MATERIALS.optionalFieldOf("extra_materials", List.of()).decode(ops, input).map(extraMaterials ->
              (Recipe<?>)new ShapelessMaterialsRecipe(base, resultTemplate, ingredients, partCount, extraMaterials)
            )
          )
        )
      ));
    }

    @Override
    public <T> RecordBuilder<T> encode(Recipe<?> value, com.mojang.serialization.DynamicOps<T> ops, RecordBuilder<T> prefix) {
      MaterialRecipeWrapper<?> wrapper = MaterialRecipeWrapper.find(value);
      Recipe<?> original = MaterialRecipeWrapper.original(value);
      if (wrapper == null || !(original instanceof ShapelessRecipe shapeless)) {
        return prefix.withErrorsFrom(DataResult.error(() -> "Expected a material-wrapped shapeless recipe"));
      }
      RecordBuilder<T> result = ShapelessRecipe.MAP_CODEC.encode(shapeless, ops, prefix);
      result = result.add("parts", Codec.INT.encodeStart(ops, wrapper.partCount()));
      if (!wrapper.extraMaterials().isEmpty()) {
        result = result.add("extra_materials", MaterialRecipeCodecs.EXTRA_MATERIALS.encodeStart(ops, wrapper.extraMaterials()));
      }
      return result;
    }

    @Override
    public <T> Stream<T> keys(com.mojang.serialization.DynamicOps<T> ops) {
      return Stream.concat(
        ShapelessRecipe.MAP_CODEC.keys(ops),
        Stream.of(ops.createString("parts"), ops.createString("extra_materials"))
      );
    }
  };

  static final MapCodec<Recipe<?>> SHAPED_MATERIAL = new MapCodec<>() {
    @Override
    public <T> DataResult<Recipe<?>> decode(com.mojang.serialization.DynamicOps<T> ops, MapLike<T> input) {
      return ShapedRecipe.MAP_CODEC.decode(ops, input).flatMap(base ->
        ItemStackTemplate.CODEC.fieldOf("result").decode(ops, input).flatMap(resultTemplate ->
          MaterialRecipeCodecs.EXTRA_MATERIALS.optionalFieldOf("extra_materials", List.of()).decode(ops, input).map(extraMaterials ->
            (Recipe<?>)new ShapedMaterialRecipe(base, resultTemplate, extraMaterials)
          )
        )
      );
    }

    @Override
    public <T> RecordBuilder<T> encode(Recipe<?> value, com.mojang.serialization.DynamicOps<T> ops, RecordBuilder<T> prefix) {
      MaterialRecipeWrapper<?> wrapper = MaterialRecipeWrapper.find(value);
      Recipe<?> original = MaterialRecipeWrapper.original(value);
      if (wrapper == null || !(original instanceof ShapedRecipe shaped)) {
        return prefix.withErrorsFrom(DataResult.error(() -> "Expected a material-wrapped shaped recipe"));
      }
      RecordBuilder<T> result = ShapedRecipe.MAP_CODEC.encode(shaped, ops, prefix);
      if (!wrapper.extraMaterials().isEmpty()) {
        result = result.add("extra_materials", MaterialRecipeCodecs.EXTRA_MATERIALS.encodeStart(ops, wrapper.extraMaterials()));
      }
      return result;
    }

    @Override
    public <T> Stream<T> keys(com.mojang.serialization.DynamicOps<T> ops) {
      return Stream.concat(
        ShapedRecipe.MAP_CODEC.keys(ops),
        Stream.of(ops.createString("extra_materials"))
      );
    }
  };

  private MaterialRecipeMapCodecs() {}
}
