package slimeknights.tconstruct.smeltery.block.entity.component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.common.multiblock.IMasterLogic;
import slimeknights.tconstruct.common.multiblock.ServantTileEntity;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

/** Mostly extended to make type validaton easier, and the servant base class is not registered */
public class SmelteryComponentBlockEntity extends ServantTileEntity {

  public SmelteryComponentBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.smelteryComponent.get(), pos, state);
  }

  protected SmelteryComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  /**
   * Block method to update neighbors of a smeltery component when a new one is placed
   * @param world  World instance
   * @param pos    Location of new smeltery component
   */
  /** Notifies any nearby linked structure when this component is removed or replaced. */
  public static void notifyMasterOfChangeFromNeighbors(Level world, BlockPos pos, BlockState state) {
    BlockEntity self = ((BlockGetter) world).getBlockEntity(pos);
    if (self instanceof SmelteryComponentBlockEntity component && component.hasMaster()) {
      component.notifyMasterOfChange(pos, state);
    }
    for (Direction direction : Direction.values()) {
      BlockEntity neighbor = ((BlockGetter) world).getBlockEntity(pos.relative(direction));
      if (neighbor instanceof IMasterLogic master) {
        master.notifyChange(pos, state);
      } else if (neighbor instanceof SmelteryComponentBlockEntity component && component.hasMaster()) {
        component.notifyMasterOfChange(pos, state);
      }
    }
  }
  public static void updateNeighbors(Level world, BlockPos pos, BlockState state) {
    for (Direction direction : Direction.values()) {
      // if the neighbor is a master, notify it we exist
      BlockEntity tileEntity = ((BlockGetter) world).getBlockEntity(pos.relative(direction));
      if (tileEntity instanceof IMasterLogic master) {
        master.notifyChange(pos, state);
        break;
        // if the neighbor is a servant, notify its master we exist
      } else if (tileEntity instanceof SmelteryComponentBlockEntity component && component.hasMaster()) {
        component.notifyMasterOfChange(pos, state);
        break;
      }
    }
  }
}
