package slimeknights.tconstruct.library.events.teleport;

import net.minecraft.world.entity.LivingEntity;

/** Event fired when {@link slimeknights.tconstruct.shared.TinkerEffects#returning} teleport triggers. */
public class ReturningTeleportEvent extends TinkerTeleportEvent {
  public ReturningTeleportEvent(LivingEntity entity, double targetX, double targetY, double targetZ) {
    super(entity, targetX, targetY, targetZ);
  }
}