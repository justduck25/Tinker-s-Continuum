package slimeknights.tconstruct.library.recipe.casting;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;

import javax.annotation.Nonnull;

/** Shared logic between item and material casting */
public abstract class AbstractCastingRecipe implements ICastingRecipe {
  /* Common fields */
  public static final Ingredient EMPTY_INGREDIENT = slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA;
  protected static final LoadableField<Ingredient,AbstractCastingRecipe> CAST_FIELD = IngredientLoadable.ALLOW_EMPTY.defaultField("cast", EMPTY_INGREDIENT, AbstractCastingRecipe::getCast);
  protected static final LoadableField<Boolean,AbstractCastingRecipe> CAST_CONSUMED_FIELD = BooleanLoadable.INSTANCE.defaultField("cast_consumed", false, false, AbstractCastingRecipe::isConsumed);
  protected static final LoadableField<Boolean,AbstractCastingRecipe> SWITCH_SLOTS_FIELD = BooleanLoadable.INSTANCE.defaultField("switch_slots", false, false, AbstractCastingRecipe::switchSlots);

  @Getter @Nonnull
  private final RecipeType<? extends Recipe<ICastingContainer>> type;
  @Getter
  private final Identifier id;
  @Getter
  private final String group;
  /** 'cast' item for recipe (doesn't have to be an actual 'cast') */
  @Getter
  private final Ingredient cast;
  @Getter
  private final boolean consumed;
  @Getter @Accessors(fluent = true)
  private final boolean switchSlots;

  @SuppressWarnings("unchecked")
  protected AbstractCastingRecipe(RecipeType<?> type, Identifier id, String group, Ingredient cast, boolean consumed, boolean switchSlots) {
    this.type = (RecipeType<? extends Recipe<ICastingContainer>>)type;
    this.id = id;
    this.group = group;
    this.cast = cast;
    this.consumed = consumed;
    this.switchSlots = switchSlots;
  }

  public NonNullList<Ingredient> getIngredients() {
    return NonNullList.of(EMPTY_INGREDIENT, this.cast);
  }

  /** Checks if the casting input matches this recipe's cast slot, including intentionally empty casts. */
  protected boolean matchesCast(ItemStack stack) {
    return this.cast.test(stack) || (this.cast == EMPTY_INGREDIENT && stack.isEmpty());
  }
}
