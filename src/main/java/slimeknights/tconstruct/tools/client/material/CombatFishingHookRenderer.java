package slimeknights.tconstruct.tools.client.material;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.utils.SimpleCache;
import slimeknights.tconstruct.tools.entity.CombatFishingHook;

import javax.annotation.Nullable;
import java.util.Optional;

/** Renderer for {@link CombatFishingHook} */
public class CombatFishingHookRenderer extends EntityRenderer<CombatFishingHook, CombatFishingHookRenderState> {
  private static final Identifier LOCAL = TConstruct.getResource("fishing_hook/material");
  private static final Identifier BASE = ArmorTextureSupplier.getTexturePath(LOCAL);

  @Nullable
  private static Identifier tryTexture(String material) {
    Identifier texture = LOCAL.withSuffix(material);
    if (ArmorTextureSupplier.TEXTURE_VALIDATOR.test(texture)) {
      return ArmorTextureSupplier.getTexturePath(texture);
    }
    return null;
  }

  private static final SimpleCache<MaterialVariantId,MaterialTexture> TEXTURE_CACHE = new SimpleCache<>(material -> {
    if (!IMaterial.UNKNOWN_ID.equals(material)) {
      Optional<MaterialRenderInfo> infoOptional = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(material);
      int color = -1;
      int luminosity = 0;
      if (infoOptional.isPresent()) {
        MaterialRenderInfo info = infoOptional.get();
        Identifier untinted = info.texture();
        luminosity = info.luminosity();
        if (untinted != null) {
          Identifier texture = tryTexture('_' + untinted.getNamespace() + '_' + untinted.getPath());
          if (texture != null) {
            return new MaterialTexture(texture, -1, luminosity);
          }
        }
        color = info.vertexColor();
        for (String fallback : info.fallbacks()) {
          Identifier texture = tryTexture('_' + fallback);
          if (texture != null) {
            return new MaterialTexture(texture, color, luminosity);
          }
        }
      }
      return new MaterialTexture(ArmorTextureSupplier.getTexturePath(LOCAL), color, luminosity);
    }
    return MaterialTexture.EMPTY;
  });

  public CombatFishingHookRenderer(Context context) {
    super(context);
  }

  public static void clearCache() {
    TEXTURE_CACHE.clear();
  }

  @Override
  public void submit(CombatFishingHookRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (state.texture == null) return;

    poseStack.pushPose();
    poseStack.pushPose();
    poseStack.scale(0.5F, 0.5F, 0.5F);
    poseStack.mulPose(camera.orientation);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

    int bobberLight = state.lightCoords;
    if (state.luminosity > 0) {
      int blockLight = (state.lightCoords >> 4) & 0xF;
      int skyLight = (state.lightCoords >> 20) & 0xF;
      blockLight = Math.min(blockLight + state.luminosity, 15);
      bobberLight = (blockLight << 4) | (skyLight << 20);
    }

    final int bobberLightFinal = bobberLight;
    Identifier bobberTexture = state.texture;
    int bobberColor = state.color;
    submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(bobberTexture), (pose, buffer) -> {
      vertexColored(buffer, pose, bobberLightFinal, bobberColor, 0f, 0, 0, 1);
      vertexColored(buffer, pose, bobberLightFinal, bobberColor, 1f, 0, 1, 1);
      vertexColored(buffer, pose, bobberLightFinal, bobberColor, 1f, 1, 1, 0);
      vertexColored(buffer, pose, bobberLightFinal, bobberColor, 0f, 1, 0, 0);
    });
    poseStack.popPose();

