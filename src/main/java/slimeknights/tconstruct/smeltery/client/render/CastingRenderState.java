package slimeknights.tconstruct.smeltery.client.render;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class CastingRenderState extends BlockEntityRenderState {
  public BlockState blockState;
  public FluidStack fluidStack = FluidStack.EMPTY;
  public int capacity;
  public ItemStack input = ItemStack.EMPTY;
  public ItemStack output = ItemStack.EMPTY;
  public ItemStack recipeOutput = ItemStack.EMPTY;
  public int timer, totalTime;
  public boolean hasFluids, hasItems;
  public final ItemStackRenderState inputItemState = new ItemStackRenderState();
  public final ItemStackRenderState outputItemState = new ItemStackRenderState();
}
