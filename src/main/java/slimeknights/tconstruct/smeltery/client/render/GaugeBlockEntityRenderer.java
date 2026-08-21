package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.mantle.client.render.MantleRenderTypes;
import slimeknights.tconstruct.smeltery.block.entity.GaugeBlockEntity;

import java.util.List;

public class GaugeBlockEntityRenderer implements BlockEntityRenderer<GaugeBlockEntity, GaugeRenderState> {
  public GaugeBlockEntityRenderer(Context context) {}

  @Override
  public GaugeRenderState createRenderState() {
    return new GaugeRenderState();
  }

  @Override
  public void extractRenderState(GaugeBlockEntity tile, GaugeRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(tile, state, breakProgress);
    state.blockState = tile.getBlockState();
    IFluidHandler tank = tile.getTank();
    if (tank.getTanks() > 0) {
      state.fluidStack = tank.getFluidInTank(0);
    }
  }

  @Override
  public void submit(GaugeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (state.fluidStack.isEmpty()) return;
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(state.blockState, List.of());
    if (!fluids.isEmpty()) {
      submitNodeCollector.submitCustomGeometry(poseStack, MantleRenderTypes.FLUID, (pose, buffer) -> {
        PoseStack localPoseStack = new PoseStack();
        localPoseStack.last().pose().set(pose.pose());
        localPoseStack.last().normal().set(pose.normal());
        FluidRenderer.renderCuboids(localPoseStack, buffer, fluids, state.fluidStack, state.lightCoords);
      });
    }
  }
}
