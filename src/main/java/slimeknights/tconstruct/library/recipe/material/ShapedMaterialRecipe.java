package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
@SuppressWarnings("unchecked")

@Deprecated
public class ShapedMaterialRecipe extends ShapedRecipe {
    private static ItemStackTemplate template(ItemStack result) {
    return result.isEmpty() ? new ItemStackTemplate(Items.STICK) : ItemStackTemplate.fromNonEmptyStack(result);
  }

  private static ItemStackTemplate template(@Nullable ItemStackTemplate result) {
    return result == null ? new ItemStackTemplate(Items.STICK) : result;
  }

  private final Identifier id;
  private final NonNullList<Ingredient> tconstructIngredients;
  private final ItemStackTemplate result;
  private final List<MaterialVariantId> extraMaterials;
  private MaterialValueIngredient material;

  public ShapedMaterialRecipe(Identifier id, String group, net.minecraft.world.item.crafting.CraftingBookCategory category, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result, boolean showNotification, List<MaterialVariantId> extraMaterials) {
    super(new Recipe.CommonInfo(showNotification), new CraftingRecipe.CraftingBookInfo(category, group), new ShapedRecipePattern(width, height, ingredients.stream().map(Optional::ofNullable).toList(), Optional.empty()), template(result));
    this.id = id;
    this.tconstructIngredients = ingredients;
    this.result = template(result);
    this.extraMaterials = extraMaterials;
  }

  @Deprecated(forRemoval = true)
  public ShapedMaterialRecipe(Identifier id, String group, net.minecraft.world.item.crafting.CraftingBookCategory category, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result, boolean showNotification) {
    this(id, group, category, width, height, ingredients, result, showNotification, List.of());
  }

  /** Creates the runtime material recipe from a decoded vanilla shaped recipe. */
  ShapedMaterialRecipe(ShapedRecipe recipe, ItemStackTemplate result, List<MaterialVariantId> extraMaterials) {
    super(new Recipe.CommonInfo(recipe.showNotification()), new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()), recipe.pattern, template(result));
    this.id = TConstruct.getResource("decoded_shaped_material");
    NonNullList<Ingredient> ingredients = NonNullList.withSize(recipe.pattern.ingredients().size(), slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA);
    for (int i = 0; i < recipe.pattern.ingredients().size(); i++) {
      final int index = i;
      recipe.pattern.ingredients().get(index).ifPresent(ingredient -> ingredients.set(index, ingredient));
    }
    this.tconstructIngredients = ingredients;
    this.result = template(result);
    this.extraMaterials = List.copyOf(extraMaterials);
  }

  public Identifier getId() {
    return id;
  }

  @Nullable
  public MaterialValueIngredient getMaterial() {
    if (material == null) {
      for (Ingredient ingredient : tconstructIngredients) {
        if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof MaterialValueIngredient materialValue) {
          material = material == null ? materialValue : material.merge(materialValue);
        }
      }
      if (material == null) {
        TConstruct.LOG.error("No material ingredient found for material shaped recipe {}, this indicates a broken recipe", id);
      }
    }
    return material;
  }

  @Nullable
  private MaterialVariantId findMaterial(CraftingInput inventory) {
    MaterialValueIngredient material = getMaterial();
    if (material == null) {
      return null;
    }
    MaterialVariantId firstMaterial = null;
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        MaterialVariantId matchedMaterial = material.getMaterial(stack);
        if (matchedMaterial != null) {
          if (firstMaterial == null) {
            firstMaterial = matchedMaterial;
          } else if (!firstMaterial.matchesVariant(matchedMaterial)) {
            if (firstMaterial.getId().equals(matchedMaterial.getId())) {
              firstMaterial = firstMaterial.getMaterialId();
            } else {
              return null;
            }
          }
        }
      }
    }
    return firstMaterial;
  }

  @Override
  public boolean matches(CraftingInput inventory, Level level) {
    return super.matches(inventory, level) && findMaterial(inventory) != null;
  }

  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    ShapedMaterialsRecipe.setMaterial(stack, material, extraMaterials);
  }

  @Override
  public ItemStack assemble(CraftingInput inventory) {
    ItemStack stack = result.create();
    MaterialVariantId material = findMaterial(inventory);
    if (material != null) {
      setMaterial(stack, material);
    }
    return stack;
  }

  @Override
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer<ShapedRecipe>)(RecipeSerializer<?>)TinkerTables.shapedMaterialRecipeSerializer.get();
  }

  public static RecipeSerializer<ShapedMaterialRecipe> serializer() {
    return new RecipeSerializer<>(MapCodec.unit(new ShapedMaterialRecipe(TConstruct.getResource("empty_shaped_material"), "", net.minecraft.world.item.crafting.CraftingBookCategory.MISC, 1, 1, NonNullList.of(slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA), ItemStack.EMPTY, true, List.of())), StreamCodec.of((RegistryFriendlyByteBuf buf, ShapedMaterialRecipe recipe) -> {}, buf -> new ShapedMaterialRecipe(TConstruct.getResource("empty_shaped_material"), "", net.minecraft.world.item.crafting.CraftingBookCategory.MISC, 1, 1, NonNullList.of(slimeknights.mantle.recipe.ingredient.EmptyIngredient.VANILLA), ItemStack.EMPTY, true, List.of())));
  }
}