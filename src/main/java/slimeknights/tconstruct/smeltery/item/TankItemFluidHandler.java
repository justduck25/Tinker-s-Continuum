package slimeknights.tconstruct.smeltery.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;

import java.util.Objects;

/** Transfer handler that stores tank item fluid in item data components. */
public class TankItemFluidHandler extends ItemAccessResourceHandler<FluidResource> {
  private final TankItem tankItem;

  public TankItemFluidHandler(TankItem tankItem, ItemAccess itemAccess) {
    super(itemAccess, 1);
    this.tankItem = tankItem;
  }

  private FluidTank getTank(ItemResource accessResource) {
    return tankItem.getTank(accessResource.toStack());
  }

  @Override
  protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
    Objects.checkIndex(index, size());
    FluidStack fluid = getTank(accessResource).getFluid();
    return fluid.isEmpty() ? FluidResource.EMPTY : FluidResource.of(fluid);
  }

  @Override
  protected int getAmountFrom(ItemResource accessResource, int index) {
    Objects.checkIndex(index, size());
    return getTank(accessResource).getFluidAmount();
  }

  @Override
  protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
    Objects.checkIndex(index, size());
    ItemStack stack = accessResource.toStack();
    TankItem.setTank(stack, newAmount == 0 ? FluidStack.EMPTY : newResource.toStack(newAmount));
    return ItemResource.of(stack);
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    Objects.checkIndex(index, size());
    return !resource.isEmpty();
  }

  @Override
  protected int getCapacity(int index, FluidResource resource) {
    Objects.checkIndex(index, size());
    return TankBlockEntity.getCapacity(itemAccess.getResource().getItem());
  }
}