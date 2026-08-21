package slimeknights.tconstruct.gadgets.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.gadgets.entity.FancyItemFrameEntity;
import slimeknights.tconstruct.gadgets.entity.FrameType;

public class FancyItemFrameRenderer<T extends FancyItemFrameEntity> extends ItemFrameRenderer<T> {
  private final BlockModelResolver blockModelResolver;
  private final ItemModelResolver itemModelResolver;
  private final MapRenderer mapRenderer;

  public FancyItemFrameRenderer(EntityRendererProvider.Context context) {
    super(context);
    this.blockModelResolver = context.getBlockModelResolver();
    this.itemModelResolver = context.getItemModelResolver();
    this.mapRenderer = context.getMapRenderer();
  }

  @Override
  public FancyItemFrameRenderState createRenderState() {
    return new FancyItemFrameRenderState();
  }

  @Override
  public void extractRenderState(T frame, ItemFrameRenderState state, float partialTicks) {
    super.extractRenderState(frame, state, partialTicks);
    FancyItemFrameRenderState fancyState = (FancyItemFrameRenderState)state;
    fancyState.frameType = frame.getFrameType();
    fancyState.framedStack = frame.getItem().copy();
    fancyState.framedLevel = frame.level();

    boolean isMap = fancyState.framedStack.getItem() instanceof MapItem;
    this.blockModelResolver.update(
      fancyState.frameModel,
      (isMap ? TinkerGadgets.itemFrameMapModel : TinkerGadgets.itemFrameModel).get(fancyState.frameType).defaultBlockState(),
      ItemFrameRenderer.BLOCK_DISPLAY_CONTEXT
    );
    this.itemModelResolver.updateForNonLiving(fancyState.framedItemModel, fancyState.framedStack, net.minecraft.world.item.ItemDisplayContext.FIXED, frame);

    if (fancyState.frameType == FrameType.MANYULLYN) {
      int blockLight = (state.lightCoords >> 4) & 0xF;
      int skyLight = (state.lightCoords >> 20) & 0xF;
      state.lightCoords = (Math.max(7, blockLight) << 4) | (skyLight << 20);
    }

    state.item.clear();
  }

  @Override
  public void submit(ItemFrameRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    FancyItemFrameRenderState fancyState = (FancyItemFrameRenderState)state;

    poseStack.pushPose();
    Direction facing = state.direction;
    Vec3 offset = this.getRenderOffset(state);
    poseStack.translate(-offset.x(), -offset.y(), -offset.z());
    poseStack.translate(facing.getStepX() * 0.46875D, facing.getStepY() * 0.46875D, facing.getStepZ() * 0.46875D);

    float xRot;
    float yRot;
    if (facing.getAxis().isHorizontal()) {
      xRot = 0.0F;
      yRot = 180.0F - facing.toYRot();
    } else {
      xRot = -90 * facing.getAxisDirection().getStep();
      yRot = 180.0F;
    }
    poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
    poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

    boolean frameVisible = !state.isInvisible && (fancyState.frameType != FrameType.CLEAR || fancyState.framedStack.isEmpty());
    if (frameVisible) {
      poseStack.pushPose();
      poseStack.translate(-0.5D, -0.5D, -0.5D);
      fancyState.frameModel.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
      poseStack.popPose();
    }

    if (!fancyState.framedStack.isEmpty()) {
      poseStack.translate(0.0D, 0.0D, 0.4375D);
      if (fancyState.framedStack.getItem() instanceof MapItem) {
        MapItemSavedData mapData = MapItem.getSavedData(fancyState.framedStack, fancyState.framedLevel);
        if (mapData != null && state.mapId != null) {
          int rotation = fancyState.frameType.hasMoreRotations() ? (state.rotation + 2) % 4 * 4 : (state.rotation + 2) % 4 * 2;
          poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 360.0F / (fancyState.frameType.hasMoreRotations() ? 16.0F : 8.0F)));
          poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
          poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
          poseStack.translate(-64.0D, -64.0D, -1.0D);
          int light = fancyState.frameType == FrameType.MANYULLYN ? 0x00F000F0 : state.lightCoords;
          this.mapRenderer.render(state.mapRenderState, poseStack, submitNodeCollector, true, light);
        }
      } else {
        if (fancyState.frameType.hasMoreRotations()) {
          poseStack.mulPose(Axis.ZP.rotationDegrees(state.rotation * 360.0F / 16.0F));
        } else {
          poseStack.mulPose(Axis.ZP.rotationDegrees(state.rotation * 360.0F / 8.0F));
        }
        float scale = fancyState.frameType == FrameType.CLEAR ? 0.75F : 0.5F;
        poseStack.scale(scale, scale, scale);
        int light = fancyState.frameType == FrameType.MANYULLYN ? 0x00F000F0 : state.lightCoords;
        fancyState.framedItemModel.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
      }
    }

    poseStack.popPose();
  }

  public static class FancyItemFrameRenderState extends ItemFrameRenderState {
    FrameType frameType = FrameType.CLEAR;
    ItemStack framedStack = ItemStack.EMPTY;
    @Nullable Level framedLevel;
    final net.minecraft.client.renderer.block.BlockModelRenderState frameModel = new net.minecraft.client.renderer.block.BlockModelRenderState();
    final net.minecraft.client.renderer.item.ItemStackRenderState framedItemModel = new net.minecraft.client.renderer.item.ItemStackRenderState();
  }
}