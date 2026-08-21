package slimeknights.tconstruct.library.events.teleport;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/** @deprecated No longer used. See {@link SlingModifierTeleportEvent}. */
@Deprecated(forRemoval = true)
public class SlimeslingTeleportEvent extends TinkerTeleportEvent {
  @Getter
  private final ItemStack sling;
  public SlimeslingTeleportEvent(Entity entity, double targetX, double targetY, double targetZ, ItemStack sling) {
    super(entity, targetX, targetY, targetZ);
    this.sling = sling;
  }
}