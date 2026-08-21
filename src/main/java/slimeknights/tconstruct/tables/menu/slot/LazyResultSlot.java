package slimeknights.tconstruct.tables.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;

/**
 * Slot for display of {@link LazyResultContainer}.
 */
@SuppressWarnings("WeakerAccess")
public class LazyResultSlot extends Slot {
  protected final LazyResultContainer inventory;
  protected int amountCrafted = 0;
  public LazyResultSlot(LazyResultContainer inventory, int xPosition, int yPosition) {
    super(inventory, 0, xPosition, yPosition);
    this.inventory = inventory;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return false;
  }

  @Override
  public ItemStack remove(int amount) {
    if (this.hasItem()) {
      this.amountCrafted += Math.min(amount, this.getItem().getCount());
    }
    return super.remove(amount);
  }

  @Override
  public void onTake(Player player, ItemStack stack) {
    // MC 26.x does not always route result-slot takes through remove(), notably for synced lazy outputs.
    // Ensure taking a visible result always consumes the recipe once instead of leaving an infinite output.
    if (amountCrafted <= 0 && !stack.isEmpty()) {
      amountCrafted = stack.getCount();
    }
    inventory.craftResult(player, stack, amountCrafted);
    amountCrafted = 0;
  }
  @Override
  protected void onQuickCraft(ItemStack stack, int amount) {
    this.amountCrafted += amount;
    this.checkTakeAchievements(stack);
  }

  @Override
  protected void onSwapCraft(int numItemsCrafted) {
    this.amountCrafted += numItemsCrafted;
  }
}
