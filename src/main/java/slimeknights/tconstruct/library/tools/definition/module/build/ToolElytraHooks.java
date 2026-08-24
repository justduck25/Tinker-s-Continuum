package slimeknights.tconstruct.library.tools.definition.module.build;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * Global bridge for addons that need to observe a successful TCon elytra flight tick.
 * The callback receives TCon's authoritative tool and the live chestplate stack.
 */
public final class ToolElytraHooks {
  @FunctionalInterface
  public interface FlightHook {
    void onFlightTick(ToolStack tool, ItemStack chestplate, LivingEntity entity, int flightTicks);
  }

  private static final CopyOnWriteArrayList<FlightHook> EXTERNAL_HOOKS = new CopyOnWriteArrayList<>();

  private ToolElytraHooks() {}

  /** Registers a flight callback. Duplicate callback instances are ignored. */
  public static void register(FlightHook hook) {
    FlightHook value = Objects.requireNonNull(hook, "hook");
    if (!EXTERNAL_HOOKS.contains(value)) {
      EXTERNAL_HOOKS.add(value);
    }
  }

  /** Removes a previously registered flight callback. */
  public static void unregister(FlightHook hook) {
    EXTERNAL_HOOKS.remove(hook);
  }

  /** Notifies registered callbacks after TCon accepted the flight tick. */
  public static void onFlightTick(ToolStack tool, ItemStack chestplate, LivingEntity entity, int flightTicks) {
    for (FlightHook hook : EXTERNAL_HOOKS) {
      hook.onFlightTick(tool, chestplate, entity, flightTicks);
    }
    // Persist flight XP and any other addon changes on the authoritative stack.
    tool.updateStack(chestplate);
  }
}

