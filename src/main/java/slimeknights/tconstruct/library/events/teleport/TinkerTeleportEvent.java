package slimeknights.tconstruct.library.events.teleport;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Event fired when Tinkers' Construct teleports an entity. Replaces the removed NeoForge EntityTeleportEvent use. */
public class TinkerTeleportEvent extends Event implements ICancellableEvent {
  private final Entity entity;
  private double targetX;
  private double targetY;
  private double targetZ;

  public TinkerTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
    this.entity = entity;
    this.targetX = targetX;
    this.targetY = targetY;
    this.targetZ = targetZ;
  }

  public Entity getEntity() {
    return entity;
  }

  public double getTargetX() {
    return targetX;
  }

  public void setTargetX(double targetX) {
    this.targetX = targetX;
  }

  public double getTargetY() {
    return targetY;
  }

  public void setTargetY(double targetY) {
    this.targetY = targetY;
  }

  public double getTargetZ() {
    return targetZ;
  }

  public void setTargetZ(double targetZ) {
    this.targetZ = targetZ;
  }

  public void setTarget(double targetX, double targetY, double targetZ) {
    this.targetX = targetX;
    this.targetY = targetY;
    this.targetZ = targetZ;
  }
}