package slimeknights.tconstruct.library.recipe.modifiers.adding;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.common.TinkerTags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for overslime recipes
 */
@RequiredArgsConstructor(staticName = "modifier")
public class OverslimeModifierRecipeBuilder extends AbstractRecipeBuilder<OverslimeModifierRecipeBuilder> {
  @Setter @Accessors(chain = true)
  private Ingredient tools = tagIngredient(TinkerTags.Items.DURABILITY);
  private final Ingredient ingredient;
  private final int restoreAmount;

  private static Ingredient tagIngredient(TagKey<Item> tag) {
    return slimeknights.tconstruct.library.recipe.ingredient.LegacyIngredientType.ofTag(tag);
  }

  /** Creates a new builder for the given item */
  public static OverslimeModifierRecipeBuilder modifier(ItemLike item, int restoreAmount) {
    return modifier(Ingredient.of(item), restoreAmount);
  }

  @Override
  public void save(RecipeOutput consumer) {
    ItemStack stack = ingredient.items().map(ItemStack::new).findFirst().orElseThrow(() -> new IllegalStateException("Empty ingredient not allowed"));
    save(consumer, BuiltInRegistries.ITEM.getKey(stack.getItem()));
  }

  @Override
  public void save(RecipeOutput consumer, Identifier id) {
    if (ingredient.isEmpty()) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    var key = recipeKey(id);
    consumer.accept(key, new OverslimeModifierRecipe(id, tools, ingredient, restoreAmount), buildOptionalAdvancement(key, "modifiers"));
  }

  /** Creates a crafting table overslime repair recipe */
  public OverslimeModifierRecipeBuilder saveCrafting(RecipeOutput consumer, Identifier id) {
    if (ingredient.isEmpty()) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    var key = recipeKey(id);
    consumer.accept(key, new OverslimeCraftingTableRecipe(id, tools, ingredient, restoreAmount), buildOptionalAdvancement(key, "modifiers"));
    return this;
  }
}
