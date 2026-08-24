package slimeknights.tconstruct.tools.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModList;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.json.TinkerEnchantmentLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.RequirementsModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

/** Caps luck using the Apothic Enchanting cap for the enchantment matching the tool role. */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class LuckApothicEnchantmentCapModule implements ModifierModule, ValidateModifierHook, RequirementsModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<LuckApothicEnchantmentCapModule>defaultHooks(ModifierHooks.VALIDATE_UPGRADE, ModifierHooks.REQUIREMENTS);
  private static final String APOTHIC_ENCHANTING = "apothic_enchanting";
  private static final String TRANSLATION_KEY = "modifier.tconstruct.apotheosis.cap";

  public static final RecordLoadable<LuckApothicEnchantmentCapModule> LOADER = RecordLoadable.create(
    TinkerEnchantmentLoadable.INSTANCE.requiredField("fortune", LuckApothicEnchantmentCapModule::fortune),
    TinkerEnchantmentLoadable.INSTANCE.requiredField("looting", LuckApothicEnchantmentCapModule::looting),
    TinkerEnchantmentLoadable.INSTANCE.requiredField("luck_of_the_sea", LuckApothicEnchantmentCapModule::luckOfTheSea),
    IntLoadable.FROM_ONE.requiredField("min_level", LuckApothicEnchantmentCapModule::minLevel),
    IntLoadable.FROM_ONE.requiredField("fallback_max", LuckApothicEnchantmentCapModule::fallbackMax),
    LuckApothicEnchantmentCapModule::new);

  private final Enchantment fortune;
  private final Enchantment looting;
  private final Enchantment luckOfTheSea;
  private final int minLevel;
  private final int fallbackMax;

  @Nullable
  private Enchantment getEnchantment(IToolStackView tool) {
    if (tool.hasTag(TinkerTags.Items.FISHING_RODS)) {
      return luckOfTheSea;
    }
    if (tool.hasTag(TinkerTags.Items.HARVEST)) {
      return fortune;
    }
    if (tool.hasTag(TinkerTags.Items.MELEE_WEAPON) || tool.hasTag(TinkerTags.Items.LAUNCHERS)) {
      return looting;
    }
    return null;
  }

  /** Gets the configured cap, falling back to the existing fixed cap if Apothic Enchanting is not present. */
  private int getCap(Enchantment enchantment) {
    if (ModList.get().isLoaded(APOTHIC_ENCHANTING)) {
      try {
        Class<?> hooks = Class.forName("dev.shadowsoffire.apothic_enchanting.asm.EnchHooks");
        Method method = hooks.getMethod("getMaxLevel", Enchantment.class);
        Object cap = method.invoke(null, enchantment);
        if (cap instanceof Integer value) {
          return Math.max(value, fallbackMax);
        }
      } catch (ReflectiveOperationException | LinkageError ignored) {}
    }
    return fallbackMax;
  }

  @Nullable
  private Component errorMessage(IToolStackView tool, ModifierEntry modifier) {
    if (modifier.getLevel() >= minLevel) {
      Enchantment enchantment = getEnchantment(tool);
      if (enchantment != null) {
        int cap = getCap(enchantment);
        if (modifier.getLevel() > cap) {
          return Component.translatable(TRANSLATION_KEY, cap);
        }
      }
    }
    return null;
  }

  @Nullable
  @Override
  public Component validate(IToolStackView tool, ModifierEntry modifier) {
    return errorMessage(tool, modifier);
  }

  @Nullable
  @Override
  public Component requirementsError(ModifierEntry entry) {
    return null;
  }

  @Override
  public List<ModifierEntry> displayModifiers(ModifierEntry entry) {
    return List.of();
  }

  @Override
  public RecordLoadable<LuckApothicEnchantmentCapModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
