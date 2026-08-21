package slimeknights.tconstruct.library.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;


/**
 * Fluid handler wrapper that only allows filling
 */
public class FillOnlyFluidHandler implements IFluidHandler {
	private final IFluidHandler parent;
	public FillOnlyFluidHandler(IFluidHandler parent) {
		this.parent = parent;
	}

	@Override
	public int getTanks() {
		return parent.getTanks();
	}

	@Nonnull
	@Override
	public FluidStack getFluidInTank(int tank) {
		return parent.getFluidInTank(tank);
	}

	@Override
	public int getTankCapacity(int tank) {
		return parent.getTankCapacity(tank);
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return false;
	}

	@Override
	public int fill(FluidStack resource, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction action) {
		return parent.fill(resource, action);
	}

	@Nonnull
	@Override
	public FluidStack drain(FluidStack resource, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction action) {
		return FluidStack.EMPTY;
	}

	@Nonnull
	@Override
	public FluidStack drain(int maxDrain, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction action) {
		return FluidStack.EMPTY;
	}
}
