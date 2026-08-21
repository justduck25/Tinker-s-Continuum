package slimeknights.tconstruct.plugin.jei.material;

import com.google.common.collect.Streams;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.plugin.jei.MantleJEIConstants;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.recipe.material.MaterialsCraftingTableRecipe;
import slimeknights.tconstruct.library.recipe.material.ShapelessMaterialsRecipe;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Common logic for {@link ShapedMaterialsExtension} and {@link ShapelessMaterialsExtension} */
public class MaterialsCraftingExtension<T extends CraftingRecipe & MaterialsCraftingTableRecipe> implements ICraftingCategoryExtension<T> {
  protected final T recipe;
  private final ItemStack plainResult;
  private final List<ItemStack> result;
  @Nullable
  private final int[] materialSlots;

  public MaterialsCraftingExtension(T recipe) {
    this.recipe = recipe;
    this.plainResult = recipe.assemble(CraftingInput.EMPTY);

    // if we have just the one part, set the output to match its material
    if (recipe.getPartCount() == 1) {
      Ingredient firstPart = recipe.getParts().get(0);
      this.result = firstPart.items().map(variant -> {
        ItemStack variantStack = new ItemStack(variant.value());
        ItemStack stack = plainResult.copy();
        if (variantStack.getItem() instanceof IMaterialItem materialItem) {
          recipe.setMaterial(stack, materialItem.getMaterial(variantStack));
        } else {
          recipe.setMaterial(stack, MaterialRecipeCache.findRecipe(variantStack).getMaterial().getVariant());
        }
        return stack;
      }).toList();
      this.materialSlots = getMaterialSlots(recipe, firstPart);
      // otherwise, use a display material. allow display tool part if it has just 1 material
    } else if (recipe.getExtraMaterials().isEmpty() && plainResult.getItem() instanceof IMaterialItem materialItem) {
      this.result = List.of(materialItem.setMaterialForced(plainResult, ToolBuildHandler.getRenderMaterial(0)));
      this.materialSlots = null;
    } else {
      // display tool
      this.result = List.of(IModifiableDisplay.getDisplayStack(plainResult));
      this.materialSlots = null;
    }
  }

  /** {@return Instance of the shapeless extension, or null if the recipe is invalid for display} */
  @Nullable
  public static MaterialsCraftingExtension<ShapelessMaterialsRecipe> shapeless(ShapelessMaterialsRecipe recipe) {
    List<Ingredient> parts = recipe.getParts();
    for (int i = 0; i < recipe.getPartCount(); i++) {
      if (parts.get(i).isEmpty()) {
        return null;
      }
    }
    return new MaterialsCraftingExtension<>(recipe);
  }

  /** Gets the material slots for the given recipe */
  protected int[] getMaterialSlots(T recipe, Ingredient firstPart) {
    return new int[] {0};
  }

@Override
  public List<SlotDisplay> getIngredients(RecipeHolder<T> recipeHolder) {
    if (recipe instanceof ShapedRecipe shaped) {
      return shaped.getIngredients().stream().map(Ingredient::optionalIngredientToDisplay).toList();
    }
    return recipe.getParts().stream().map(Ingredient::display).toList();
  }
  /** Sets the recipe in the builder */
  public static void setRecipe(ICraftingCategoryExtension self, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, CraftingRecipe recipe, List<ItemStack> result, ItemStack plainResult, @Nullable int[] materialSlots) {
    builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(plainResult);

    // apply ingredient stacks
    List<List<ItemStack>> inputStacks;
    if (recipe instanceof ShapedRecipe shaped) {
      inputStacks = shaped.getIngredients().stream().map(optional -> optional.map(ingredient -> ingredient.items().map(holder -> new ItemStack(holder.value())).toList()).orElseGet(List::of)).toList();
    } else {
      inputStacks = ((MaterialsCraftingTableRecipe) recipe).getParts().stream().map(ingredient -> ingredient.items().map(holder -> new ItemStack(holder.value())).toList()).toList();
    }
    // shapeless needs its width and height set, but we also want to recover those sizes, so calculate it locally
    int width = 0;
    int height = 0;
    if (recipe instanceof ShapedRecipe shaped) {
      width = shaped.getWidth();
      height = shaped.getHeight();
    }
    if (width <= 0 || height <= 0) {
      width = height = getShapelessSize(inputStacks.size());
      builder.setShapeless();
    }
    List<IRecipeSlotBuilder> inputs = craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputStacks, width, height);
    IRecipeSlotBuilder output = craftingGridHelper.createAndSetOutputs(builder, result);
    if (inputs.size() != 9) {
      Mantle.logger.error("Failed to create focus link for {} as the layout {} is not 3x3", recipe.getClass().getSimpleName(), builder.getClass().getName());
    } else if (materialSlots != null) {
      // apply focus links
      int finalWidth = width;
      int finalHeight = height;
      builder.createFocusLink(Streams.concat(
        Stream.of(output),
        Arrays.stream(materialSlots).mapToObj(i -> inputs.get(MantleJEIConstants.getCraftingIndex(i, finalWidth, finalHeight)))
      ).toArray(IRecipeSlotBuilder[]::new));
    }
  }

  @Override
  public void setRecipe(RecipeHolder<T> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    setRecipe(this, builder, craftingGridHelper, recipe, result, plainResult, materialSlots);
  }

  /** Gets the width and height of the grid for a shapeless recipe. */
  private static int getShapelessSize(int total) {
    if (total > 4) {
      return 3;
    } else if (total > 1) {
      return 2;
    } else {
      return 1;
    }
  }
}
