package slimeknights.tconstruct.library.tools.definition.module.build;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** Global bridge for addons observing successful TCon block harvests. */
public final class ToolHarvestHooks {
  @FunctionalInterface
  public interface Hook {
    void onSuccessfulHarvest(ServerPlayer player, ToolStack tool, ItemStack stack);
  }

  private static final CopyOnWriteArrayList<Hook> EXTERNAL_HOOKS = new CopyOnWriteArrayList<>();

  private ToolHarvestHooks() {}

  /** Registers a callback after TCon successfully breaks a block with a tool. */
  public static void register(Hook hook) {
    Hook value = Objects.requireNonNull(hook, "hook");
    if (!EXTERNAL_HOOKS.contains(value)) {
      EXTERNAL_HOOKS.add(value);
    }
  }

  /** Notifies callbacks after the block-break pipeline succeeds. */
  public static void onSuccessfulHarvest(ServerPlayer player, ToolStack tool, ItemStack stack) {
    for (Hook hook : EXTERNAL_HOOKS) {
      hook.onSuccessfulHarvest(player, tool, stack);
    }
    // Persist addon changes before TCon continues with durability synchronization.
    tool.updateStack(stack);
  }
}

