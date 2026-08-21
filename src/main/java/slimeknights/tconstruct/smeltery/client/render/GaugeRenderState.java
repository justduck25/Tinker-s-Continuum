package slimeknights.tconstruct.smeltery.client.render;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.neoforged.neoforge.fluids.FluidStack;

public class GaugeRenderState extends BlockEntityRenderState {
  public BlockState blockState;
  public FluidStack fluidStack = FluidStack.EMPTY;
}