    float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
    Vec3 offset = state.lineOriginOffset;
    float xa = (float)offset.x;
    float ya = (float)offset.y;
    float za = (float)offset.z;
    submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
      for (int i = 0; i < 16; i++) {
        float a0 = (float)i / 16f;
        float a1 = (float)(i + 1) / 16f;
        stringVertex(xa, ya, za, buffer, pose, a0, a1, width);
        stringVertex(xa, ya, za, buffer, pose, a1, a0, width);
      }
    });
    poseStack.popPose();
    super.submit(state, poseStack, submitNodeCollector, camera);
  }

  public Identifier getTextureLocation(CombatFishingHookRenderState state) {
    return BASE;
  }

  @Override
  public CombatFishingHookRenderState createRenderState() {
    return new CombatFishingHookRenderState();
  }

  @Override
  public void extractRenderState(CombatFishingHook entity, CombatFishingHookRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    Player player = entity.getPlayerOwner();
    if (player == null) return;

    MaterialTexture texture = TEXTURE_CACHE.apply(entity.getMaterial());
    state.texture = texture.texture();
    state.color = texture.color();
    state.luminosity = texture.luminosity();

    HumanoidArm arm = player.getMainArm();
    ItemStack mainHand = player.getMainHandItem();
    state.sideOffset = (arm == HumanoidArm.RIGHT ? 1 : -1);
    if (!mainHand.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST)) {
      state.sideOffset = -state.sideOffset;
    }

    state.attackAnim = player.getAttackAnim(partialTicks);
    state.yBodyRot = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot);
    state.eyeHeightOffset = player.isCrouching() ? -0.1875F : 0.0F;
    state.isFirstPerson = this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player;
    state.playerPos = player.getPosition(partialTicks);

    Vec3 hookPos = entity.getPosition(partialTicks).add(0.0, 0.25, 0.0);
    Vec3 handPos;
    if (state.isFirstPerson) {
      float fov = this.entityRenderDispatcher.options.fov().get().intValue();
      Vec3 point = this.entityRenderDispatcher
        .camera
        .getNearPlane(fov)
        .getPointOnPlane(state.sideOffset * 0.525F, -0.1F)
        .scale(960.0 / fov);
      float swingSin = Mth.sin(Mth.sqrt(state.attackAnim) * (float)Math.PI);
      point = point.yRot(swingSin * 0.5F).xRot(-swingSin * 0.7F);
      handPos = player.getEyePosition(partialTicks).add(point);
    } else {
      float radians = state.yBodyRot * (float)(Math.PI / 180.0);
      double sin = Mth.sin(radians);
      double cos = Mth.cos(radians);
      double scaledSide = state.sideOffset * 0.35;
      float playerScale = player.getScale();
      handPos = player.getEyePosition(partialTicks)
        .add(-cos * scaledSide - sin * 0.8 * playerScale, state.eyeHeightOffset - 0.45 * playerScale, -sin * scaledSide + cos * 0.8 * playerScale);
    }
    state.lineOriginOffset = handPos.subtract(hookPos);
  }

  private static void vertexColored(VertexConsumer builder, PoseStack.Pose pose, int lightCoords, int color, float x, int y, int u, int v) {
    builder.addVertex(pose, x - 0.5F, y - 0.5F, 0.0F)
      .setColor(color)
      .setUv(u, v)
      .setOverlay(OverlayTexture.NO_OVERLAY)
      .setLight(lightCoords)
      .setNormal(pose, 0.0F, 1.0F, 0.0F);
  }

  private static void stringVertex(float xa, float ya, float za, VertexConsumer stringBuffer, PoseStack.Pose stringPose, float aa, float nexta, float width) {
    float x = xa * aa;
    float y = ya * (aa * aa + aa) * 0.5F + 0.25F;
    float z = za * aa;
    float nx = xa * nexta - x;
    float ny = ya * (nexta * nexta + nexta) * 0.5F + 0.25F - y;
    float nz = za * nexta - z;
    float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
    nx /= length;
    ny /= length;
    nz /= length;
    stringBuffer.addVertex(stringPose, x, y, z).setColor(-16777216).setNormal(stringPose, nx, ny, nz).setLineWidth(width);
  }

  private record MaterialTexture(Identifier texture, int color, int luminosity) {
    public static final MaterialTexture EMPTY = new MaterialTexture(BASE, -1, 0);
  }
}
