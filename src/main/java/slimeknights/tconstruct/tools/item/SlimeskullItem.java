package slimeknights.tconstruct.tools.item;

import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.armor.ArmorModelManager.ArmorModelDispatcher;
import slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier.TextureType;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.helper.ArmorUtil;
import slimeknights.tconstruct.library.tools.item.armor.ModifiableArmorItem;
import slimeknights.tconstruct.tools.client.SlimeskullArmorModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/** This item is mainly to return the proper model for a slimeskull */
public class SlimeskullItem extends ModifiableArmorItem {
  /** Model ID for our slimeskull. You may want your own for a custom slimeskull */
  public static final Identifier MODEL_LOCATION = TConstruct.getResource("slimeskull");

  private final Identifier name;

  public SlimeskullItem(ModifiableArmorMaterial material, Identifier name, Properties properties) {
    super(material, ArmorType.HELMET, properties);
    this.name = name;
  }

  public SlimeskullItem(ModifiableArmorMaterial material, Properties properties) {
    this(material, material.getId(), properties);
  }

  @Nullable
  public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
    return ArmorUtil.getDummyArmorTexture(slot);
  }

  public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    consumer.accept(new ArmorModelDispatcher() {
      @Override
      protected Identifier getName() {
        return name;
      }

      @Nonnull
      @Override
      public Model getGenericArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
        TextureType textureType = switch (layerType) {
          case HUMANOID_LEGGINGS -> TextureType.LEGGINGS;
          case WINGS -> TextureType.WINGS;
          default -> TextureType.ARMOR;
        };
        return SlimeskullArmorModel.INSTANCE.setup(stack, textureType, original, getModel(stack));
      }
    });
  }
}