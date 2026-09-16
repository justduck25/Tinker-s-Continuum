package slimeknights.tconstruct.smeltery.block.entity.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import slimeknights.mantle.inventory.SingleItemHandler;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.network.InventorySlotSyncPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.utils.WeakListenerList;
import slimeknights.tconstruct.smeltery.block.entity.component.DuctBlockEntity;

import java.util.function.Consumer;

/**
 * Item handler for the duct
 */
public class DuctItemHandler extends SingleItemHandler<DuctBlockEntity> {
  private final WeakListenerList onUpdate = new WeakListenerList();
  private FluidStack fluid = null;
  public DuctItemHandler(DuctBlockEntity parent) {
    super(parent, 1);
  }

  /** Called when the fluid changes to alert listeners */
  private void updateFluid() {
    fluid = null;
    onUpdate.run();
    parent.onFilterChanged();
  }

  /** Clears cached fluid state after loading directly from NBT. */
  public void refreshFluid() {
    updateFluid();
  }

  /**
   * Adds a listener to run when the fluid updates.
   * Generally these should just clear cache rather than immediately consuming the new fluid.
   */
  public <T> void addListener(T parent, Consumer<T> listener) {
    onUpdate.addListener(parent, listener);
  }

  /**
   * Sets the stack in this duct
   * @param newStack  New stack
   */
  @Override
  public void setStack(ItemStack newStack) {
    Level world = parent.getLevel();
    ItemStack current = getStack();
    // if both are empty, assume shift click so we need to update
    boolean hasChange = (current.isEmpty() && newStack.isEmpty()) || !ItemStack.matches(current, newStack);
    super.setStack(newStack);
    if (hasChange) {
      updateFluid();
      if (world != null) {
        if (!world.isClientSide()) {
          BlockPos pos = parent.getBlockPos();
          TinkerNetwork.getInstance().sendToClientsAround(new InventorySlotSyncPacket(newStack, 0, pos), world, pos);
        } else {
          parent.updateFluid();
        }
      }
    }
  }

  @Override
  protected boolean isItemValid(ItemStack stack) {
    if (getContainedFluid(stack).isEmpty()) {
      return false;
    }
    // the item or its empty container must be in the tag
    if (stack.is(TinkerTags.Items.DUCT_CONTAINERS)) {
      return true;
    }
    if (stack.getItem() instanceof BucketItem bucket && bucket.getContent() != Fluids.EMPTY) {
      return true;
    }
    ItemStack container = stack.getCraftingRemainder() == null ? ItemStack.EMPTY : stack.getCraftingRemainder().create();
    return !container.isEmpty() && container.is(TinkerTags.Items.DUCT_CONTAINERS);
  }

  /**
   * Gets the fluid filter for this duct
   * @return  Fluid filter
   */
  public FluidStack getFluid() {
    if (fluid == null) {
      fluid = getContainedFluid(getStack());
    }
    return fluid;
  }

  /** Gets the fluid stored inside the filter item. */
  private static FluidStack getContainedFluid(ItemStack stack) {
    if (stack.isEmpty()) {
      return FluidStack.EMPTY;
    }
    ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack).oneByOne().getCapability(Capabilities.Fluid.ITEM);
    if (handler != null) {
      for (int i = 0; i < handler.size(); i++) {
        FluidResource resource = handler.getResource(i);
        int amount = handler.getAmountAsInt(i);
        if (!resource.isEmpty() && amount > 0) {
          return resource.toStack(amount);
        }
      }
      try (Transaction tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
        var extracted = ResourceHandlerUtil.extractFirst(handler, resource -> !resource.isEmpty(), Integer.MAX_VALUE, tx);
        if (extracted != null && extracted.amount() > 0) {
          return extracted.resource().toStack(extracted.amount());
        }
      }
    }
    if (stack.getItem() instanceof BucketItem bucket && bucket.getContent() != Fluids.EMPTY) {
      return new FluidStack(bucket.getContent(), FluidType.BUCKET_VOLUME);
    }
    return FluidStack.EMPTY;
  }
}
