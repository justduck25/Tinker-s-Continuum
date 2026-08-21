package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.client.TinkerRenderTypes;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.library.TinkerItemDisplays;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.MeltingModuleInventory;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;

public class HeatingStructureBlockEntityRenderer implements BlockEntityRenderer<HeatingStructureBlockEntity, HeatingStructureRenderState> {
  private static final float ITEM_SCALE = 15f/16f;
  private final ItemModelResolver itemModelResolver;

  public HeatingStructureBlockEntityRenderer(Context context) {
    this.itemModelResolver = context.itemModelResolver();
  }

  @Override
  public HeatingStructureRenderState createRenderState() {
    return new HeatingStructureRenderState();
  }

  @Override
  public void extractRenderState(HeatingStructureBlockEntity smeltery, HeatingStructureRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(smeltery, state, breakProgress);
    state.blockState = smeltery.getBlockState();
    Level world = smeltery.getLevel();
    if (world == null) return;
    StructureData structure = smeltery.getStructure();
    state.structureValid = smeltery.getBlockState().getValue(ControllerBlock.IN_STRUCTURE) && structure != null;

    state.errorPos = smeltery.getErrorPos();
    state.highlightError = smeltery.isHighlightError();
    state.showDebug = Minecraft.getInstance().player != null && smeltery.showDebugBlockBorder(Minecraft.getInstance().player);

    if (!state.structureValid) return;

    BlockPos pos = smeltery.getBlockPos();
    state.minPos = structure.getMinInside();
    state.maxPos = structure.getMaxInside();

    state.fluids.clear();
    smeltery.getTank().getFluids().forEach(f -> state.fluids.add(f));
    state.tankCapacity = smeltery.getTank().getCapacity();

    int xd = 1 + state.maxPos.getX() - state.minPos.getX();
    int zd = 1 + state.maxPos.getZ() - state.minPos.getZ();
    int layer = xd * zd;
    Direction facing = smeltery.getBlockState().getValue(ControllerBlock.FACING);
    state.items.clear();
    MeltingModuleInventory inventory = smeltery.getMeltingInventory();
    for (int i = 0; i < inventory.getSlots(); i++) {
      if (!inventory.getStackInSlot(i).isEmpty()) {
        int height = i / layer;
        int layerIndex = i % layer;
        int offsetX = layerIndex % xd;
        int offsetZ = layerIndex / xd;
        BlockPos itemPos = state.minPos.offset(offsetX, height, offsetZ);
        HeatingStructureRenderState.ItemEntry entry = new HeatingStructureRenderState.ItemEntry();
        this.itemModelResolver.updateForTopItem(entry.itemState, inventory.getStackInSlot(i), TinkerItemDisplays.MELTER, world, null, i);
        entry.slotIndex = i;
        state.items.add(entry);
      }
    }
  }

