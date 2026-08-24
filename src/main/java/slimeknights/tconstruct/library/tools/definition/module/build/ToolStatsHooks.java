package slimeknights.tconstruct.library.tools.definition.module.build;

import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Global stat hook bridge for addons that need to contribute persistent tool stats
 * without registering a modifier or changing every tool definition datapack.
 *
 * <p>Hooks are called after the normal tool-definition and modifier stat hooks,
 * while the tool is being rebuilt. Callers should only add stats through the
 * supplied builder and must not mutate the tool from this callback.</p>
 */
public final class ToolStatsHooks {
  private static final CopyOnWriteArrayList<ToolStatsHook> EXTERNAL_HOOKS = new CopyOnWriteArrayList<>();

  private ToolStatsHooks() {}

  /**
   * Registers an addon stat hook.
   *
   * @param hook hook to call during tool stat rebuilds
   */
  public static void register(ToolStatsHook hook) {
    ToolStatsHook value = Objects.requireNonNull(hook, "hook");
    if (!EXTERNAL_HOOKS.contains(value)) {
      EXTERNAL_HOOKS.add(value);
    }
  }

  /** Removes a previously registered addon stat hook. */
  public static void unregister(ToolStatsHook hook) {
    EXTERNAL_HOOKS.remove(hook);
  }

  /** Applies all registered addon hooks to the current stat builder. */
  public static void apply(IToolContext context, ModifierStatsBuilder builder) {
    for (ToolStatsHook hook : EXTERNAL_HOOKS) {
      hook.addToolStats(context, builder);
    }
  }
}
