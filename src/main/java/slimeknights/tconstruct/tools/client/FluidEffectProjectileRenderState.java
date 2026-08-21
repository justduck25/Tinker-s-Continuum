package slimeknights.tconstruct.tools.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidEffectProjectileRenderState extends EntityRenderState {
  public FluidStack fluidStack = FluidStack.EMPTY;
  public float yRot, xRot;
}