  @Override
  public void submit(HeatingStructureRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    Minecraft minecraft = Minecraft.getInstance();
    Level world = minecraft.level;
    if (world == null) return;
    BlockPos pos = state.blockPos;

    if (state.errorPos != null && minecraft.player != null && ((!state.structureValid && state.highlightError) || state.showDebug)) {
      BlockPos playerPos = minecraft.player.blockPosition();
      int dx = playerPos.getX() - pos.getX();
      int dz = playerPos.getZ() - pos.getZ();
      if ((dx * dx + dz * dz) < 512) {
        BlockPos errorPos = state.errorPos;
        RenderType renderType = TinkerRenderTypes.ERROR_BLOCK;
        PoseStack renderPose = RenderUtils.copyPose(poseStack);
        renderPose.translate(errorPos.getX() - pos.getX(), errorPos.getY() - pos.getY(), errorPos.getZ() - pos.getZ());
        submitNodeCollector.submitCustomGeometry(renderPose, renderType, (pose, buffer) -> renderLineBox(pose, buffer, 1f, state.structureValid ? 1f : 0f, 0f, 0.5f));
      }
    }

    if (!state.structureValid) return;

    BlockPos minPos = state.minPos;
    BlockPos maxPos = state.maxPos;

    poseStack.pushPose();
    poseStack.translate(minPos.getX() - pos.getX(), minPos.getY() - pos.getY(), minPos.getZ() - pos.getZ());
    int brightness = 0xF000F0;

    // render tank fluids
    if (!state.fluids.isEmpty()) {
      submitNodeCollector.submitCustomGeometry(poseStack, TinkerRenderTypes.SMELTERY_FLUID, (pose, buffer) -> {
        PoseStack localPoseStack = new PoseStack();
        localPoseStack.last().pose().set(pose.pose());
        localPoseStack.last().normal().set(pose.normal());
        SmelteryTankRenderer.renderFluids(localPoseStack, buffer, state.fluids, state.tankCapacity, minPos, maxPos, brightness);
      });
    }

    // render items
    Direction facing = state.blockState.getValue(ControllerBlock.FACING);
    Quaternionf itemRotation = Axis.YP.rotationDegrees(-90.0F * (float)facing.get2DDataValue());
    int xd = 1 + maxPos.getX() - minPos.getX();
    int zd = 1 + maxPos.getZ() - minPos.getZ();
    int layer = xd * zd;

    int max = Config.CLIENT.maxSmelteryItemQuads.get();
    if (max != 0) {
      int quadsRendered = 0;
      for (HeatingStructureRenderState.ItemEntry entry : state.items) {
        int i = entry.slotIndex;
        int height = i / layer;
        int layerIndex = i % layer;
        int offsetX = layerIndex % xd;
        int offsetZ = layerIndex / xd;
        BlockPos itemPos = minPos.offset(offsetX, height, offsetZ);

        poseStack.pushPose();
        poseStack.translate(offsetX + 0.5f, height + 0.5f, offsetZ + 0.5f);
        poseStack.mulPose(itemRotation);
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        entry.itemState.submit(poseStack, submitNodeCollector, brightness, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        if (max != -1) {
          quadsRendered += 100;
          if (quadsRendered > max) break;
        }
      }
    }

    poseStack.popPose();
  }
  private static void renderLineBox(PoseStack.Pose pose, VertexConsumer buffer, float red, float green, float blue, float alpha) {
    line(buffer, pose, 0, 0, 0, 1, 0, 0, red, green, blue, alpha, 1, 0, 0);
    line(buffer, pose, 0, 0, 1, 1, 0, 1, red, green, blue, alpha, 1, 0, 0);
    line(buffer, pose, 0, 1, 0, 1, 1, 0, red, green, blue, alpha, 1, 0, 0);
    line(buffer, pose, 0, 1, 1, 1, 1, 1, red, green, blue, alpha, 1, 0, 0);
    line(buffer, pose, 0, 0, 0, 0, 1, 0, red, green, blue, alpha, 0, 1, 0);
    line(buffer, pose, 1, 0, 0, 1, 1, 0, red, green, blue, alpha, 0, 1, 0);
    line(buffer, pose, 0, 0, 1, 0, 1, 1, red, green, blue, alpha, 0, 1, 0);
    line(buffer, pose, 1, 0, 1, 1, 1, 1, red, green, blue, alpha, 0, 1, 0);
    line(buffer, pose, 0, 0, 0, 0, 0, 1, red, green, blue, alpha, 0, 0, 1);
    line(buffer, pose, 1, 0, 0, 1, 0, 1, red, green, blue, alpha, 0, 0, 1);
    line(buffer, pose, 0, 1, 0, 0, 1, 1, red, green, blue, alpha, 0, 0, 1);
    line(buffer, pose, 1, 1, 0, 1, 1, 1, red, green, blue, alpha, 0, 0, 1);
  }

  private static void line(VertexConsumer buffer, PoseStack.Pose pose, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha, float nx, float ny, float nz) {
    buffer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha).setNormal(pose, nx, ny, nz).setLineWidth(1.0f);
    buffer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, alpha).setNormal(pose, nx, ny, nz).setLineWidth(1.0f);
  }
}