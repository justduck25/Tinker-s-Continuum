package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.MantleRenderTypes;
import slimeknights.mantle.client.render.RenderItem;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.smeltery.block.entity.ProxyTankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.tank.ProxyItemTank;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class ProxyTankBlockEntityRenderer implements BlockEntityRenderer<ProxyTankBlockEntity, ProxyTankRenderState> {
  private final ItemModelResolver itemModelResolver;
  public ProxyTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    this.itemModelResolver = context.itemModelResolver();
  }

  @Override
  public ProxyTankRenderState createRenderState() {
    return new ProxyTankRenderState();
  }

  @Override
  public void extractRenderState(ProxyTankBlockEntity proxyTank, ProxyTankRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(proxyTank, state, breakProgress);
    state.blockState = proxyTank.getBlockState();
    ProxyItemTank<?> itemTank = proxyTank.getItemTank();
    state.fluidStack = itemTank.getFluidInTank(0);
    state.capacity = itemTank.getTankCapacity(0);
    state.items.clear();
    for (int i = 0; i < itemTank.getSlots(); i++) {
      ProxyTankRenderState.ItemEntry entry = new ProxyTankRenderState.ItemEntry();
      this.itemModelResolver.updateForTopItem(entry.itemState, itemTank.getStackInSlot(i), ItemDisplayContext.NONE, proxyTank.getLevel(), null, 0);
      state.items.add(entry);
    }
  }

  @Override
  public void submit(ProxyTankRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    List<FluidCuboid> fluids = Config.CLIENT.tankFluidModel.get() ? List.of() : FluidCuboid.REGISTRY.get(state.blockState, List.of());
    List<RenderItem> renderItems = RenderItem.STATE_REGISTRY.get(state.blockState, List.of());
    if (!fluids.isEmpty() || !renderItems.isEmpty()) {
      boolean isRotated = RenderingHelper.applyRotation(poseStack, state.blockState.getValue(HORIZONTAL_FACING));

      if (!fluids.isEmpty()) {
        PoseStack renderPose = RenderUtils.copyPose(poseStack);
        submitNodeCollector.submitCustomGeometry(renderPose, MantleRenderTypes.FLUID, (pose, buffer) -> {
          PoseStack localPoseStack = new PoseStack();
          localPoseStack.last().pose().set(pose.pose());
          localPoseStack.last().normal().set(pose.normal());
          for (FluidCuboid cube : fluids) {
            RenderUtils.renderScaled(localPoseStack, buffer, cube, state.fluidStack, 0, state.capacity, state.lightCoords, true);
          }
        });
      }

      for (int i = 0; i < Math.min(renderItems.size(), state.items.size()); i++) {
        RenderingHelper.renderItem(poseStack, submitNodeCollector, state.items.get(i).itemState, renderItems.get(i), state.lightCoords);
      }

      if (isRotated) {
        poseStack.popPose();
      }
    }
  }
}
