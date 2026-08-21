package slimeknights.tconstruct.library.tools.part.block;

import lombok.Getter;
import net.minecraft.world.level.block.Block;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.part.IToolPart;


/** Implementation of {@link ToolPartItem} for {@link net.minecraft.world.item.BlockItem}. */
public class ToolPartBlockItem extends MaterialBlockItem implements IToolPart {
  @Getter
  public final MaterialStatsId statType;
  public ToolPartBlockItem(Block block, Properties properties, MaterialStatsId statType) {
    super(block, properties);
    this.statType = statType;
  }

}
