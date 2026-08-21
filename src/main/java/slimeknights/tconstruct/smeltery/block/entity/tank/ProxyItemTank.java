package slimeknights.tconstruct.smeltery.block.entity.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.FluidResourceHandlerItemAdapter;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.mantle.inventory.SingleItemHandler;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.network.InventorySlotSyncPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.fluid.EmptyFluidHandlerItem;
import slimeknights.tconstruct.library.fluid.IFluidTankUpdater;

/** Fluid handler that proxies to an item stack tank */
public class ProxyItemTank<T extends MantleBlockEntity & IFluidTankUpdater> extends SingleItemHandler<T> implements IFluidHandler {
  private IFluidHandlerItem itemTank;
  public ProxyItemTank(T parent) {
    super(parent, 1);
  }

  @SuppressWarnings("deprecation")
  @Override
  protected boolean isItemValid(ItemStack stack) {
    // can only store items that are fluid handlers, though allow blacklist in case something is really broken
    // blacklist is mostly used for items that don't support incremental filling, as this block really isn't good at working with them
    // we check the container item so we don't have to put every bucket in the tag. Not bothering with complex container items; odds are item stack sensitive just returns the same item
    return !stack.is(TinkerTags.Items.PROXY_TANK_BLACKLIST) && hasFluidHandler(stack);
  }

  /** Used by the fluid handler logic to sync changes as we directly mutate the internal stack */
  private void setStack(ItemStack newStack, boolean syncSame) {
    // if swapping to an empty stack, switch to the empty stack instance
    // prevents accidently having a 0 stack size capability
    if (newStack.isEmpty()) {
      newStack = ItemStack.EMPTY;
    }
    // update stack
    ItemStack oldStack = getStack();
    super.setStack(newStack);

    // server side may need to sync
    Level world = parent.getLevel();
    boolean needsUpdate = world != null && !world.isClientSide();
    if (oldStack != newStack) {
      // if the stack instance changed, discard cached cap and sync
      itemTank = null;
      if (needsUpdate) {
        // both stacks being empty means our stack shrunk by 1 and is being replaced with ItemStack.EMPTY
        needsUpdate = (oldStack.isEmpty() && newStack.isEmpty()) || !ItemStack.isSameItemSameComponents(oldStack, newStack);
      }
    } else if (needsUpdate) {
      needsUpdate = syncSame;
    }
    // sync changes
    if (needsUpdate) {
      parent.onTankContentsChanged();
      BlockPos pos = parent.getBlockPos();
      TinkerNetwork.getInstance().sendToClientsAround(new InventorySlotSyncPacket(newStack, 0, pos), world, pos);
    }
  }

  @Override
  public void setStack(ItemStack newStack) {
    setStack(newStack, false);
  }


  /** Checks if this stack exposes NeoForge's item fluid transfer capability. */
  private static boolean hasFluidHandler(ItemStack stack) {
    if (stack.isEmpty()) {
      return false;
    }
    return ItemAccess.forStack(stack).oneByOne().getCapability(Capabilities.Fluid.ITEM) != null;
  }

  /** Gets a legacy fluid handler adapter for the item stack. */
  private static IFluidHandlerItem getFluidHandler(ItemStack stack) {
    if (stack.isEmpty()) {
      return new EmptyFluidHandlerItem(stack);
    }
    ItemAccess access = ItemAccess.forStack(stack).oneByOne();
    ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
    return handler == null ? new EmptyFluidHandlerItem(stack) : new FluidResourceHandlerItemAdapter(handler, access);
  }
  /** Gets the fluid handler for the item */
  private IFluidHandlerItem getItemTank() {
    if (itemTank == null) {
      ItemStack stack = getStack();
      itemTank = getFluidHandler(stack);
    }
    return itemTank;
  }

  @Override
  public int getTanks() {
    return getItemTank().getTanks();
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    return getItemTank().getFluidInTank(tank);
  }

  @Override
  public int getTankCapacity(int tank) {
    return getItemTank().getTankCapacity(tank);
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return getItemTank().isFluidValid(tank, stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    IFluidHandlerItem tank = getItemTank();
    int filled = tank.fill(resource, action);
    // if something happened, force a sync of the item stack
    // hopefully it's the same instance, but we still need a client sync likely
    if (filled > 0 && action.execute()) {
      setStack(tank.getContainer(), true);
    }
    return filled;
  }

  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    IFluidHandlerItem tank = getItemTank();
    FluidStack drained = tank.drain(resource, action);
    if (!drained.isEmpty() && action.execute()) {
      setStack(tank.getContainer(), true);
    }
    return drained;
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    IFluidHandlerItem tank = getItemTank();
    FluidStack drained = tank.drain(maxDrain, action);
    if (!drained.isEmpty() && action.execute()) {
      setStack(tank.getContainer(), true);
    }
    return drained;
  }
}
