package slimeknights.tconstruct.library.client.materials;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.TextureAtlas;
import slimeknights.mantle.data.loadable.common.ColorLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Determines the type of texture used for rendering a specific material.
 */
public record MaterialRenderInfo(MaterialVariantId id, @Nullable Identifier texture,
                                 String[] fallbacks, int vertexColor, int luminosity) {
  public static final RecordLoadable<MaterialRenderInfo> LOADABLE = RecordLoadable.create(
    MaterialVariantId.CONTEXT_KEY.requiredField(),
    MaterialTextureField.INSTANCE,
    StringLoadable.DEFAULT.array(String[]::new, false, 0).emptyField("fallbacks", MaterialRenderInfo::fallbacks),
    ColorLoadable.ALPHA.defaultField("color", false, MaterialRenderInfo::vertexColor),
    IntLoadable.range(0, 15).defaultField("luminosity", 0, MaterialRenderInfo::luminosity),
    MaterialRenderInfo::new);

  /**
   * Tries to get a sprite for the given texture.
   *
   * @param base Base texture.
   * @param suffix Sprite suffix.
   * @param spriteGetter Logic to get the sprite.
   * @return Sprite if valid, null if missing.
   */
  @Nullable
  private TextureAtlasSprite trySprite(Material base, String suffix, Function<Material,TextureAtlasSprite> spriteGetter) {
    Material materialTexture = getMaterial(base.sprite(), suffix);
    TextureAtlasSprite sprite = spriteGetter.apply(materialTexture);
    if (!MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
      return sprite;
    }
    return null;
  }

  /**
   * Gets the texture for this render material.
   *
   * @param base Base texture.
   * @param spriteGetter Logic to get the sprite.
   * @return Sprite, tint color, and emissivity.
   */
  public TintedSprite getSprite(Material base, Function<Material,TextureAtlasSprite> spriteGetter) {
    TextureAtlasSprite sprite = null;
    if (texture != null) {
      sprite = trySprite(base, getSuffix(texture), spriteGetter);
      if (sprite != null) {
        return new TintedSprite(sprite, -1, luminosity);
      }
    }
    for (String fallback : fallbacks) {
      sprite = trySprite(base, fallback, spriteGetter);
      if (sprite != null) {
        return new TintedSprite(sprite, vertexColor, luminosity);
      }
    }
    return new TintedSprite(spriteGetter.apply(base), vertexColor, luminosity);
  }

  /** Converts a material identifier into a sprite suffix. */
  public static String getSuffix(Identifier material) {
    String namespace = material.getNamespace();
    if ("minecraft".equals(namespace)) {
      return material.getPath();
    }
    return namespace + "_" + material.getPath();
  }

  private static Material getMaterial(Identifier texture, String suffix) {
    return new Material(texture.withPath(path -> path + "_" + suffix), false);
  }

  public record TintedSprite(TextureAtlasSprite sprite, int color, int emissivity) {}
}