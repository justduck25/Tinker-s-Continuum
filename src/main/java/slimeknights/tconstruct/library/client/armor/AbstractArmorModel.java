package slimeknights.tconstruct.library.client.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier.TextureType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/** Common shared logic for material armor models */
public abstract class AbstractArmorModel extends Model<Unit> {
  /** Base model instance for rendering */
  @Nullable
  protected Model base;
  /** If true, applies the enchantment glint to extra layers */
  protected boolean hasGlint = false;
  /** If true, uses the legs texture */
  protected TextureType textureType = TextureType.ARMOR;

  protected AbstractArmorModel() {
    super(new ModelPart(List.of(), Map.of()), RenderTypes::armorCutoutNoCull);
  }

  /** Sets up the model given the passed arguments */
  protected void setup(ItemStack stack, TextureType type, Model base) {
    this.base = base;
    this.hasGlint = stack.hasFoil();
    this.textureType = type;
  }

  /** Renders a colored model */
  public static void renderColored(Model model, PoseStack matrices, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, int color, float red, float green, float blue, float alpha) {
    if (color != -1) {
      alpha *= (float)(color >> 24 & 255) / 255.0F;
      red *= (float)(color >> 16 & 255) / 255.0F;
      green *= (float)(color >> 8 & 255) / 255.0F;
      blue *= (float)(color & 255) / 255.0F;
    }
    int packedColor = ((int)(alpha * 255) << 24) | ((int)(red * 255) << 16) | ((int)(green * 255) << 8) | (int)(blue * 255);
    model.renderToBuffer(matrices, buffer, packedLightIn, packedOverlayIn, packedColor);
  }

  /** Renders the wings layer */
  protected void renderWings(PoseStack matrices, int packedLightIn, int packedOverlayIn,
                             slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier.ArmorTexture texture,
                             float red, float green, float blue, float alpha, boolean hasGlint) {
    if (buffer != null && base instanceof EntityModel baseModel) {
      ElytraModel wings = getWings();
      copyProperties(baseModel, wings);
      texture.renderTexture(wings, matrices, buffer, packedLightIn, packedOverlayIn,
                            red, green, blue, alpha, hasGlint);
    }
  }
  /* Helpers */

  /** Buffer from the render living event, stored as we lose access to it later */
  @Nullable
  public static MultiBufferSource buffer;

  /** Initializes the wrapper */
  public static void init() {
    // register listeners to set and clear the buffer
    NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (RenderLivingEvent.Pre event) -> buffer = Minecraft.getInstance().renderBuffers().bufferSource());
    NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (RenderLivingEvent.Post event) -> buffer = null);
  }

  /** Wings model to render */
  @Nullable
  private static ElytraModel wingsModel;

  /** Gets or creates the elytra model */
  private static ElytraModel getWings() {
    if (wingsModel == null) {
      wingsModel = new ElytraModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELYTRA));
    }
    return wingsModel;
  }

  /** Handles copying the humanoid model pose to the elytra model. */
  public static void copyProperties(EntityModel base, EntityModel other) {
    if (base instanceof HumanoidModel baseHumanoid && other instanceof HumanoidModel otherHumanoid) {
      copyPart(baseHumanoid.head, otherHumanoid.head);
      copyPart(baseHumanoid.hat, otherHumanoid.hat);
      copyPart(baseHumanoid.body, otherHumanoid.body);
      copyPart(baseHumanoid.rightArm, otherHumanoid.rightArm);
      copyPart(baseHumanoid.leftArm, otherHumanoid.leftArm);
      copyPart(baseHumanoid.rightLeg, otherHumanoid.rightLeg);
      copyPart(baseHumanoid.leftLeg, otherHumanoid.leftLeg);
    }
  }

  private static void copyPart(ModelPart source, ModelPart target) {
    target.x = source.x;
    target.y = source.y;
    target.z = source.z;
    target.xRot = source.xRot;
    target.yRot = source.yRot;
    target.zRot = source.zRot;
    target.xScale = source.xScale;
    target.yScale = source.yScale;
    target.zScale = source.zScale;
    target.visible = source.visible;
    target.skipDraw = source.skipDraw;
  }
}
