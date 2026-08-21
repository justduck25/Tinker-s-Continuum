package slimeknights.tconstruct.plugin.jei.util;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import javax.annotation.Nullable;

/** Common subtype logic for potion item/fluid ingredients in Minecraft 26.1. */
public interface PotionSubtypeInterpreter<T> extends ISubtypeInterpreter<T> {
  @Nullable
  PotionContents getPotionContents(T ingredient);

  @Override
  default Object getSubtypeData(T ingredient, UidContext context) {
    PotionContents contents = getPotionContents(ingredient);
    if (contents == null) {
      return "";
    }
    StringBuilder stringBuilder = new StringBuilder(contents.getName("").getString());
    for (MobEffectInstance effect : contents.getAllEffects()) {
      stringBuilder.append(';').append(effect);
    }
    return stringBuilder.toString();
  }
}