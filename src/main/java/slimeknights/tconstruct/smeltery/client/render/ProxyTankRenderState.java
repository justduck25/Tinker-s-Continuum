package slimeknights.tconstruct.smeltery.client.render;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class ProxyTankRenderState extends BlockEntityRenderState {
  public BlockState blockState;
  public FluidStack fluidStack = FluidStack.EMPTY;
  public int capacity;
  public final List<ItemEntry> items = new ArrayList<>();

  public static class ItemEntry {
    public final ItemStackRenderState itemState = new ItemStackRenderState();
  }
}
