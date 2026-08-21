package slimeknights.tconstruct.tables.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.tables.block.entity.inventory.CraftingContainerWrapper;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;

/**
 * Extension of vanilla result slot that adds player sensitive display while keeping vanilla grid consumption.
 */
public class PlayerSensitiveLazyResultSlot extends ResultSlot {
  /** Player using the slot */
  private final Player player;
  /** Lazy result inventory, needed for client/server synced display */
  private final LazyResultContainer inventory;
  /** Last non-player sensitive result */
  private ItemStack lastNormalResult;
  /** Last player sensitive result */
  private ItemStack lastPlayerResult;

  public PlayerSensitiveLazyResultSlot(Player player, CraftingContainerWrapper craftingInventory, LazyResultContainer inventory, int xPosition, int yPosition) {
    super(player, craftingInventory, inventory, 0, xPosition, yPosition);
    this.player = player;
    this.inventory = inventory;
  }

  @Override
  public ItemStack getItem() {
    // On the client, MC 26.x does not expose full recipe lookup through RecipeAccess.
    // The crafting station syncs the server-authoritative display stack, so render that directly.
    if (player.level().isClientSide()) {
      return inventory.getResult();
    }
    // if we have not yet calculated the player specific result, or the inventory recalculated it, then recalculate
    ItemStack newResult = inventory.getResult();
    if (lastPlayerResult == null || lastNormalResult != newResult) {
      lastNormalResult = newResult;
      lastPlayerResult = inventory.calcResult(player);
    }
    return lastPlayerResult;
  }

  @Override
  public ItemStack remove(int amount) {
    ItemStack result = getItem().copy();
    if (!result.isEmpty()) {
      this.removeCount += Math.min(amount, result.getCount());
    }
    return result;
  }

  @Override
  public void onTake(Player player, ItemStack stack) {
    if (this.removeCount <= 0 && !stack.isEmpty()) {
      this.removeCount = stack.getCount();
    }
    super.onTake(player, stack);
    inventory.clearContent();
    if (!player.level().isClientSide()) {
      inventory.calcResult(player);
    }
  }
}