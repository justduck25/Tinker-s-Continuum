package slimeknights.tconstruct.plugin.jei.transfer;

import lombok.Getter;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implements JEI recipe transfer in the tinker station. */
@Internal
public class TinkerStationTransferInfo<T> implements IRecipeTransferInfo<TinkerStationContainerMenu,T>, IRecipeTransferHandler<TinkerStationContainerMenu,T> {
  @Getter
  private final IRecipeType<T> recipeType;
  private final IRecipeTransferHandlerHelper handlerHelper;
  private final IRecipeTransferHandler<TinkerStationContainerMenu,T> handler;

  public TinkerStationTransferInfo(IRecipeType<T> recipeType, IRecipeTransferHandlerHelper handlerHelper) {
    this.recipeType = recipeType;
    this.handlerHelper = handlerHelper;
    this.handler = handlerHelper.createUnregisteredRecipeTransferHandler(this);
  }

  @Override
  public Class<TinkerStationContainerMenu> getContainerClass() {
    return TinkerStationContainerMenu.class;
  }

  @Override
  public Optional<MenuType<TinkerStationContainerMenu>> getMenuType() {
    return Optional.of(TinkerTables.tinkerStationContainer.get());
  }

  @Override
  public boolean canHandle(TinkerStationContainerMenu container, T recipe) {
    return true;
  }

  @Nullable
  @Override
  public IRecipeTransferError transferRecipe(TinkerStationContainerMenu container, T recipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
    if (!handlerHelper.recipeTransferHasServerSupport()) {
      return handlerHelper.createUserErrorWithTooltip(Component.translatable("jei.tooltip.error.recipe.transfer.no.server"));
    }

    List<IRecipeSlotView> slotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
    int size = container.getInputSlots().size();
    if (!validateRecipeFits(slotViews, size)) {
      return this.handlerHelper.createUserErrorWithTooltip(Component.translatable("jei.tconstruct.tinker_station.too_large"));
    }

    IRecipeSlotsView filteredSlotView = recipeSlotsView;
    if (slotViews.size() > size) {
      filteredSlotView = handlerHelper.createRecipeSlotsView(slotViews.subList(0, size));
    }
    return this.handler.transferRecipe(container, recipe, filteredSlotView, player, maxTransfer, doTransfer);
  }

  @Override
  public List<Slot> getRecipeSlots(TinkerStationContainerMenu container, T recipe) {
    return container.getInputSlots();
  }

  @Override
  public List<Slot> getInventorySlots(TinkerStationContainerMenu container, T recipe) {
    List<Slot> slots = new ArrayList<>();
    int start = Math.max(0, container.slots.size() - 36);
    for (int i = start; i < start + 36 && i < container.slots.size(); i++) {
      slots.add(container.getSlot(i));
    }
    return slots;
  }

  private static boolean validateRecipeFits(List<IRecipeSlotView> slotViews, int size) {
    int bound = slotViews.size();
    for (int i = size; i < bound; i++) {
      IRecipeSlotView slotView = slotViews.get(i);
      if (!slotView.isEmpty() && slotView.getDisplayedIngredient(VanillaTypes.ITEM_STACK).isPresent()) {
        return false;
      }
    }
    return true;
  }
}
