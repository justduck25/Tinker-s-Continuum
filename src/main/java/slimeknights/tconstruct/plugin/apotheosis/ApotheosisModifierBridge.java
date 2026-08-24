package slimeknights.tconstruct.plugin.apotheosis;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.tools.data.ModifierIds;

/**
 * Optional Apotheosis bridge for TCon modifier levels.
 *
 * <p>This entry point has no hard load-time dependency on Apotheosis. The implementation class is
 * initialized only after the mod-list guard succeeds, so TCon remains safe when Apotheosis is absent.</p>
 */
public final class ApotheosisModifierBridge {
  private static final String APOTHEOSIS = "apotheosis";

  private ApotheosisModifierBridge() {}

  /**
   * Adds the supported Apotheosis bonus to a TCon modifier level.
   *
   * <p>A modifier is never created by this method: a base level of zero stays zero. This prevents
   * an affix from changing modifier availability, slot usage, recipes, tooltips, or saved NBT.</p>
   */
  public static int getEffectiveLevel(ItemStack stack, ModifierId modifier, int baseLevel) {
    if (baseLevel <= 0 || stack.isEmpty() || !ModList.get().isLoaded(APOTHEOSIS)
        || !Config.COMMON.apotheosisProsperousBridge.get()
        || !ModifierIds.luck.equals(modifier)) {
      return baseLevel;
    }
    return ApotheosisImpl.getEffectiveLuckLevel(stack, baseLevel);
  }

  /** Loaded only when Apotheosis is present. Keep all Apotheosis types in this class. */
  private static final class ApotheosisImpl {
    private static final Identifier BREAKER_PROSPEROUS = Identifier.fromNamespaceAndPath(APOTHEOSIS, "breaker/enchantment/prosperous");
    private static final Identifier RANGED_PROSPEROUS = Identifier.fromNamespaceAndPath(APOTHEOSIS, "ranged/enchantment/prosperous");
    private static final Identifier FORTUNE = Identifier.fromNamespaceAndPath("minecraft", "fortune");
    private static final Identifier LOOTING = Identifier.fromNamespaceAndPath("minecraft", "looting");
    private static final java.lang.reflect.Field ENCHANTMENT_FIELD = findEnchantmentField();
    private static final ThreadLocal<Boolean> APPLYING = ThreadLocal.withInitial(() -> false);

    private ApotheosisImpl() {}

    private static int getEffectiveLuckLevel(ItemStack stack, int baseLevel) {
      if (APPLYING.get()) {
        return baseLevel;
      }
      APPLYING.set(true);
      try {
        int bonus = 0;
        for (dev.shadowsoffire.apotheosis.affix.AffixInstance instance
            : (Iterable<dev.shadowsoffire.apotheosis.affix.AffixInstance>) dev.shadowsoffire.apotheosis.affix.AffixHelper.streamAffixes(stack)::iterator) {
          if (!instance.isValid()) {
            continue;
          }
          Identifier affixId = instance.affix().getId();
          if (BREAKER_PROSPEROUS.equals(affixId)) {
            bonus += getAffixEnchantmentBonus(instance, FORTUNE, baseLevel);
          } else if (RANGED_PROSPEROUS.equals(affixId)) {
            bonus += getAffixEnchantmentBonus(instance, LOOTING, baseLevel);
          }
        }
        return baseLevel + Math.max(bonus, 0);
      } finally {
        APPLYING.set(false);
      }
    }

    /**
     * Reuses Apotheosis' own EnchantmentAffix implementation so rarity and StepFunction rounding stay
     * identical to the affix tooltip/event. The current implementation does not read the event lookup,
     * so this stack-only bridge supplies no fabricated vanilla enchantment component.
     */
    @SuppressWarnings("unchecked")
    private static int getAffixEnchantmentBonus(dev.shadowsoffire.apotheosis.affix.AffixInstance instance,
                                                 Identifier expectedEnchantment, int baseLevel) {
      dev.shadowsoffire.apotheosis.affix.Affix affix = instance.getAffix();
      if (!(affix instanceof dev.shadowsoffire.apotheosis.affix.effect.EnchantmentAffix)) {
        return 0;
      }
      try {
        net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> target =
            (net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>) ENCHANTMENT_FIELD.get(affix);
        if (!target.is(expectedEnchantment)) {
          return 0;
        }
        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable =
            new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        mutable.set(target, baseLevel);
        net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent event =
            new net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent(instance.stack(), mutable, target, null);
        instance.getEnchantmentLevels(event);
        return Math.max(0, mutable.getLevel(target) - baseLevel);
      } catch (IllegalAccessException e) {
        return 0;
      }
    }

    private static java.lang.reflect.Field findEnchantmentField() {
      try {
        java.lang.reflect.Field field = dev.shadowsoffire.apotheosis.affix.effect.EnchantmentAffix.class.getDeclaredField("ench");
        field.setAccessible(true);
        return field;
      } catch (ReflectiveOperationException e) {
        throw new ExceptionInInitializerError(e);
      }
    }
  }
}
