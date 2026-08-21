package slimeknights.tconstruct.tools.modules.durability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;

/** Repairs damaged tools over time, with faster growth in light. */
public enum MossyRepairModule implements ModifierModule, InventoryTickModifierHook {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MossyRepairModule>defaultHooks(ModifierHooks.INVENTORY_TICK);
  public static final RecordLoadable<MossyRepairModule> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public RecordLoadable<MossyRepairModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
    if (world.isClientSide() || holder.getUseItem() == stack || !tool.hasTag(TinkerTags.Items.DURABILITY) || tool.isUnbreakable() || tool.getDamage() <= 0) {
      return;
    }

    int level = modifier.getLevel();
    int repairAmount;
    int interval;
    if (level >= 3) {
      repairAmount = 2;
      interval = 5 * 20;
    } else if (level == 2) {
      repairAmount = 2;
      interval = 15 * 20;
    } else {
      repairAmount = 1;
      interval = 20 * 20;
    }

    if (world.getMaxLocalRawBrightness(holder.blockPosition()) > 0) {
      interval = Math.max(1, interval / 2);
    }
    if (holder.tickCount % interval != 0) {
      return;
    }

    ToolDamageUtil.repair(tool, repairAmount);
    if (tool instanceof ToolStack toolStack) {
      toolStack.updateStack(stack, false);
    } else {
      ToolDamageUtil.syncDamageComponents(tool, stack);
    }
  }
}
