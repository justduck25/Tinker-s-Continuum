package slimeknights.tconstruct.library.tools.definition.module.build;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import net.minecraft.server.level.ServerPlayer;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * Global bridge for addons that need to observe successful fishing catches.
 * The callback receives the server player and the live outer tool used to reel in.
 */
public final class ToolFishingHooks {
  private static final CopyOnWriteArrayList<BiConsumer<ServerPlayer, ToolStack>> EXTERNAL_HOOKS =
      new CopyOnWriteArrayList<>();

  private ToolFishingHooks() {}

  /** Registers a fishing callback. Duplicate callback instances are ignored. */
  public static void register(BiConsumer<ServerPlayer, ToolStack> hook) {
    BiConsumer<ServerPlayer, ToolStack> value = Objects.requireNonNull(hook, "hook");
    if (!EXTERNAL_HOOKS.contains(value)) {
      EXTERNAL_HOOKS.add(value);
    }
  }

  /** Removes a previously registered fishing callback. */
  public static void unregister(BiConsumer<ServerPlayer, ToolStack> hook) {
    EXTERNAL_HOOKS.remove(hook);
  }

  /** Notifies registered callbacks after a successful fish catch. */
  public static void onSuccessfulCatch(ServerPlayer player, ToolStack tool) {
    for (BiConsumer<ServerPlayer, ToolStack> hook : EXTERNAL_HOOKS) {
      hook.accept(player, tool);
    }
  }
}
