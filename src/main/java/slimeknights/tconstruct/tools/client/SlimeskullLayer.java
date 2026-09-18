package slimeknights.tconstruct.tools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/** Submits the slimeskull inner head while the humanoid model is still posed. */
public class SlimeskullLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
  public SlimeskullLayer(RenderLayerParent<S, M> parent) {
    super(parent);
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
    SlimeskullArmorModel.submitSkull(poseStack, collector, getParentModel(), state, lightCoords);
  }
}
