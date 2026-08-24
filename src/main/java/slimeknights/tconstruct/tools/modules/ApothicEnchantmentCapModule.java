package slimeknights.tconstruct.tools.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModList;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
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

/** Caps Apotheosis post-cap modifier levels using Apothic Enchanting when present. */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class ApothicEnchantmentCapModule implements ModifierModule, ValidateModifierHook, RequirementsModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ApothicEnchantmentCapModule>defaultHooks(ModifierHooks.VALIDATE_UPGRADE, ModifierHooks.REQUIREMENTS);
  private static final String APOTHIC_ENCHANTING = "apothic_enchanting";
  private static final String TRANSLATION_KEY = "modifier.tconstruct.apotheosis.cap";

  public static final RecordLoadable<ApothicEnchantmentCapModule> LOADER = RecordLoadable.create(
    TinkerEnchantmentLoadable.INSTANCE.requiredField("name", ApothicEnchantmentCapModule::enchantment),
    BooleanLoadable.INSTANCE.defaultField("loot_level", false, false, ApothicEnchantmentCapModule::lootLevel),
    IntLoadable.FROM_ONE.requiredField("min_level", ApothicEnchantmentCapModule::minLevel),
    IntLoadable.FROM_ONE.requiredField("fallback_max", ApothicEnchantmentCapModule::fallbackMax),
    ApothicEnchantmentCapModule::new);

  private final Enchantment enchantment;
  private final boolean lootLevel;
  private final int minLevel;
  private final int fallbackMax;

  /** Gets the configured cap, falling back to the existing fixed cap if Apothic Enchanting is not present. */
  private int getCap() {
    if (ModList.get().isLoaded(APOTHIC_ENCHANTING)) {
      try {
        Class<?> hooks = Class.forName("dev.shadowsoffire.apothic_enchanting.asm.EnchHooks");
        Method method = hooks.getMethod(lootLevel ? "getMaxLootLevel" : "getMaxLevel", Enchantment.class);
        Object cap = method.invoke(null, enchantment);
        if (cap instanceof Integer value) {
          return Math.max(value, fallbackMax);
        }
      } catch (ReflectiveOperationException | LinkageError ignored) {}
    }
    return fallbackMax;
  }

  @Nullable
  private Component errorMessage(ModifierEntry modifier) {
    if (modifier.getLevel() >= minLevel) {
      int cap = getCap();
      if (modifier.getLevel() > cap) {
        return Component.translatable(TRANSLATION_KEY, cap);
      }
    }
    return null;
  }

  @Nullable
  @Override
  public Component validate(IToolStackView tool, ModifierEntry modifier) {
    return errorMessage(modifier);
  }

  @Nullable
  @Override
  public Component requirementsError(ModifierEntry entry) {
    return errorMessage(entry);
  }

  @Override
  public List<ModifierEntry> displayModifiers(ModifierEntry entry) {
    return List.of();
  }

  @Override
  public RecordLoadable<ApothicEnchantmentCapModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
