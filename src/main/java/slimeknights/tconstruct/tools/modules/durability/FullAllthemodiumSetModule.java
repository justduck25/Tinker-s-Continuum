package slimeknights.tconstruct.tools.modules.durability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/** Cancels durability loss only when every material-bearing part is from the AllTheModium family. */
public enum FullAllthemodiumSetModule implements ModifierModule, ToolDamageModifierHook {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FullAllthemodiumSetModule>defaultHooks(ModifierHooks.TOOL_DAMAGE);
  public static final RecordLoadable<FullAllthemodiumSetModule> LOADER = new SingletonLoader<>(INSTANCE);
  private static final Set<MaterialId> MATERIALS = Set.of(
    MaterialIds.allthemodium,
    MaterialIds.vibranium,
    MaterialIds.unobtainium);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<FullAllthemodiumSetModule> getLoader() {
    return LOADER;
  }

  @Override
  public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
    return isFullAllthemodiumSet(tool) ? 0 : amount;
  }

  @Override
  public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder, @Nullable ItemStack stack, ModifierId cause) {
    return isFullAllthemodiumSet(tool) ? 0 : amount;
  }

  private static boolean isFullAllthemodiumSet(IToolStackView tool) {
    if (tool.getMaterials().isEmpty()) {
      return false;
    }
    for (MaterialVariant material : tool.getMaterials()) {
      if (!MATERIALS.contains(material.getVariant().getMaterialId())) {
        return false;
      }
    }
    return true;
  }
}
