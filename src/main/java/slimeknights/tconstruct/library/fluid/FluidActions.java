package slimeknights.tconstruct.library.fluid;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Shared fluid action constants while the port still uses NeoForge's legacy fluid handler API. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("removal")
public class FluidActions {
  public static final IFluidHandler.FluidAction EXECUTE = IFluidHandler.FluidAction.EXECUTE;
  public static final IFluidHandler.FluidAction SIMULATE = IFluidHandler.FluidAction.SIMULATE;
}
