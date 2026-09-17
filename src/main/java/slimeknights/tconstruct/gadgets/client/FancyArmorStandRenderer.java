package slimeknights.tconstruct.gadgets.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.entity.FancyArmorStandEntity;
import slimeknights.tconstruct.gadgets.entity.FancyArmorStandEntity.StandType;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** Renderer for fancy armor stands */
public class FancyArmorStandRenderer extends ArmorStandRenderer {
  private static final Map<StandType, Identifier> TEXTURES = new EnumMap<>(StandType.class);
  static {
    Identifier base = TConstruct.getResource("textures/entity/armorstand/");
    for (StandType type : StandType.values()) {
      TEXTURES.put(type, base.withSuffix(type.name().toLowerCase(Locale.ROOT) + ".png"));
    }
  }

  public FancyArmorStandRenderer(Context context) {
    super(context);
  }

  @Override
  public FancyArmorStandRenderState createRenderState() {
    return new FancyArmorStandRenderState();
  }

  @Override
  public void extractRenderState(ArmorStand entity, ArmorStandRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    // Armor and held-item layers special-case vanilla ARMOR_STAND so small stands keep
    // adult equipment assets on the small models instead of HUMANOID_BABY transforms.
    state.entityType = EntityType.ARMOR_STAND;
    state.isBaby = entity.isSmall();
    if (entity instanceof FancyArmorStandEntity stand && state instanceof FancyArmorStandRenderState fancy) {
      fancy.standType = stand.getStandType();
      if (stand.getStandType().isFullbright()) {
        state.lightCoords = 0x00F000F0;
      }
      if (stand.isClearHidden()) {
        state.isInvisible = true;
      }
    }
  }

  @Override
  public Identifier getTextureLocation(ArmorStandRenderState state) {
    if (state instanceof FancyArmorStandRenderState fancy && fancy.standType != null) {
      return TEXTURES.get(fancy.standType);
    }
    return super.getTextureLocation(state);
  }

  @Override
  public void submit(ArmorStandRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    super.submit(state, poseStack, submitNodeCollector, camera);
  }

  public static class FancyArmorStandRenderState extends ArmorStandRenderState {
    public StandType standType = StandType.BAMBOO;
  }
}
