package slimeknights.tconstruct.plugin.jei;

import lombok.Getter;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe.SLOT_SIZE;
import static slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe.X_OFFSET;
import static slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe.Y_OFFSET;

public class ToolBuildingCategory implements IRecipeCategory<ToolBuildingRecipe> {
  private static final Identifier BACKGROUND_LOC = TConstruct.getResource("textures/gui/jei/tinker_station.png");
  private static final Component TITLE = TConstruct.makeTranslation("jei", "tinkering.tool_building");
  private static final int WIDTH = 134;
  private static final int HEIGHT = 66;
  private static final int ITEM_SIZE = 16;

  @Getter
  private final IDrawable icon;
  @Getter
  private final IDrawable background;
  private final IDrawable anvil;
  private final IDrawable slotBg;
  private final IDrawable slotBorder;

  public ToolBuildingCategory(IGuiHelper guiHelper) {
    this.icon = guiHelper.createDrawableItemStack(TinkerTools.pickaxe.get().getRenderTool());
    this.background = guiHelper.createDrawable(BACKGROUND_LOC, 122, 77, WIDTH, HEIGHT);
    this.slotBg = guiHelper.createDrawable(BACKGROUND_LOC, 144, 59, SLOT_SIZE, SLOT_SIZE);
    this.slotBorder = guiHelper.createDrawable(BACKGROUND_LOC, 162, 59, SLOT_SIZE, SLOT_SIZE);
    this.anvil = guiHelper.createDrawable(BACKGROUND_LOC, 128, 61, ITEM_SIZE, ITEM_SIZE);
  }

  @Override
  public RecipeType<ToolBuildingRecipe> getRecipeType() {
    return TConstructJEIConstants.TOOL_BUILDING;
  }

  @Override
  public Component getTitle() {
    return TITLE;
  }

  @Override
  public int getWidth() {
    return WIDTH;
  }

  @Override
  public int getHeight() {
    return HEIGHT;
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, ToolBuildingRecipe recipe, IFocusGroup focuses) {
    List<List<ItemStack>> partsAndExtras = Stream.concat(recipe.getAllToolParts().stream(),
      recipe.getExtraRequirements().stream().map(ingredient -> ingredient.items().map(ItemStack::new).toList())).toList();
    List<LayoutSlot> layoutSlots = recipe.getLayoutSlots();

    int missingSlots = partsAndExtras.size() - layoutSlots.size();
    if (missingSlots < 0) {
      partsAndExtras = new ArrayList<>(partsAndExtras);
      for (int additionalItem = 0; additionalItem > missingSlots; additionalItem--) {
        partsAndExtras.add(List.of(ItemStack.EMPTY));
      }
    }

    IRecipeSlotBuilder firstSlot = null;
    for (int i = 0; i < layoutSlots.size(); i++) {
      IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, layoutSlots.get(i).getX() + X_OFFSET, layoutSlots.get(i).getY() + Y_OFFSET)
        .addItemStacks(partsAndExtras.get(i));
      if (i == 0) {
        firstSlot = slot;
      }
    }

    List<ItemStack> result = recipe.getDisplayOutput();
    IRecipeSlotBuilder resultSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 26, 23).addItemStacks(result);
    if (firstSlot != null && result.size() > 1 && !partsAndExtras.isEmpty() && partsAndExtras.get(0).size() == result.size()) {
      builder.createFocusLink(resultSlot, firstSlot);
    }

    List<ItemStack> hiddenInputs = recipe.getHiddenInputs();
    if (!hiddenInputs.isEmpty()) {
      builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(hiddenInputs);
    }
  }

  @Override
  public void draw(ToolBuildingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
    ItemStack outputStack = recipe.getOutput() instanceof IModifiableDisplay modifiable ? modifiable.getRenderTool() : recipe.getOutput().asItem().getDefaultInstance();
    Matrix3x2fStack pose = graphics.pose();
    pose.pushMatrix();
    pose.translate(5, 6.5f);
    pose.scale(3.7f, 3.7f);
    graphics.item(outputStack, 0, 0);
    pose.popMatrix();

    graphics.fill(5, 6, 75, 66, 0xD0FFFFFF);
    for (LayoutSlot layoutSlot : recipe.getLayoutSlots()) {
      this.slotBg.draw(graphics, layoutSlot.getX() + X_OFFSET - 1, layoutSlot.getY() + Y_OFFSET - 1);
    }

    for (LayoutSlot layoutSlot : recipe.getLayoutSlots()) {
      this.slotBorder.draw(graphics, layoutSlot.getX() + X_OFFSET - 1, layoutSlot.getY() + Y_OFFSET - 1);
    }

    if (recipe.requiresAnvil()) {
      this.anvil.draw(graphics, 76, 44);
    }
  }

  @Override
  public void getTooltip(ITooltipBuilder tooltip, ToolBuildingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
    if (recipe.requiresAnvil() && GuiUtil.isHovered((int) mouseX, (int) mouseY, 76, 44, ITEM_SIZE, ITEM_SIZE)) {
      tooltip.add(TConstruct.makeTranslation("jei", "tinkering.tool_building.anvil"));
    }
  }

  @Override
  public Identifier getIdentifier(ToolBuildingRecipe recipe) {
    return recipe.getId();
  }
}
