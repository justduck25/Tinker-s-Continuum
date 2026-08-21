package slimeknights.tconstruct.library.events.teleport;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.utils.TeleportHelper.ITeleportEventFactory;

/** Event fired when an entity teleports via the fluid effect. */
public class FluidEffectTeleportEvent extends TinkerTeleportEvent {
  public static final ITeleportEventFactory TELEPORT_FACTORY = FluidEffectTeleportEvent::new;

  public FluidEffectTeleportEvent(LivingEntity entity, double targetX, double targetY, double targetZ) {
    super(entity, targetX, targetY, targetZ);
  }
}