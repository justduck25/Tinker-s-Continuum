package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.MantleRenderTypes;
import slimeknights.mantle.client.render.RenderItem;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.tank.CastingFluidHandler;

import java.util.List;

public class CastingBlockEntityRenderer implements BlockEntityRenderer<CastingBlockEntity, CastingRenderState> {
  private final ItemModelResolver itemModelResolver;
  public CastingBlockEntityRenderer(Context context) {
    this.itemModelResolver = context.itemModelResolver();
  }

  @Override
  public CastingRenderState createRenderState() {
    return new CastingRenderState();
  }

  @Override
  public void extractRenderState(CastingBlockEntity casting, CastingRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(casting, state, breakProgress);
    state.blockState = casting.getBlockState();
    CastingFluidHandler tank = casting.getTank();
    state.fluidStack = tank.getFluid();
    state.capacity = tank.getCapacity();
    state.input = casting.getItem(0);
    state.output = casting.getItem(1);
    state.recipeOutput = casting.getRecipeOutput();
    state.timer = casting.getTimer();
    state.totalTime = casting.getCoolingTime();
    List<RenderItem> renderItems = RenderItem.STATE_REGISTRY.get(state.blockState, List.of());
    if (!renderItems.isEmpty()) {
      if (state.input.isEmpty()) {
        state.inputItemState.clear();
      } else {
        this.itemModelResolver.updateForTopItem(state.inputItemState, state.input, renderItems.get(0).getTransform(), casting.getLevel(), null, (int)casting.getBlockPos().asLong());
      }
      if (renderItems.size() >= 2) {
        ItemStack output = state.output.isEmpty() && !state.fluidStack.isEmpty() ? state.recipeOutput : state.output;
        if (output.isEmpty()) {
          state.outputItemState.clear();
        } else {
          this.itemModelResolver.updateForTopItem(state.outputItemState, output, renderItems.get(1).getTransform(), casting.getLevel(), null, (int)(casting.getBlockPos().asLong() + 1));
        }
      } else {
        state.outputItemState.clear();
      }
    } else {
      state.inputItemState.clear();
      state.outputItemState.clear();
    }
  }

  @Override
  public void submit(CastingRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(state.blockState, List.of());
    List<RenderItem> renderItems = RenderItem.STATE_REGISTRY.get(state.blockState, List.of());
    if (!fluids.isEmpty() || !renderItems.isEmpty()) {
      boolean isRotated = RenderingHelper.applyRotation(poseStack, state.blockState);

      int fluidOpacity = 0xFF;
      if (state.timer > 0 && state.totalTime > 0) {
        int opacity = (4 * 0xFF) * state.timer / state.totalTime;
        if (opacity > 3 * 0xFF) {
          fluidOpacity = (4 * 0xFF) - opacity;
        }
      }

      if (!state.fluidStack.isEmpty() && state.capacity > 0 && state.fluidStack.getAmount() >= state.capacity) {
        int finalFluidOpacity = fluidOpacity;
        submitNodeCollector.submitCustomGeometry(poseStack, MantleRenderTypes.FLUID, (pose, buffer) -> {
          PoseStack localPoseStack = new PoseStack();
          localPoseStack.last().pose().set(pose.pose());
          localPoseStack.last().normal().set(pose.normal());
          for (FluidCuboid fluid : fluids) {
            RenderUtils.renderTransparentCuboid(localPoseStack, buffer, fluid, state.fluidStack, finalFluidOpacity, state.lightCoords);
          }
        });
      } else if (!state.fluidStack.isEmpty() && state.capacity > 0) {
        submitNodeCollector.submitCustomGeometry(poseStack, MantleRenderTypes.FLUID, (pose, buffer) -> {
          PoseStack localPoseStack = new PoseStack();
          localPoseStack.last().pose().set(pose.pose());
          localPoseStack.last().normal().set(pose.normal());
          for (FluidCuboid fluid : fluids) {
            RenderUtils.renderScaled(localPoseStack, buffer, fluid, state.fluidStack, 0, state.capacity, state.lightCoords, false);
          }
        });
      }

      if (!renderItems.isEmpty()) {
        if (!state.input.isEmpty()) {
          RenderingHelper.renderItem(poseStack, submitNodeCollector, state.inputItemState, renderItems.get(0), state.lightCoords);
        }
        if (renderItems.size() >= 2) {
          ItemStack output = state.output.isEmpty() && !state.fluidStack.isEmpty() ? state.recipeOutput : state.output;
          if (!output.isEmpty()) {
            RenderingHelper.renderItem(poseStack, submitNodeCollector, state.outputItemState, renderItems.get(1), state.lightCoords);
          }
        }
      }

      if (isRotated) {
        poseStack.popPose();
      }
    }
  }
}
