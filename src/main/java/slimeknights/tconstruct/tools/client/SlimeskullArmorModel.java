package slimeknights.tconstruct.tools.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.skull.PiglinHeadModel;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import slimeknights.mantle.data.listener.ISafeManagerReloadListener;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.MaterialIdNBT;
import slimeknights.tconstruct.library.utils.SimpleCache;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.item.SlimeskullItem;
import slimeknights.tconstruct.world.client.BlockModelSkullRenderer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/** Renders the skull under a slimeskull helmet on the 26.1 equipment pipeline. */
public final class SlimeskullArmorModel {
  public static final SlimeskullArmorModel INSTANCE = new SlimeskullArmorModel();
  static final SimpleCache<MaterialVariantId,Integer> MATERIAL_COLOR_CACHE = new SimpleCache<>(mat ->
    MaterialRenderInfoLoader.INSTANCE.getRenderInfo(mat)
      .map(MaterialRenderInfo::vertexColor)
      .orElse(-1));
  public static final ISafeManagerReloadListener RELOAD_LISTENER = manager -> {
    HEAD_MODELS = null;
    MATERIAL_COLOR_CACHE.clear();
  };

  private SlimeskullArmorModel() {}

  /** Renders the inner skull while the parent humanoid model still has its posed head. */
  static void submitSkull(PoseStack poseStack, SubmitNodeCollector collector, HumanoidModel<?> humanoidModel, HumanoidRenderState humanoidState, int light) {
    ItemStack helmet = humanoidState.headEquipment;
    if (helmet.isEmpty() || !(helmet.getItem() instanceof SlimeskullItem)) {
      return;
    }
    MaterialId materialId = MaterialIdNBT.from(helmet).getMaterial(0).getMaterialId();
    if (materialId.equals(MaterialId.UNKNOWN)) {
      return;
    }
    SkullModelBase skull = getHeadModel(materialId);
    Identifier texture = HEAD_TEXTURES.get(materialId);
    if (skull == null || texture == null) {
      return;
    }
    int headColor = -1;
    ModifierId dyed = TinkerModifiers.dyed.getId();
    if (ModifierUtil.getModifierLevel(helmet, dyed) > 0) {
      headColor = 0xFF000000 | ModifierUtil.getPersistentInt(helmet, dyed.getId(), -1);
    } else {
      MaterialVariantId slime = MaterialIdNBT.from(helmet).getMaterial(1);
      if (!MaterialId.UNKNOWN.equals(slime.getMaterialId())) {
        headColor = MATERIAL_COLOR_CACHE.apply(slime);
      }
    }
    poseStack.pushPose();
    if (humanoidState.isCrouching) {
      poseStack.translate(0, humanoidModel.head.y / 16.0F, 0);
    }
    if (humanoidState.isBaby) {
      poseStack.scale(0.85F, 0.85F, 0.85F);
      poseStack.translate(0.0D, 0.9D, 0.0D);
    } else {
      poseStack.scale(1.115f, 1.115f, 1.115f);
    }
    // Match 1.20: rotate from the head pivot instead of translating a reset pose at the entity origin.
    poseStack.mulPose(new Quaternionf().rotationZYX(0, humanoidModel.head.yRot, humanoidModel.head.xRot));
    SkullModelBase.State modelState = new SkullModelBase.State();
    modelState.animationPos = humanoidState.walkAnimationPos;
    RenderType renderType = RenderTypes.entityCutoutZOffset(texture);
    int packedColor = headColor == -1 ? 0xFFFFFFFF : headColor;
    int overlay = humanoidState.hasRedOverlay ? OverlayTexture.pack(0, 10) : OverlayTexture.NO_OVERLAY;
    collector.submitModel(skull, modelState, poseStack, renderType, light, overlay, packedColor, null, humanoidState.outlineColor, null);
    if (helmet.hasFoil()) {
      collector.submitModel(skull, modelState, poseStack, RenderTypes.armorEntityGlint(), light, overlay, packedColor, null, humanoidState.outlineColor, null);
    }
    poseStack.popPose();
  }

  private static final Map<MaterialId,Function<EntityModelSet,? extends SkullModelBase>> HEAD_MODEL_FACTORIES = new HashMap<>();
  private static final Map<MaterialId,Identifier> HEAD_TEXTURES = new HashMap<>();

  public static void registerHeadModel(MaterialId materialId, ModelLayerLocation headModel, Identifier texture) {
    registerHeadModel(materialId, modelSet -> new SkullModel(modelSet.bakeLayer(headModel)), texture);
  }

  public static void registerPiglinHeadModel(MaterialId materialId, ModelLayerLocation headModel, Identifier texture) {
    registerHeadModel(materialId, modelSet -> new PiglinHeadModel(modelSet.bakeLayer(headModel)), texture);
  }

  public static void registerBlockModel(MaterialId materialId, ItemStack stack) {
    registerHeadModel(materialId, modelSet -> new BlockModelSkullRenderer(stack), TextureAtlas.LOCATION_BLOCKS);
  }

  public static void registerHeadModel(MaterialId materialId, Function<EntityModelSet,? extends SkullModelBase> headFunction, Identifier texture) {
    if (HEAD_MODEL_FACTORIES.containsKey(materialId)) {
      throw new IllegalArgumentException("Duplicate head model " + materialId);
    }
    HEAD_MODEL_FACTORIES.put(materialId, headFunction);
    HEAD_TEXTURES.put(materialId, texture);
  }

  @Nullable
  private static Map<MaterialId, SkullModelBase> HEAD_MODELS;

  @Nullable
  private static SkullModelBase getHeadModel(MaterialId materialId) {
    if (HEAD_MODELS == null) {
      EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
      ImmutableMap.Builder<MaterialId,SkullModelBase> models = ImmutableMap.builder();
      for (Entry<MaterialId,Function<EntityModelSet,? extends SkullModelBase>> entry : HEAD_MODEL_FACTORIES.entrySet()) {
        models.put(entry.getKey(), entry.getValue().apply(modelSet));
      }
      HEAD_MODELS = models.build();
    }
    return HEAD_MODELS.get(materialId);
  }
}
