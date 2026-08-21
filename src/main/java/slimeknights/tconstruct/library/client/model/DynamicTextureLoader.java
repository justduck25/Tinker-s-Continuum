package slimeknights.tconstruct.library.client.model;

import lombok.extern.log4j.Log4j2;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
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
    // ResourceValidator already performs the scan and logs missing resources.
    // Skip it entirely when the diagnostic config is disabled.
    if (!Config.CLIENT.logMissingModifierTextures.get()) {
      return;
    }
    super.onReloadSafe(manager);
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
    if (!logMissingTextures) {
      return material -> true;
    }
    return material -> {
      Identifier texture = material.sprite();
      if (texture.getPath().startsWith("item/")) {
        return INSTANCE.test(material.sprite());
      }
      TextureAtlasSprite sprite = spriteGetter.apply(material);
      return !MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
    };
  }
}