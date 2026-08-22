package slimeknights.tconstruct.library.client.model;

import lombok.extern.log4j.Log4j2;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import slimeknights.mantle.data.listener.ResourceValidator;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Tracks dynamic item textures and optionally reports missing resources during a
 * client resource reload. This is the NeoForge 26.1 equivalent of the old
 * RegisterClientReloadListenersEvent validator.
 */
@Log4j2
public class DynamicTextureLoader extends ResourceValidator {
  /** Instance registered once with the client reload listener graph. */
  private static final DynamicTextureLoader INSTANCE = new DynamicTextureLoader();

  private DynamicTextureLoader() {
    super("textures/item", "textures", ".png");
  }

  @Override
  public void onReloadSafe(ResourceManager manager) {
    // When missing-texture logging is disabled, cache available resources so the
    // model validator can try fallback roots without triggering vanilla warnings.
    if (!Config.CLIENT.logMissingModifierTextures.get()) {
      super.onReloadSafe(manager);
    }
  }

  /** Registers the validator in the NeoForge client reload listener graph. */
  public static void init(AddClientReloadListenersEvent event) {
    event.addListener(TConstruct.getResource("dynamic_texture_loader"), INSTANCE);
  }

  /**
   * Creates the material validator used by dynamic model builders.
   * Item textures use the reload validator; non-item materials are checked
   * directly against the sprite getter so missing atlas sprites are rejected.
   */
  public static Predicate<Material> getTextureValidator(Function<Material, TextureAtlasSprite> spriteGetter,
                                                          boolean logMissingTextures) {
    if (logMissingTextures || INSTANCE.resources.isEmpty()) {
      // Vanilla logs missing sprites through the supplied getter.
      return material -> !MissingTextureAtlasSprite.getLocation().equals(spriteGetter.apply(material).contents().name());
    }
    return material -> {
      // Material no longer stores an atlas identifier in MC 26.1. Item texture
      // paths can still be checked from our cache before trying another root.
      Identifier texture = material.sprite();
      if (texture.getPath().startsWith("item/")) {
        return INSTANCE.test(texture);
      }
      // Non-item materials still need the sprite getter for the atlas lookup.
      return !MissingTextureAtlasSprite.getLocation().equals(spriteGetter.apply(material).contents().name());
    };
  }
}