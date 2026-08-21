package slimeknights.tconstruct.world.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;

/** Recreation of {@link net.minecraft.client.model.object.skull.DragonHeadModel} but adjusted for slimeskulls. */
public class DragonSkullModel extends SkullModelBase {
  private final ModelPart head;
  private final ModelPart jaw;

  public DragonSkullModel(ModelPart root) {
    super(root);
    this.head = root.getChild("head");
    this.jaw = this.head.getChild("jaw");
  }

  @Override
  public void setupAnim(State state) {
    super.setupAnim(state);
    this.jaw.xRot = (float)(Math.sin(state.animationPos * Math.PI * 0.2f) + 1) * 0.2f;
    this.head.yRot = state.yRot * ((float)Math.PI / 180);
    this.head.xRot = state.xRot * ((float)Math.PI / 180);
  }
}