package slimeknights.tconstruct.world.worldgen.trees;

import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.block.FoliageType;

public class SlimeTree {

  private final TreeGrower treeGrower;

  public SlimeTree(FoliageType foliageType) {
    this.treeGrower = switch (foliageType) {
      case EARTH -> new TreeGrower("earth_slime", Optional.empty(), Optional.of(TinkerStructures.earthSlimeTree), Optional.empty());
      case SKY -> new TreeGrower("sky_slime", Optional.empty(), Optional.of(TinkerStructures.skySlimeTree), Optional.empty());
      case ENDER -> new TreeGrower("ender_slime", Optional.empty(), Optional.of(TinkerStructures.enderSlimeTreeTall), Optional.empty());
      case BLOOD -> new TreeGrower("blood_slime", Optional.empty(), Optional.of(TinkerStructures.bloodSlimeFungus), Optional.empty());
      case ICHOR -> new TreeGrower("ichor_slime", Optional.empty(), Optional.of(TinkerStructures.ichorSlimeFungus), Optional.empty());
    };
  }

  public TreeGrower getTreeGrower() {
    return treeGrower;
  }
}
