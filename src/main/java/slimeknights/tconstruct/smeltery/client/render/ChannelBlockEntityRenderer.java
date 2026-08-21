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
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.client.render.ChannelFluids;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.mantle.client.render.MantleRenderTypes;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.smeltery.block.ChannelBlock;
import slimeknights.tconstruct.smeltery.block.ChannelBlock.ChannelConnection;
import slimeknights.tconstruct.smeltery.block.entity.ChannelBlockEntity;

public class ChannelBlockEntityRenderer implements BlockEntityRenderer<ChannelBlockEntity, ChannelRenderState> {
  public ChannelBlockEntityRenderer(Context context) {}

  @Override
  public ChannelRenderState createRenderState() {
    return new ChannelRenderState();
  }

  @Override
  public void extractRenderState(ChannelBlockEntity te, ChannelRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(te, state, breakProgress);
    state.blockState = te.getBlockState();
    state.fluidStack = te.getFluid();
    state.down = te.getBlockState().getValue(ChannelBlock.DOWN);
    int i = 0;
    for (Direction direction : Plane.HORIZONTAL) {
      state.flowing[i++] = te.isFlowing(direction);
    }
    state.flowing[4] = te.isFlowing(Direction.DOWN);
    state.level = te.getLevel();
  }

  @Override
  public void submit(ChannelRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (state.fluidStack.isEmpty()) return;
    if (state.level == null) return;
    ChannelFluids model = ChannelFluids.REGISTRY.get(state.blockState.getBlock());
    if (model == null) return;

    FluidType fluidType = state.fluidStack.getFluid().getFluidType();
    TextureAtlasSprite still = FluidRenderer.getBlockSprite(RenderUtils.getStillTexture(state.fluidStack, fluidType));
    TextureAtlasSprite flowing = FluidRenderer.getBlockSprite(RenderUtils.getFlowingTexture(state.fluidStack, fluidType));
    int color = RenderUtils.getFluidColor(state.fluidStack, fluidType);
    int light = FluidRenderer.withBlockLight(state.lightCoords, fluidType.getLightLevel(state.fluidStack));

    Direction[] centerFlow = {Direction.UP};
    boolean[] flowStates = state.flowing;

    submitNodeCollector.submitCustomGeometry(poseStack, MantleRenderTypes.FLUID, (pose, buffer) -> {
      PoseStack localPoseStack = new PoseStack();
      localPoseStack.last().pose().set(pose.pose());
      localPoseStack.last().normal().set(pose.normal());
      for (int idx = 0; idx < 4; idx++) {
        Direction direction = Direction.Plane.HORIZONTAL.stream().toList().get(idx);
        ChannelConnection connection = state.blockState.getValue(ChannelBlock.DIRECTION_MAP.get(direction));
        if (connection.canFlow()) {
          boolean isRotated = RenderingHelper.applyRotation(localPoseStack, direction);
          FluidCuboid cube;
          if (flowStates[idx]) {
            cube = model.side().flow(connection == ChannelConnection.OUT);
            if (connection == ChannelConnection.OUT) {
              if (centerFlow[0] == Direction.UP) {
                centerFlow[0] = direction;
              } else if (centerFlow[0] != direction) {
                centerFlow[0] = Direction.DOWN;
              }
            }
            if (!state.level.getBlockState(state.blockPos.relative(direction)).is(state.blockState.getBlock())) {
              FluidRenderer.renderCuboid(localPoseStack, buffer, model.side().edge(), 0, still, flowing, color, light, false);
            }
          } else {
            cube = model.side().still();
          }
          FluidRenderer.renderCuboid(localPoseStack, buffer, cube, 0, still, flowing, color, light, false);
          if (isRotated) localPoseStack.popPose();
        }
      }

      boolean isRotated = false;
      FluidCuboid cube;
      if (centerFlow[0].getAxis().isVertical()) {
        cube = model.center(false);
      } else {
        cube = model.center(true);
        isRotated = RenderingHelper.applyRotation(localPoseStack, centerFlow[0]);
      }
      FluidRenderer.renderCuboid(localPoseStack, buffer, cube, 0, still, flowing, color, light, false);
      if (isRotated) localPoseStack.popPose();

      if (state.down && flowStates[4]) {
        cube = model.down();
        FluidRenderer.renderCuboid(localPoseStack, buffer, cube, 0, still, flowing, color, light, false);
        RenderingHelper.renderFaucetFluids(state.level, state.blockPos, Direction.DOWN, localPoseStack, buffer, still, flowing, color, light);
      }
    });
  }
}
