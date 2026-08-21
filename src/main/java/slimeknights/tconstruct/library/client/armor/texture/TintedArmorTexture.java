package slimeknights.tconstruct.library.client.armor.texture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import slimeknights.tconstruct.library.client.armor.AbstractArmorModel;
import slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier.ArmorTexture;

/** Armor texture which tints the texture */
public class TintedArmorTexture implements ArmorTexture {
  private static final int MAX_LIGHT = 15728880; // LightTexture.pack(15, 15)

  private final Identifier texture;
  private int color = -1;
  private int luminosity = 0;

  public TintedArmorTexture(Identifier texture, int color, int luminosity) {
    this.texture = texture;
    this.color = color;
    this.luminosity = luminosity;
  }

  public TintedArmorTexture(Identifier texture, int color) {
    this(texture, color, 0);
  }

  public Identifier texture() {
    return texture;
  }

  public int color() {
    return color;
  }

  public void color(int color) {
    this.color = color;
  }

  public int luminosity() {
    return luminosity;
  }

  public void luminosity(int luminosity) {
    this.luminosity = luminosity;
  }

  /** Applies luminosity to the given lightmap color. Assumes that {@code luminosity} is between 1 and 15. */
  public static int applyLuminosity(int packedLight, int luminosity) {
    // if full bright, skip some math
    if (luminosity >= 15) {
      return TintedArmorTexture.MAX_LIGHT;
    }
    // inlined version of methods from LightTexture
    return Math.max(luminosity, (packedLight & 0xFFFF) >> 4) << 4
      | Math.max(luminosity, packedLight >> 20 & 0xFFFF) << 20;
  }

  @Override
  public void renderTexture(Model model, PoseStack matrices, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, boolean hasGlint) {
    VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.armorCutoutNoCull(texture));
    if (hasGlint) {
      buffer = VertexMultiConsumer.create(buffer, bufferSource.getBuffer(RenderTypes.armorEntityGlint()));
    }
    if (luminosity > 0) {
      packedLight = applyLuminosity(packedLight, luminosity);
    }
    AbstractArmorModel.renderColored(model, matrices, buffer, packedLight, packedOverlay, color, red, green, blue, alpha);
  }
}
