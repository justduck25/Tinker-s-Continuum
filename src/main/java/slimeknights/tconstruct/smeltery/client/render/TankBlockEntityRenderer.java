package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.MantleRenderTypes;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;

import java.util.List;

public class TankBlockEntityRenderer<T extends BlockEntity & ITankBlockEntity> implements BlockEntityRenderer<T, TankRenderState> {
  public TankBlockEntityRenderer(Context context) {}

  @Override
  public TankRenderState createRenderState() {
    return new TankRenderState();
  }

  @Override
  public void extractRenderState(T tile, TankRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(tile, state, breakProgress);
    state.blockState = tile.getBlockState();
    FluidTankAnimated tank = tile.getTank();
    state.fluidStack = tank.getFluid();
    state.capacity = tank.getCapacity();
    float offset = tank.getRenderOffset();
    if (offset > 1.2f || offset < -1.2f) {
      offset = offset - ((offset / 12f + 0.1f) * partialTicks);
      tank.setRenderOffset(offset);
      state.renderOffset = offset;
    } else {
      tank.setRenderOffset(0);
      state.renderOffset = 0;
    }
  }

  @Override
  public void submit(TankRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (Config.CLIENT.tankFluidModel.get()) return;
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(state.blockState, List.of());
    if (!fluids.isEmpty()) {
      PoseStack renderPose = RenderUtils.copyPose(poseStack);
        submitNodeCollector.submitCustomGeometry(renderPose, MantleRenderTypes.FLUID, (pose, buffer) -> {
        PoseStack localPoseStack = new PoseStack();
        localPoseStack.last().pose().set(pose.pose());
        localPoseStack.last().normal().set(pose.normal());
        for (FluidCuboid fluid : fluids) {
          RenderUtils.renderScaled(localPoseStack, buffer, fluid, state.fluidStack, state.renderOffset, state.capacity, state.lightCoords, true);
        }
      });
    }
  }
}
