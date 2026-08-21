package slimeknights.tconstruct.library.events.teleport;

import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.world.entity.EnderSlimeEntity;

/** Fired when an ender slime teleports or teleports another entity. */
public class EnderSlimeTeleportEvent extends TinkerTeleportEvent {
  /** Gets the slime that caused this teleport. If this is the same as {@link #getEntity()} then the slime is teleporting itself. */
  @Getter
  private final EnderSlimeEntity slime;

  public EnderSlimeTeleportEvent(LivingEntity entity, double targetX, double targetY, double targetZ, EnderSlimeEntity slime) {
    super(entity, targetX, targetY, targetZ);
    this.slime = slime;
  }

  /** Checks if the enderslime is teleporting itself. */
  public boolean isTeleportingSelf() {
    return getEntity() == slime;
  }
}