package slimeknights.tconstruct.library.tools.definition.module.build;

import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Global volatile-data hook bridge for addons that need to contribute persistent
 * tool slots without registering a modifier or changing every tool definition.
 *
 * <p>Hooks run after the normal tool-definition and modifier volatile-data hooks
 * and may add slot data to the supplied volatile data object.</p>
 */
public final class ToolVolatileDataHooks {
  private static final CopyOnWriteArrayList<ToolVolatileDataHook> EXTERNAL_HOOKS = new CopyOnWriteArrayList<>();

  private ToolVolatileDataHooks() {}

  /** Registers an addon volatile-data hook. */
  public static void register(ToolVolatileDataHook hook) {
    ToolVolatileDataHook value = Objects.requireNonNull(hook, "hook");
    if (!EXTERNAL_HOOKS.contains(value)) {
      EXTERNAL_HOOKS.add(value);
    }
  }

  /** Removes a previously registered addon volatile-data hook. */
  public static void unregister(ToolVolatileDataHook hook) {
    EXTERNAL_HOOKS.remove(hook);
  }

  /** Applies all registered addon hooks to the current volatile-data object. */
  public static void apply(IToolContext context, ToolDataNBT volatileData) {
    for (ToolVolatileDataHook hook : EXTERNAL_HOOKS) {
      hook.addVolatileData(context, volatileData);
    }
  }
}

