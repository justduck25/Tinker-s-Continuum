package slimeknights.tconstruct.tools.modifiers.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.TinkerModifiers;


/** Effect for rendering the charge up when you start using a helmet */
public class HelmetChargingEffect extends MobEffect {
  public HelmetChargingEffect() {
    super(MobEffectCategory.NEUTRAL, -1);
  }

  /* Helpers */

  /** Starts using the helmet with the charge time rendering */
  @SuppressWarnings("unchecked")
  public static int startUsingHelmet(IToolStackView tool, LivingEntity living, float speedFactor) {
    int time = GeneralInteractionModifierHook.startDrawing(tool, living, speedFactor);
    MobEffectInstance charging = new MobEffectInstance((Holder<MobEffect>)(Holder<?>) TinkerModifiers.helmetCharging, time + 20, 0, true, false, true);
    living.addEffect(charging);
    return time;
  }
}
