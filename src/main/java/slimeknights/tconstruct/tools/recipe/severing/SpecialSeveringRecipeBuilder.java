package slimeknights.tconstruct.tools.recipe.severing;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.Objects;
import java.util.function.Supplier;

/** Builder for severing recipes that have only the base chance and looting bonus as fields */
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "serializer")
public class SpecialSeveringRecipeBuilder extends AbstractRecipeBuilder<SpecialSeveringRecipeBuilder> {
  private final RecipeSerializer<? extends SeveringRecipe> serializer;
  private float baseChance = 0.05f;
  private float lootingBonus = 0.01f;

  /** Creates a new builder for the given serializer. */
  public static SpecialSeveringRecipeBuilder serializer(Supplier<? extends RecipeSerializer<? extends SeveringRecipe>> supplier) {
    return serializer(supplier.get());
  }

  /** Doubles the drop chances for this rare mob */
  public SpecialSeveringRecipeBuilder rareMob() {
    baseChance = 0.1f;
    lootingBonus = 0.02f;
    return this;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void save(RecipeOutput consumer) {
    save(consumer, Objects.requireNonNull(BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer)));
  }

  @Override
  public void save(RecipeOutput consumer, Identifier id) {
    SeveringRecipe recipe;
    if (serializer == TinkerModifiers.playerBeheadingSerializer.get()) {
      recipe = new PlayerBeheadingRecipe(id, baseChance, lootingBonus);
    } else if (serializer == TinkerModifiers.snowGolemBeheadingSerializer.get()) {
      recipe = new SnowGolemBeheadingRecipe(id, baseChance, lootingBonus);
    } else if (serializer == TinkerModifiers.mooshroomDemushroomingSerializer.get()) {
      recipe = new MooshroomDemushroomingRecipe(id, baseChance, lootingBonus);
    } else if (serializer == TinkerModifiers.sheepShearing.get()) {
      recipe = new SheepShearingRecipe(id, baseChance, lootingBonus);
    } else {
      throw new IllegalStateException("Unsupported special severing serializer: " + serializer);
    }
    var key = recipeKey(id);
    consumer.accept(key, recipe, buildOptionalAdvancement(key, "severing"));
  }
}
