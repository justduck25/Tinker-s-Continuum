package slimeknights.tconstruct.world.block;

import com.mojang.serialization.MapCodec;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.common.TinkerTags;

public class SlimeLeavesBlock extends LeavesBlock {
  @Getter
  private final FoliageType foliageType;
  @Override
  public MapCodec<? extends SlimeLeavesBlock> codec() {
    return simpleCodec(properties -> new SlimeLeavesBlock(properties, this.foliageType));
  }

  public SlimeLeavesBlock(Properties properties, FoliageType foliageType) {
    super(0.01f, properties);
    this.foliageType = foliageType;
  }

  @Override
  protected BlockState updateShape(BlockState stateIn, LevelReader worldIn, ScheduledTickAccess ticks, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
    int i = getDistance(facingState) + 1;
    if (i != 1 || stateIn.getValue(DISTANCE) != i) {
      ticks.scheduleTick(currentPos, this, 1);
    }

    return stateIn;
  }

  @Override
  protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
    worldIn.setBlock(pos, updateDistance(state, worldIn, pos), 3);
  }

  private static BlockState updateDistance(BlockState state, LevelAccessor world, BlockPos pos) {
    int i = 7;

    for (Direction direction : Direction.values()) {
      BlockPos mutableBlockPos = pos.relative(direction);
      i = Math.min(i, getDistance(world.getBlockState(mutableBlockPos)) + 1);
      if (i == 1) {
        break;
      }
    }

    return state.setValue(DISTANCE, i);
  }

  private static int getDistance(BlockState neighbor) {
    if (neighbor.is(TinkerTags.Blocks.SLIMY_LOGS)) {
      return 0;
    } else {
      return neighbor.getBlock() instanceof SlimeLeavesBlock ? neighbor.getValue(DISTANCE) : 7;
    }
  }

  @Override
  protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {}

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return updateDistance(this.defaultBlockState().setValue(PERSISTENT, Boolean.TRUE), context.getLevel(), context.getClickedPos());
  }

// TODO: needed?
//  @Override
//  public boolean canBeReplacedByLeaves(BlockState state, LevelReader world, BlockPos pos) {
//    return this.isAir(state, world, pos) || state.is(BlockTags.LEAVES) || state.is(TinkerTags.Blocks.SLIMY_LEAVES);
//  }
}
