package slimeknights.tconstruct.tools.modules;

import net.minecraft.world.item.equipment.ArmorType;

public interface ArmorModuleBuilder<T> {
  int[] MAX_DAMAGE_ARRAY = {11, 16, 15, 13};
  int SHIELD_DAMAGE = 22;

  T build(ArmorType slot);

  interface ArmorShieldModuleBuilder<T> extends ArmorModuleBuilder<T> {
    T buildShield();
  }
}
