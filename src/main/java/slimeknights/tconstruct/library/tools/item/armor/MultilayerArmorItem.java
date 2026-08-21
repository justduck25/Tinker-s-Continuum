package slimeknights.tconstruct.library.tools.item.armor;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import slimeknights.tconstruct.library.client.armor.ArmorModelManager.ArmorModelDispatcher;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ArmorUtil;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Armor model that applies multiple texture layers in order */
public class MultilayerArmorItem extends ModifiableArmorItem {
  private final Identifier name;

  public MultilayerArmorItem(Properties properties, ToolDefinition toolDefinition, Identifier name) {
    super(properties, toolDefinition);
    this.name = name;
  }

  public MultilayerArmorItem(ModifiableArmorMaterial material, ArmorType type, Properties properties) {
    super(material, type, properties);
    this.name = material.getId();
  }

  public MultilayerArmorItem(ModifiableArmorMaterial material, ArmorType type, Properties properties, ToolDefinition toolDefinition, Identifier name) {
    super(properties, toolDefinition, type, net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(material.getEquipSound()), net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, name));
    this.name = name;
  }

  @Nullable
  public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
    return ArmorUtil.getDummyArmorTexture(slot);
  }

  public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    consumer.accept(new ArmorModelDispatcher() {
      protected Identifier getName() {
        return name;
      }
    });
  }
}
