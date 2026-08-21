package slimeknights.tconstruct.library.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;

/**
 * Render types for Tinkers Construct.
 * In 1.21.2, most custom render types are now created via {@link RenderTypes} factory methods.
 */
public final class TinkerRenderTypes {
  private TinkerRenderTypes() {}

  /** Render type for the error block that is seen through everything, using vanilla's LINES type */
  public static final RenderType ERROR_BLOCK = RenderTypes.LINES;

  /** Render type for smeltery fluid (renders both sides, no cull).
   *  Same as Mantle's FLUID but without culling. */
  public static final RenderType SMELTERY_FLUID = RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
}
