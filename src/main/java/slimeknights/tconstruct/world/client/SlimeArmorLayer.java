package slimeknights.tconstruct.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.util.Util;

import java.util.function.Function;

public class SlimeArmorLayer<S extends SlimeRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
  private final boolean lavaSlime;
  private final Function<SkullBlock.Type, net.minecraft.client.model.object.skull.SkullModelBase> skullModels;

  public SlimeArmorLayer(RenderLayerParent<S, M> pRenderer, EntityModelSet modelSet, boolean lavaSlime) {
    super(pRenderer);
    this.lavaSlime = lavaSlime;
    this.skullModels = Util.memoize(type -> SkullBlockRenderer.createModel(modelSet, type));
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {
    if (state.headItem.isEmpty() && state.wornHeadType == null) return;
    poseStack.pushPose();
    if (lavaSlime) {
      float squish = state.squish;
      if (squish < 0) squish = 0;
      poseStack.translate(0, 1.5 - 0.425 * squish, 0);
    } else {
      poseStack.translate(0, 1.5, 0);
    }
    poseStack.scale(0.9f, 0.9f, 0.9f);
    this.getParentModel().root().translateAndRotate(poseStack);
    if (state.wornHeadType != null) {
      poseStack.translate(0.0F, -1.5F, 0.0F);
      poseStack.scale(1.1875F, 1.1875F, 1.1875F);
      SkullBlock.Type type = state.wornHeadType;
      SkullBlockRenderer.submitSkull(state.wornHeadAnimationPos, poseStack, submitNodeCollector, lightCoords, skullModels.apply(type), SkullBlockRenderer.getSkullRenderType(type, null), state.outlineColor, null);
    } else {
      poseStack.translate(0.0F, -1.0F, 0.0F);
      CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
      state.headItem.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    }
    poseStack.popPose();
  }
}
