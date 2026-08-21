package slimeknights.tconstruct.tools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.mantle.client.render.MantleRenderTypes;
import slimeknights.tconstruct.tools.entity.FluidEffectProjectile;

import java.util.List;

public class FluidEffectProjectileRenderer extends EntityRenderer<FluidEffectProjectile, FluidEffectProjectileRenderState> {
  private final List<FluidCuboid> fluids;
  public FluidEffectProjectileRenderer(Context context) {
    super(context);
    this.fluids = List.of(
      FluidCuboid.builder().from(-4,  0,  0).to(-2,  2,  2).build(),
      FluidCuboid.builder().from( 0, -4,  0).to( 2, -2,  2).build(),
      FluidCuboid.builder().from( 0,  0, -4).to( 2,  2, -2).build(),
      FluidCuboid.builder().from( 2,  0,  0).to( 4,  2,  2).build(),
      FluidCuboid.builder().from( 0,  0,  0).to( 2,  4,  2).build(),
      FluidCuboid.builder().from( 0,  0,  2).to( 2,  2,  4).build());
  }

  @Override
  public void submit(FluidEffectProjectileRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (state.fluidStack.isEmpty()) return;
    poseStack.pushPose();
    poseStack.translate(0.0D, 0.15F, 0.0D);
    poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
    poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
    submitNodeCollector.submitCustomGeometry(poseStack, MantleRenderTypes.FLUID, (pose, buffer) -> {
      PoseStack localPoseStack = new PoseStack();
      localPoseStack.last().pose().set(pose.pose());
      localPoseStack.last().normal().set(pose.normal());
      FluidRenderer.renderCuboids(localPoseStack, buffer, fluids, state.fluidStack, state.lightCoords);
    });
    poseStack.popPose();
    super.submit(state, poseStack, submitNodeCollector, camera);
  }

  public Identifier getTextureLocation(FluidEffectProjectileRenderState state) {
    return TextureAtlas.LOCATION_BLOCKS;
  }

  @Override
  public FluidEffectProjectileRenderState createRenderState() {
    return new FluidEffectProjectileRenderState();
  }

  @Override
  public void extractRenderState(FluidEffectProjectile entity, FluidEffectProjectileRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.fluidStack = entity.getFluid();
    state.yRot = entity.getYRot(partialTicks);
    state.xRot = entity.getXRot(partialTicks);
  }
}
