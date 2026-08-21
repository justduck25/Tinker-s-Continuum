package slimeknights.tconstruct.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.common.TinkerTags;

/**
 * Slimy variant of nylium, mostly changes the way it bonemeals
 */
public class SlimeNyliumBlock extends Block implements BonemealableBlock {
  private final FoliageType foliageType;
  public SlimeNyliumBlock(Properties properties, FoliageType foliageType) {
    super(properties);
    this.foliageType = foliageType;
  }

  private static boolean isDarkEnough(BlockState state, LevelReader reader, BlockPos pos) {
    return reader instanceof Level level ? level.getMaxLocalRawBrightness(pos.above()) < 15 : true;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
    if (!isDarkEnough(state, worldIn, pos)) {
      worldIn.setBlockAndUpdate(pos, SlimeGrassBlock.getDirtState(state));
    }
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState state) {
    return worldIn.getBlockState(pos.above()).isAir();
  }

  @Override
  public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
    return true;
  }

  @Override
  public void performBonemeal(ServerLevel world, RandomSource rand, BlockPos pos, BlockState state) {
    SlimeGrassBlock.growGrass(world, rand, pos, TinkerTags.Blocks.SLIMY_NYLIUM, foliageType, true, true);
  }
}
