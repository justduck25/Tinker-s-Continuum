package slimeknights.tconstruct.tools.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSource.DiscardableLoader;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo;

import java.io.IOException;
import java.util.Map.Entry;

/** Sprite source creating modifier textures for banners using shield banner textures */
public record ShieldBannerModifierSpriteSource(int cropX, int cropY, int cropWidth, int cropHeight, Identifier destinationPrefix, int offsetX, int offsetY, int outSize) implements SpriteSource {
  private static final Codec<Integer> NON_NEGATIVE = ExtraCodecs.intRange(0, Integer.MAX_VALUE);
  private static final Codec<Integer> SHIELD_SIZE = ExtraCodecs.intRange(0, 64);
  public static final MapCodec<ShieldBannerModifierSpriteSource> CODEC = RecordCodecBuilder.<ShieldBannerModifierSpriteSource>mapCodec(inst -> inst.group(
    SHIELD_SIZE.fieldOf("crop_x").forGetter(ShieldBannerModifierSpriteSource::cropX),
    SHIELD_SIZE.fieldOf("crop_y").forGetter(ShieldBannerModifierSpriteSource::cropY),
    SHIELD_SIZE.fieldOf("crop_width").forGetter(ShieldBannerModifierSpriteSource::cropWidth),
    SHIELD_SIZE.fieldOf("crop_height").forGetter(ShieldBannerModifierSpriteSource::cropHeight),
    Identifier.CODEC.fieldOf("destination_prefix").forGetter(ShieldBannerModifierSpriteSource::destinationPrefix),
    NON_NEGATIVE.fieldOf("offset_x").forGetter(ShieldBannerModifierSpriteSource::offsetX),
    NON_NEGATIVE.fieldOf("offset_y").forGetter(ShieldBannerModifierSpriteSource::offsetY),
    NON_NEGATIVE.fieldOf("output_size").forGetter(ShieldBannerModifierSpriteSource::outSize)
  ).apply(inst, ShieldBannerModifierSpriteSource::new));
  private static final String SHIELD_TEXTURE_PREFIX = "entity/shield/";

  /** Registers this sprite source */
  public static void register(RegisterSpriteSourcesEvent event) {
    event.register(TConstruct.getResource("shield_banner_to_modifier"), CODEC);
  }

  @Override
  public void run(ResourceManager manager, Output output) {
    for (Entry<Identifier, Resource> entry : manager.listResources("textures/" + SHIELD_TEXTURE_PREFIX.substring(0, SHIELD_TEXTURE_PREFIX.length() - 1), id -> id.getPath().endsWith(".png")).entrySet()) {
      Identifier input = entry.getKey();
      Identifier sprite = TEXTURE_ID_CONVERTER.fileToId(input);
      String path = sprite.getPath();
      if (!path.startsWith(SHIELD_TEXTURE_PREFIX)) {
        continue;
      }
      Identifier pattern = sprite.withPath(path.substring(SHIELD_TEXTURE_PREFIX.length()));
      Identifier destination = destinationPrefix.withSuffix(MaterialRenderInfo.getSuffix(pattern));
      LazyLoadedImage image = new LazyLoadedImage(input, entry.getValue(), 1);
      output.add(destination, new BannerModifierSpriteLoader(image, input, destination));
    }
  }

  @Override
  public MapCodec<? extends SpriteSource> codec() {
    return CODEC;
  }

  /** Generates a cropped sprite lazily. */
  private class BannerModifierSpriteLoader implements DiscardableLoader {
    private final LazyLoadedImage original;
    private final Identifier input;
    private final Identifier output;

    private BannerModifierSpriteLoader(LazyLoadedImage original, Identifier input, Identifier output) {
      this.original = original;
      this.input = input;
      this.output = output;
    }

    @Override
    public SpriteContents get(net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader loader) {
      try {
        NativeImage original = this.original.get();
        int scale = original.getWidth() / 64;
        if (scale <= 0) {
          TConstruct.LOG.warn("Unable to crop {} to produce {} as texture size is less than 64", input, output);
        } else {
          NativeImage generated = new NativeImage(outSize * scale, outSize * scale, true);
          original.copyRect(generated, cropX * scale, cropY * scale, offsetX * scale, offsetY * scale, cropWidth * scale, cropHeight * scale, false, false);
          return new SpriteContents(this.output, new FrameSize(generated.getWidth(), generated.getHeight()), generated);
        }
      } catch (IllegalArgumentException | IOException ex) {
        TConstruct.LOG.warn("Unable to crop {} to produce {}", this.input, this.output, ex);
      } finally {
        discard();
      }
      return null;
    }

    @Override
    public void discard() {
      this.original.release();
    }
  }
}
