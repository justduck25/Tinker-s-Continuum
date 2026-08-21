package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.mantle.client.render.MantleRenderTypes;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;

import java.util.List;

public class FaucetBlockEntityRenderer implements BlockEntityRenderer<FaucetBlockEntity, FaucetRenderState> {
  public FaucetBlockEntityRenderer(Context context) {}

  @Override
  public FaucetRenderState createRenderState() {
    return new FaucetRenderState();
  }

  @Override
  public void extractRenderState(FaucetBlockEntity tile, FaucetRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(tile, state, breakProgress);
    state.blockState = tile.getBlockState();
    state.level = tile.getLevel();
    state.fluidStack = tile.getRenderFluid();
    state.isPouring = tile.isPouring();
  }

  @Override
  public void submit(FaucetRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (!state.isPouring || state.fluidStack.isEmpty()) return;
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(state.blockState, List.of());
    if (fluids.isEmpty()) return;

    Direction direction = state.blockState.getValue(slimeknights.tconstruct.smeltery.block.FaucetBlock.FACING);
    boolean isRotated = RenderingHelper.applyRotation(poseStack, direction);

    FluidType fluidType = state.fluidStack.getFluid().getFluidType();
    final int color = RenderUtils.getFluidColor(state.fluidStack, fluidType);
    final TextureAtlasSprite still = FluidRenderer.getBlockSprite(RenderUtils.getStillTexture(state.fluidStack, fluidType));
    final TextureAtlasSprite flowing = FluidRenderer.getBlockSprite(RenderUtils.getFlowingTexture(state.fluidStack, fluidType));
    final int light = FluidRenderer.withBlockLight(state.lightCoords, fluidType.getLightLevel(state.fluidStack));

    PoseStack renderPose = RenderUtils.copyPose(poseStack);
        submitNodeCollector.submitCustomGeometry(renderPose, MantleRenderTypes.FLUID, (pose, buffer) -> {
      PoseStack localPoseStack = new PoseStack();
      localPoseStack.last().pose().set(pose.pose());
      localPoseStack.last().normal().set(pose.normal());
      for (FluidCuboid cube : fluids) {
        FluidRenderer.renderCuboid(localPoseStack, buffer, cube, 0, still, flowing, color, light, false);
      }
      if (state.level != null) {
        RenderingHelper.renderFaucetFluids(state.level, state.blockPos, direction, localPoseStack, buffer, still, flowing, color, light);
      }
    });

    if (isRotated) {
      poseStack.popPose();
    }
  }
}
