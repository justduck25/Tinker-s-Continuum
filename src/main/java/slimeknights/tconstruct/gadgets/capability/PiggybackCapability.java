package slimeknights.tconstruct.gadgets.capability;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import slimeknights.tconstruct.TConstruct;

/** Capability logic */
public class PiggybackCapability {
  private static final Identifier ID = TConstruct.getResource("piggyback");
  public static final EntityCapability<PiggybackHandler, Void> PIGGYBACK = EntityCapability.createVoid(ID, PiggybackHandler.class);

  private PiggybackCapability() {}

  /** Registers this capability */
  public static void register() {
    IEventBus bus = ModList.get().getModContainerById(TConstruct.MOD_ID)
      .map(ModContainer::getEventBus)
      .orElseThrow(() -> new RuntimeException("Could not find mod event bus for " + TConstruct.MOD_ID));
    bus.addListener(EventPriority.NORMAL, false, RegisterCapabilitiesEvent.class, PiggybackCapability::register);
  }

  /** Registers the capability with the event bus */
  private static void register(RegisterCapabilitiesEvent event) {
    event.registerEntity(PIGGYBACK, EntityType.PLAYER, (Entity entity, Void ctx) -> entity instanceof Player player ? new PiggybackHandler(player) : null);
  }
}
