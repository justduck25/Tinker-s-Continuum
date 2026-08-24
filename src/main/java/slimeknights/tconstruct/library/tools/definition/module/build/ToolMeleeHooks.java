package slimeknights.tconstruct.library.tools.definition.module.build;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** Global bridge for addons observing successful TCon melee hits. */
public final class ToolMeleeHooks {
  @FunctionalInterface
  public interface Hook {
    void onSuccessfulHit(ToolStack tool, ToolAttackContext context, float damageDealt);
  }

  private static final CopyOnWriteArrayList<Hook> EXTERNAL_HOOKS = new CopyOnWriteArrayList<>();

  private ToolMeleeHooks() {}

  /** Registers a callback after TCon successfully hurts an attack target. */
  public static void register(Hook hook) {
    Hook value = Objects.requireNonNull(hook, "hook");
    if (!EXTERNAL_HOOKS.contains(value)) {
      EXTERNAL_HOOKS.add(value);
    }
  }

  /** Notifies callbacks after actual damage has been computed. */
  public static void onSuccessfulHit(ToolStack tool, ToolAttackContext context, float damageDealt) {
    for (Hook hook : EXTERNAL_HOOKS) {
      hook.onSuccessfulHit(tool, context, damageDealt);
    }
  }
}

