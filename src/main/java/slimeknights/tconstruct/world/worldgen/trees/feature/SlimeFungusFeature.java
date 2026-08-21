package slimeknights.tconstruct.world.worldgen.trees.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import net.minecraft.world.level.levelgen.feature.WeepingVinesFeature;
import slimeknights.tconstruct.world.worldgen.trees.config.SlimeFungusConfig;

public class SlimeFungusFeature extends HugeFungusFeature {
  public SlimeFungusFeature(Codec<HugeFungusConfiguration> codec) {
    super(codec);
  }

  @Override
  public boolean place(FeaturePlaceContext<HugeFungusConfiguration> context) {
    if (!(context.config() instanceof SlimeFungusConfig config)) {
      return super.place(context);
    }
    // must be on the right ground
    WorldGenLevel level = context.level();
    BlockPos pos = context.origin();
    if (!level.getBlockState(pos.below()).is(config.getGroundTag())) {
      return false;
    }
    // ensure not too tall
    RandomSource random = context.random();
    int height = Mth.nextInt(random, 4, 13);
    if (random.nextInt(12) == 0) {
      height *= 2;
    }
    if (!config.planted && pos.getY() + height + 1 >= context.chunkGenerator().getGenDepth()) {
      return false;
    }
    // actual generation
    boolean flag = !config.planted && random.nextFloat() < 0.06F;
    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
    this.placeStem(level, random, config, pos, height, flag);
    this.placeHat(level, random, config, pos, height, flag);
    return true;
  }

  private static boolean isReplaceable(WorldGenLevel level, BlockPos pos, HugeFungusConfiguration config, boolean checkNonReplaceablePlants) {
    if (level.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
      return true;
    }
    return checkNonReplaceablePlants && config.replaceableBlocks.test(level, pos);
  }

  private void placeStem(WorldGenLevel level, RandomSource random, HugeFungusConfiguration config, BlockPos surfaceOrigin, int totalHeight, boolean isHuge) {
    BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
    BlockState stem = config.stemState;
    int stemRadius = isHuge ? 1 : 0;

    for (int dx = -stemRadius; dx <= stemRadius; dx++) {
      for (int dz = -stemRadius; dz <= stemRadius; dz++) {
        boolean cornerOfHugeStem = isHuge && Mth.abs(dx) == stemRadius && Mth.abs(dz) == stemRadius;
        for (int dy = 0; dy < totalHeight; dy++) {
          blockPos.setWithOffset(surfaceOrigin, dx, dy, dz);
          if (isReplaceable(level, blockPos, config, true)) {
            if (config.planted) {
              if (!level.getBlockState(blockPos.below()).isAir()) {
                level.destroyBlock(blockPos, true);
              }
              level.setBlock(blockPos, stem, 3);
            } else if (cornerOfHugeStem) {
              if (random.nextFloat() < 0.1F) {
                this.setBlock(level, blockPos, stem);
              }
            } else {
              this.setBlock(level, blockPos, stem);
            }
          }
        }
      }
    }
  }

  private void placeHat(WorldGenLevel level, RandomSource random, HugeFungusConfiguration config, BlockPos surfaceOrigin, int totalHeight, boolean isHuge) {
    BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
    boolean placeVines = config.hatState.is(Blocks.NETHER_WART_BLOCK);
    int hatHeight = Math.min(random.nextInt(1 + totalHeight / 3) + 5, totalHeight);
    int hatStartY = totalHeight - hatHeight;

    for (int dy = hatStartY; dy <= totalHeight; dy++) {
      int radius = dy < totalHeight - random.nextInt(3) ? 2 : 1;
      if (hatHeight > 8 && dy < hatStartY + 4) {
        radius = 3;
      }
      if (isHuge) {
        radius++;
      }
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          boolean isEdgeX = dx == -radius || dx == radius;
          boolean isEdgeZ = dz == -radius || dz == radius;
          boolean inside = !isEdgeX && !isEdgeZ && dy != totalHeight;
          boolean corner = isEdgeX && isEdgeZ;
          boolean isHatBottom = dy < hatStartY + 3;
          blockPos.setWithOffset(surfaceOrigin, dx, dy, dz);
          if (isReplaceable(level, blockPos, config, false)) {
            if (config.planted && !level.getBlockState(blockPos.below()).isAir()) {
              level.destroyBlock(blockPos, true);
            }
            if (isHatBottom) {
              if (!inside) {
                this.placeHatDropBlock(level, random, blockPos, config.hatState, placeVines);
              }
            } else if (inside) {
              this.placeHatBlock(level, random, config, blockPos, 0.1F, 0.2F, placeVines ? 0.1F : 0.0F);
            } else if (corner) {
              this.placeHatBlock(level, random, config, blockPos, 0.01F, 0.7F, placeVines ? 0.083F : 0.0F);
            } else {
              this.placeHatBlock(level, random, config, blockPos, 5.0E-4F, 0.98F, placeVines ? 0.07F : 0.0F);
            }
          }
        }
      }
    }
  }

  private void placeHatBlock(LevelAccessor level, RandomSource random, HugeFungusConfiguration config, BlockPos.MutableBlockPos blockPos, float decorBlockProbability, float hatBlockProbability, float vinesProbability) {
    if (random.nextFloat() < decorBlockProbability) {
      this.setBlock(level, blockPos, config.decorState);
    } else if (random.nextFloat() < hatBlockProbability) {
      this.setBlock(level, blockPos, config.hatState);
      if (random.nextFloat() < vinesProbability) {
        tryPlaceWeepingVines(blockPos, level, random);
      }
    }
  }

  private void placeHatDropBlock(LevelAccessor level, RandomSource random, BlockPos blockPos, BlockState hatState, boolean placeVines) {
    if (level.getBlockState(blockPos.below()).is(hatState.getBlock())) {
      this.setBlock(level, blockPos, hatState);
    } else if (random.nextFloat() < 0.15) {
      this.setBlock(level, blockPos, hatState);
      if (placeVines && random.nextInt(11) == 0) {
        tryPlaceWeepingVines(blockPos, level, random);
      }
    }
  }

  private static void tryPlaceWeepingVines(BlockPos hatBlockPos, LevelAccessor level, RandomSource random) {
    BlockPos.MutableBlockPos placePos = hatBlockPos.mutable().move(Direction.DOWN);
    if (level.isEmptyBlock(placePos)) {
      int goalVineHeight = Mth.nextInt(random, 1, 5);
      if (random.nextInt(7) == 0) {
        goalVineHeight *= 2;
      }
      WeepingVinesFeature.placeWeepingVinesColumn(level, random, placePos, goalVineHeight, 23, 25);
    }
  }}
