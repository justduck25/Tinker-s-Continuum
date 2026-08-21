package slimeknights.tconstruct.world.client;

import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.world.item.ItemStack;

/**
 * Skull model instance for the sake of making a Slimeskull with a block item
 * Requires {@link net.minecraft.world.inventory.InventoryMenu#BLOCK_ATLAS} as the texture for the skull.
 **/
public class BlockModelSkullRenderer extends SkullModelBase {
  private final ItemStack stack;
  private float yRot = 0;
  private float xRot = 0;

  /** Creates a renderer with an empty model part - rendering requires rework for 1.21.4 model system */
  public BlockModelSkullRenderer(ItemStack stack) {
    super(new ModelPart(List.of(), Map.of()));
    this.stack = stack;
  }

  @Override
  public void setupAnim(SkullModelBase.State state) {
    this.yRot = state.yRot;
    this.xRot = state.xRot;
  }
}
