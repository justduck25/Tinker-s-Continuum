package slimeknights.tconstruct.common.data.model;

import com.google.common.math.IntMath;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.data.GenericTextureGenerator;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;
import slimeknights.tconstruct.library.client.data.spritetransformer.ISpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.OffsettingSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.RecolorSpriteTransformer;
import slimeknights.tconstruct.library.client.data.util.DataGenSpriteReader;
import slimeknights.tconstruct.shared.block.SlimeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static slimeknights.tconstruct.TConstruct.getResource;

/** Provides generated textures used in general models. */
public class ModelSpriteProvider extends GenericTextureGenerator {
  private final List<CompletableFuture<?>> tasks = new ArrayList<>();
  private final DataGenSpriteReader spriteReader;

  public ModelSpriteProvider(PackOutput packOutput, ResourceManager resourceManager) {
    super(packOutput, "textures");
    this.spriteReader = new DataGenSpriteReader(resourceManager, "textures");
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    Identifier rootsSide = getResource("block/wood/enderbark/roots");
    Identifier rootsTop = getResource("block/wood/enderbark/roots_top");

    for (SlimeType slime : SlimeType.values()) {
      String name = slime.getSerializedName();
      Identifier congealed = getResource("block/slime/storage/congealed_" + name);
      stackSprites(cache, getResource("block/wood/enderbark/roots/" + name), rootsSide, congealed);
      stackSprites(cache, getResource("block/wood/enderbark/roots/" + name + "_top"), rootsTop, congealed);
    }

    ISpriteTransformer stoneColor = new RecolorSpriteTransformer(GreyToColorMapping.builderFromBlack()
      .addARGB(63, 0xFF181818)
      .addARGB(102, 0xFF494949)
      .addARGB(140, 0xFF5A5A5A)
      .addARGB(178, 0xFF787777)
      .addARGB(216, 0xFF95918D)
      .addARGB(255, 0xFFB3B1AF)
      .build());
    transformSprite(cache, getResource("item/tool/parts/plating_helmet"), getResource("item/tool/armor/plate/helmet/plating"), new OffsettingSpriteTransformer(stoneColor, 0, 2));
    transformSprite(cache, getResource("item/tool/parts/plating_chestplate"), getResource("item/tool/armor/plate/chestplate/plating"), stoneColor);
    transformSprite(cache, getResource("item/tool/parts/plating_leggings"), getResource("item/tool/armor/plate/leggings/plating"), new OffsettingSpriteTransformer(stoneColor, 0, 1));
    transformSprite(cache, getResource("item/tool/parts/plating_boots"), getResource("item/tool/armor/plate/boots/plating"), stoneColor);

    return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).thenRunAsync(spriteReader::closeAll);
  }

  private static int lcm(int a, int b) {
    return a * (b / IntMath.gcd(a, b));
  }

  protected void transformSprite(CachedOutput cache, Identifier output, Identifier input, ISpriteTransformer transformer) {
    try {
      NativeImage original = spriteReader.read(input);
      NativeImage generated = transformer.transformCopy(original, true);
      tasks.add(saveImage(cache, output, generated));
      JsonObject meta = transformer.animationMeta(original);
      if (meta != null) {
        tasks.add(saveMetadata(cache, output, meta));
      }
    } catch (IOException e) {
      TConstruct.LOG.error("Error transforming sprite {} into {}", input, output, e);
      tasks.add(CompletableFuture.failedFuture(e));
    }
  }

  protected void stackSprites(CachedOutput cache, Identifier output, Identifier... inputs) {
    List<NativeImage> sprites = Arrays.stream(inputs).map(path -> {
      try {
        return spriteReader.read(path);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).toList();

    int width = 1;
    int height = 1;
    Identifier metaLocation = null;
    for (int i = 0; i < sprites.size(); i++) {
      NativeImage sprite = sprites.get(i);
      width = lcm(width, sprite.getWidth());
      height = lcm(height, sprite.getHeight());
      Identifier location = inputs[i];
      if (spriteReader.metadataExists(location)) {
        if (metaLocation == null) {
          metaLocation = location;
        } else {
          throw new IllegalStateException("Multiple sprites have metadata, found " + metaLocation + " and " + location);
        }
      }
    }

    NativeImage generated = spriteReader.create(width, height);
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int color = 0;
        for (NativeImage sprite : sprites) {
          int spriteColor = sprite.getPixel(x % sprite.getWidth(), y % sprite.getHeight());
          if (ARGB.alpha(spriteColor) != 0) {
            color = spriteColor;
            break;
          }
        }
        generated.setPixel(x, y, color);
      }
    }
    tasks.add(saveImage(cache, output, generated));
    if (metaLocation != null) {
      try {
        tasks.add(saveMetadata(cache, output, spriteReader.readMetadata(metaLocation)));
      } catch (IOException e) {
        TConstruct.LOG.error("Failed to save sprite metadata", e);
        tasks.add(CompletableFuture.failedFuture(e));
      }
    }
  }

  @Override
  public String getName() {
    return "Tinkers' Construct model sprite provider";
  }
}