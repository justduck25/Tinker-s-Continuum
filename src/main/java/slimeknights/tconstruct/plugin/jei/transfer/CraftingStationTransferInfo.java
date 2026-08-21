package slimeknights.tconstruct.plugin.jei.transfer;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Dynamically provides the right slot count to JEI for the crafting station. */
public class CraftingStationTransferInfo implements IRecipeTransferInfo<CraftingStationContainerMenu, RecipeHolder<CraftingRecipe>> {
  @Override
  public Class<? extends CraftingStationContainerMenu> getContainerClass() {
    return CraftingStationContainerMenu.class;
  }

  @Override
  public Optional<MenuType<CraftingStationContainerMenu>> getMenuType() {
    return Optional.of(TinkerTables.craftingStationContainer.get());
  }

  @Override
  public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
    return RecipeTypes.CRAFTING;
  }

  @Override
  public List<Slot> getInventorySlots(CraftingStationContainerMenu container, RecipeHolder<CraftingRecipe> recipe) {
    List<Slot> slots = new ArrayList<>();

    int totalSize = container.slots.size();
    int sideInventoryEnd = totalSize - 36;

    for (int i = sideInventoryEnd; i < totalSize; i++) {
      slots.add(container.getSlot(i));
    }

    Player player = SafeClientAccess.getPlayer();
    if (player != null) {
      for (int i = 10; i < sideInventoryEnd; i++) {
        Slot slot = container.getSlot(i);
        if (slot.hasItem() && slot.allowModification(player)) {
          slots.add(slot);
        }
      }
    }
    return slots;
  }

  @Override
  public List<Slot> getRecipeSlots(CraftingStationContainerMenu container, RecipeHolder<CraftingRecipe> recipe) {
    List<Slot> slots = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      slots.add(container.getSlot(i));
    }
    return slots;
  }

  @Override
  public boolean canHandle(CraftingStationContainerMenu container, RecipeHolder<CraftingRecipe> recipe) {
    return recipe.value().placementInfo().ingredients().size() <= 9;
  }
}
