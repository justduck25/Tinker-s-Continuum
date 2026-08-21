package slimeknights.tconstruct.library.client.data;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.tconstruct.TConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Data generator to create png image files */
public abstract class GenericTextureGenerator extends GenericDataProvider {
  private final PackOutput packOutput;
  private final String folder;

  /** Constructor */
  public GenericTextureGenerator(PackOutput packOutput, String folder) {
    super(packOutput, Target.RESOURCE_PACK, folder);
    this.packOutput = packOutput;
    this.folder = folder;
  }

  /** Gets an output file in the resource pack root when no folder prefix is configured. */
  private Path file(Identifier location, String extension) {
    if (folder.isEmpty()) {
      return location.withSuffix('.' + extension).resolveAgainst(packOutput.getOutputFolder(Target.RESOURCE_PACK));
    }
    return this.pathProvider.file(location, extension);
  }

  /** Saves root resource-pack JSON. Use when the location path already contains its full folder. */
  protected CompletableFuture<?> saveRootJson(CachedOutput cache, Identifier location, JsonObject json) {
    return DataProvider.saveStable(cache, json, file(location, "json"));
  }

  /** Saves the given image to the given location */
  protected CompletableFuture<?> saveImage(CachedOutput cache, Identifier location, NativeImage image) {
    return CompletableFuture.runAsync(() -> {
      try {
        Path path = file(location, "png");
        Path temp = Files.createTempFile("tconstruct-image", ".png");
        try {
          image.writeToFile(temp);
          byte[] bytes = Files.readAllBytes(temp);
          cache.writeIfNeeded(path, bytes, Hashing.sha1().hashBytes(bytes));
        } finally {
          Files.deleteIfExists(temp);
        }
      } catch (IOException e) {
        TConstruct.LOG.error("Couldn't write image for {}", location, e);
        throw new CompletionException(e);
      }
    }, Util.backgroundExecutor());
  }

  /** Saves metadata for the given image */
  protected CompletableFuture<?> saveMetadata(CachedOutput cache, Identifier location, JsonObject metadata) {
    return DataProvider.saveStable(cache, metadata, file(location, "png.mcmeta"));
  }
}