package slimeknights.tconstruct.library.client.armor.texture;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.tools.data.ModifierIds;

import javax.annotation.Nullable;

/**
 * Armor texture supplier that selects mossy overlay based on modifier level.
 */
public class MossyArmorTextureSupplier implements ArmorTextureSupplier {
  public static final RecordLoadable<MossyArmorTextureSupplier> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("prefix", s -> s.prefix),
    MossyArmorTextureSupplier::new);

  private final Identifier prefix;
  private final TintedArmorTexture[][] textures; // [type][level - 1]

  public MossyArmorTextureSupplier(Identifier prefix) {
    this.prefix = prefix;
    this.textures = new TintedArmorTexture[TextureType.values().length][3];
    for (int level = 1; level <= 3; level++) {
      this.textures[TextureType.ARMOR.ordinal()][level - 1] = getTexture(prefix, "moss_" + level + "_armor");
      this.textures[TextureType.LEGGINGS.ordinal()][level - 1] = getTexture(prefix, "moss_" + level + "_leggings");
      this.textures[TextureType.WINGS.ordinal()][level - 1] = getTexture(prefix, "moss_" + level + "_wings");
    }
  }

  @Nullable
  private static TintedArmorTexture getTexture(Identifier base, String suffix) {
    String basePath = base.getPath();
    if (!basePath.endsWith("/")) {
      basePath += "/";
    }
    Identifier name = Identifier.fromNamespaceAndPath(base.getNamespace(), basePath + suffix);
    if (TEXTURE_VALIDATOR.test(name)) {
      return new TintedArmorTexture(ArmorTextureSupplier.getTexturePath(name), -1, 0);
    }
    return null;
  }

  @Override
  public ArmorTexture getArmorTexture(ItemStack stack, TextureType textureType, RegistryAccess access) {
    int level = ModifierUtil.getModifierLevel(stack, ModifierIds.mossy);
    if (level <= 0) {
      return ArmorTexture.EMPTY;
    }
    int clamped = Math.clamp(level, 1, 3);
    TintedArmorTexture texture = textures[textureType.ordinal()][clamped - 1];
    return texture != null ? texture : ArmorTexture.EMPTY;
  }

  @Override
  public RecordLoadable<MossyArmorTextureSupplier> getLoader() {
    return LOADER;
  }
}
