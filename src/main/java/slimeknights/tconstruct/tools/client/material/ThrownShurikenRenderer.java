package slimeknights.tconstruct.tools.client.material;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemDisplayContext;
import slimeknights.tconstruct.tools.entity.ThrownShuriken;
import slimeknights.tconstruct.tools.entity.ToolProjectile;

public class ThrownShurikenRenderer<T extends Projectile & ToolProjectile> extends EntityRenderer<T, ThrownShurikenRenderState> {
  private final ItemModelResolver itemModelResolver;
  public ThrownShurikenRenderer(EntityRendererProvider.Context context) {
    super(context);
    this.itemModelResolver = context.getItemModelResolver();
  }

  @Override
  public void submit(ThrownShurikenRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (state.ageTicks < 2) return;
    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw + 90));
    poseStack.mulPose(Axis.ZP.rotationDegrees((state.ageTicks + state.partialTick) * 30 % 360));
    poseStack.translate(-0.03125, -0.09375, 0);
    state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    poseStack.popPose();
    super.submit(state, poseStack, submitNodeCollector, camera);
  }

  public Identifier getTextureLocation(ThrownShurikenRenderState state) {
    return Identifier.parse("minecraft:textures/atlas/blocks.png");
  }

  @Override
  public ThrownShurikenRenderState createRenderState() {
    return new ThrownShurikenRenderState();
  }

  @Override
  public void extractRenderState(T entity, ThrownShurikenRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    this.itemModelResolver.updateForNonLiving(state.item, entity.getDisplayTool(), ItemDisplayContext.GROUND, entity);
    state.yaw = entity.getYRot(partialTicks);
    state.ageTicks = entity.tickCount;
  }
}
