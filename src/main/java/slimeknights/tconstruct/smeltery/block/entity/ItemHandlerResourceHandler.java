package slimeknights.tconstruct.smeltery.block.entity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Bridges Tinkers' legacy IItemHandler inventories to NeoForge's ResourceHandler item capability. */
public class ItemHandlerResourceHandler implements ResourceHandler<ItemResource> {
  private final IItemHandler handler;
  private final Journal journal = new Journal();

  public ItemHandlerResourceHandler(IItemHandler handler) {
    this.handler = handler;
  }

  @Override
  public int size() {
    return handler.getSlots();
  }

  @Override
  public ItemResource getResource(int index) {
    ItemStack stack = handler.getStackInSlot(index);
    return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack);
  }

  @Override
  public long getAmountAsLong(int index) {
    return handler.getStackInSlot(index).getCount();
  }

  @Override
  public long getCapacityAsLong(int index, ItemResource resource) {
    int slotLimit = handler.getSlotLimit(index);
    return resource.isEmpty() ? slotLimit : Math.min(slotLimit, resource.getMaxStackSize());
  }

  @Override
  public boolean isValid(int index, ItemResource resource) {
    return !resource.isEmpty() && handler.isItemValid(index, resource.toStack(1));
  }

  @Override
  public int insert(int index, ItemResource resource, int maxAmount, TransactionContext transaction) {
    if (resource.isEmpty() || maxAmount <= 0) {
      return 0;
    }
    journal.updateSnapshots(transaction);
    ItemStack remaining = handler.insertItem(index, resource.toStack(maxAmount), false);
    return maxAmount - remaining.getCount();
  }

  @Override
  public int extract(int index, ItemResource resource, int maxAmount, TransactionContext transaction) {
    if (resource.isEmpty() || maxAmount <= 0 || !resource.matches(handler.getStackInSlot(index))) {
      return 0;
    }
    journal.updateSnapshots(transaction);
    return handler.extractItem(index, maxAmount, false).getCount();
  }

  private class Journal extends SnapshotJournal<ItemStack[]> {
    @Override
    protected ItemStack[] createSnapshot() {
      ItemStack[] stacks = new ItemStack[handler.getSlots()];
      for (int i = 0; i < stacks.length; i++) {
        stacks[i] = handler.getStackInSlot(i).copy();
      }
      return stacks;
    }

    @Override
    protected void revertToSnapshot(ItemStack[] snapshot) {
      if (handler instanceof IItemHandlerModifiable modifiable) {
        for (int i = 0; i < snapshot.length; i++) {
          modifiable.setStackInSlot(i, snapshot[i]);
        }
      }
    }
  }
}