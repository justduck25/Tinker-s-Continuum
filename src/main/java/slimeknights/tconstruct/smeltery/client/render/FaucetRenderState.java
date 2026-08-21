package slimeknights.tconstruct.smeltery.client.render;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public class FaucetRenderState extends BlockEntityRenderState {
  public BlockState blockState;
  public Level level;
  public FluidStack fluidStack = FluidStack.EMPTY;
  public boolean isPouring;
}
