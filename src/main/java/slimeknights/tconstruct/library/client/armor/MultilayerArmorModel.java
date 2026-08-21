package slimeknights.tconstruct.library.client.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.client.armor.ArmorModelManager.ArmorModel;
import slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier.TextureType;

/** Armor model that just applies the list of textures */
public class MultilayerArmorModel extends AbstractArmorModel {
  public static final MultilayerArmorModel INSTANCE = new MultilayerArmorModel();

  protected ItemStack armorStack = ItemStack.EMPTY;
  protected ArmorModel model = ArmorModel.EMPTY;
  protected RegistryAccess registryAccess = RegistryAccess.EMPTY;

  protected MultilayerArmorModel() {}

  /** Prepares this model */
  public Model setup(ItemStack stack, TextureType textureType, Model base, ArmorModel model) {
    this.model = model;
    this.registryAccess = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : RegistryAccess.EMPTY;
    if (!model.layers().isEmpty()) {
      setup(stack, textureType, base);
      this.armorStack = stack;
    } else {
      this.armorStack = ItemStack.EMPTY;
    }
    return this;
  }

}
