package slimeknights.tconstruct.fluids.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Objects;

/** Represents a transfer handler for a container with a constant fluid. */
public class ConstantFluidContainerWrapper extends ItemAccessResourceHandler<FluidResource> {
  private final FluidStack fluid;
  private final ItemResource emptyResource;

  public ConstantFluidContainerWrapper(ItemAccess itemAccess, FluidStack fluid, ItemStack emptyStack) {
    super(itemAccess, 1);
    this.fluid = fluid;
    this.emptyResource = ItemResource.of(emptyStack);
  }

  public ConstantFluidContainerWrapper(ItemAccess itemAccess, FluidStack fluid) {
    this(itemAccess, fluid, itemAccess.getResource().getItem().getCraftingRemainder().create());
  }

  @Override
  protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
    Objects.checkIndex(index, size());
    return accessResource.isEmpty() ? FluidResource.EMPTY : FluidResource.of(fluid);
  }

  @Override
  protected int getAmountFrom(ItemResource accessResource, int index) {
    Objects.checkIndex(index, size());
    return accessResource.isEmpty() ? 0 : fluid.getAmount();
  }

  @Override
  protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
    Objects.checkIndex(index, size());
    if (newAmount == 0) {
      return emptyResource;
    }
    return newAmount == fluid.getAmount() && newResource.matches(fluid) ? accessResource : ItemResource.EMPTY;
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    Objects.checkIndex(index, size());
    return resource.matches(fluid);
  }

  @Override
  protected int getCapacity(int index, FluidResource resource) {
    Objects.checkIndex(index, size());
    return fluid.getAmount();
  }
}