package slimeknights.tconstruct.library.client.data.util;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Logic to read sprites from existing images and return native images which can later be modified
 */
@Log4j2
public class DataGenSpriteReader extends AbstractSpriteReader {
  private final ResourceManager manager;
  private final String folder;

  public DataGenSpriteReader(ResourceManager manager, String folder) {
    this.manager = manager;
    this.folder = folder;
  }

  /** Gets a location with the given extension */
  private Identifier getLocation(Identifier base, String extension) {
    return Identifier.fromNamespaceAndPath(base.getNamespace(), folder + "/" + base.getPath() + extension);
  }

  /** Gets a location for .png */
  private Identifier getLocation(Identifier base) {
    return getLocation(base, ".png");
  }

  @Override
  public boolean exists(Identifier path) {
    return manager.getResource(getLocation(path)).isPresent();
  }

  @Override
  public boolean metadataExists(Identifier path) {
    return manager.getResource(getLocation(path, ".png.mcmeta")).isPresent();
  }

  @Override
  public NativeImage read(Identifier path) throws IOException {
    try {
      Resource resource = manager.getResource(getLocation(path)).orElseThrow(FileNotFoundException::new);
      NativeImage image = NativeImage.read(resource.open());
      openedImages.add(image);
      return image;
    } catch (IOException e) {
      log.error("Failed to read image at {}", path);
      throw e;
    }
  }

  @Override
  public JsonObject readMetadata(Identifier path) throws IOException {
    try (BufferedReader reader = manager.getResource(getLocation(path, ".png.mcmeta")).orElseThrow(FileNotFoundException::new).openAsReader()) {
      return GsonHelper.parse(reader);
    }
  }
}
