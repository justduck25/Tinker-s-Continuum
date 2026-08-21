package slimeknights.tconstruct.smeltery.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import slimeknights.tconstruct.library.recipe.FluidValues;

import java.util.Objects;

/** Transfer handler instance for the copper can item. */
public class CopperCanFluidHandler extends ItemAccessResourceHandler<FluidResource> {
  public CopperCanFluidHandler(ItemAccess itemAccess) {
    super(itemAccess, 1);
  }

  private FluidStack getFluidStack(ItemResource accessResource) {
    ItemStack stack = accessResource.toStack();
    Fluid fluid = CopperCanItem.getFluid(stack);
    if (fluid == Fluids.EMPTY) {
      return FluidStack.EMPTY;
    }
    FluidStack fluidStack = new FluidStack(fluid, FluidValues.INGOT);
    CompoundTag tag = CopperCanItem.getFluidTag(stack);
    if (tag != null) {
      fluidStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    return fluidStack;
  }

  @Override
  protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
    Objects.checkIndex(index, size());
    FluidStack fluid = getFluidStack(accessResource);
    return fluid.isEmpty() ? FluidResource.EMPTY : FluidResource.of(fluid);
  }

  @Override
  protected int getAmountFrom(ItemResource accessResource, int index) {
    Objects.checkIndex(index, size());
    return getFluidStack(accessResource).isEmpty() ? 0 : FluidValues.INGOT;
  }

  @Override
  protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
    Objects.checkIndex(index, size());
    ItemStack stack = accessResource.toStack();
    if (newAmount == 0) {
      CopperCanItem.removeFluid(stack);
      return ItemResource.of(stack);
    }
    if (newAmount != FluidValues.INGOT) {
      return ItemResource.EMPTY;
    }
    CopperCanItem.setFluid(stack, newResource.toStack(newAmount));
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
    return FluidValues.INGOT;
  }
}