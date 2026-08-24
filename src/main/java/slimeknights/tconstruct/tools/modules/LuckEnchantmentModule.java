package slimeknights.tconstruct.tools.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.world.item.enchantment.Enchantment;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.json.TinkerEnchantmentLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.EnchantmentModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Map;

/** Reports the vanilla enchantment matching luck's active tool role. */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class LuckEnchantmentModule implements ModifierModule, EnchantmentModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<LuckEnchantmentModule>defaultHooks(ModifierHooks.ENCHANTMENTS);
  public static final RecordLoadable<LuckEnchantmentModule> LOADER = RecordLoadable.create(
    TinkerEnchantmentLoadable.INSTANCE.requiredField("fortune", LuckEnchantmentModule::fortune),
    TinkerEnchantmentLoadable.INSTANCE.requiredField("looting", LuckEnchantmentModule::looting),
    TinkerEnchantmentLoadable.INSTANCE.requiredField("luck_of_the_sea", LuckEnchantmentModule::luckOfTheSea),
    LuckEnchantmentModule::new);

  private final Enchantment fortune;
  private final Enchantment looting;
  private final Enchantment luckOfTheSea;

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

  @Override
  public int updateEnchantmentLevel(IToolStackView tool, ModifierEntry modifier, Enchantment enchantment, int level) {
    if (enchantment == fortune && getEnchantment(tool) == fortune) {
      level += modifier.intEffectiveLevel();
    }
    return level;
  }

  @Override
  public void updateEnchantments(IToolStackView tool, ModifierEntry modifier, Map<Enchantment,Integer> map) {
    Enchantment enchantment = getEnchantment(tool);
    if (enchantment != null) {
      EnchantmentModifierHook.addEnchantment(map, enchantment, modifier.intEffectiveLevel());
    }
  }

  @Override
  public RecordLoadable<LuckEnchantmentModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
