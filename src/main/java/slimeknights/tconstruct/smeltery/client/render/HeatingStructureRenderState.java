package slimeknights.tconstruct.smeltery.client.render;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class HeatingStructureRenderState extends BlockEntityRenderState {
  public BlockState blockState;
  public boolean structureValid;
  @Nullable
  public BlockPos errorPos;
  public boolean highlightError;
  public boolean showDebug;
  public BlockPos minPos = BlockPos.ZERO;
  public BlockPos maxPos = BlockPos.ZERO;
  public int tankCapacity;
  public final List<FluidStack> fluids = new ArrayList<>();
  public final List<ItemEntry> items = new ArrayList<>();

  public static class ItemEntry {
    public final ItemStackRenderState itemState = new ItemStackRenderState();
    public int slotIndex;
  }
}
