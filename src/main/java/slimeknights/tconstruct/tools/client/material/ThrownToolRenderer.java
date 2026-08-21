package slimeknights.tconstruct.tools.client.material;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import slimeknights.tconstruct.library.TinkerItemDisplays;
import slimeknights.tconstruct.tools.entity.ThrownTool;
import slimeknights.tconstruct.tools.entity.ToolProjectile;

/** Renderer for {@link ThrownTool} */
public class ThrownToolRenderer<T extends AbstractArrow & ToolProjectile> extends EntityRenderer<T, ThrownToolRenderState> {
  private final ItemModelResolver itemModelResolver;
  public ThrownToolRenderer(Context context) {
    super(context);
    this.itemModelResolver = context.getItemModelResolver();
  }

  @Override
  public void submit(ThrownToolRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90));
    poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 225));
    poseStack.translate(0.2, -0.2, 0);
    state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    poseStack.popPose();
    super.submit(state, poseStack, submitNodeCollector, camera);
  }

  @Override
  public ThrownToolRenderState createRenderState() {
    return new ThrownToolRenderState();
  }

  public Identifier getTextureLocation(ThrownToolRenderState state) {
    return TextureAtlas.LOCATION_BLOCKS;
  }

  @Override
  public void extractRenderState(T entity, ThrownToolRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    this.itemModelResolver.updateForNonLiving(state.item, entity.getDisplayTool(), TinkerItemDisplays.THROWN, entity);
    state.yRot = entity.getYRot(partialTicks);
    state.xRot = entity.getXRot(partialTicks);
  }
}
