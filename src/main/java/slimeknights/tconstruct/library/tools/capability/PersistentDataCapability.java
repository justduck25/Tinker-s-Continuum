package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.SyncPersistentDataPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

/** Persistent Tinkers data attached to entities. */
public class PersistentDataCapability {
  private PersistentDataCapability() {}

  private static final Identifier ID = TConstruct.getResource("persistent_data");
  public static final EntityCapability<ModDataNBT, Void> CAPABILITY = EntityCapability.createVoid(ID, ModDataNBT.class);

  public static ModDataNBT getOrWarn(Entity entity) {
    ModDataNBT data = CAPABILITY.getCapability(entity, null);
    if (data == null) {
      TConstruct.LOG.warn("Missing Tinkers NBT on entity {}, this should not happen", entity.getType());
      return new ModDataNBT();
    }
    return data;
  }

  public static void register() {
    TConstruct.MOD_EVENT_BUS.addListener(PersistentDataCapability::registerCapabilities);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.Clone.class, PersistentDataCapability::playerClone);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerRespawnEvent.class, PersistentDataCapability::playerRespawn);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerChangedDimensionEvent.class, PersistentDataCapability::playerChangeDimension);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerLoggedInEvent.class, PersistentDataCapability::playerLoggedIn);
  }

  private static void registerCapabilities(RegisterCapabilitiesEvent event) {
    for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE.stream().toList()) {
      event.registerEntity(CAPABILITY, type, (entity, ctx) -> entity instanceof LivingEntity || EntityModifierCapability.supportCapability(entity) ? new ModDataNBT() : null);
    }
  }

  private static void sync(Player player) {
    ModDataNBT data = CAPABILITY.getCapability(player, null);
    if (data != null && player instanceof ServerPlayer serverPlayer) {
      TinkerNetwork.getInstance().sendTo(new SyncPersistentDataPacket(data.getCopy()), serverPlayer);
    }
  }

  private static void playerClone(PlayerEvent.Clone event) {
    ModDataNBT oldData = CAPABILITY.getCapability(event.getOriginal(), null);
    ModDataNBT newData = CAPABILITY.getCapability(event.getEntity(), null);
    if (oldData != null && newData != null) {
      CompoundTag nbt = oldData.getCopy();
      if (!nbt.isEmpty()) {
        newData.copyFrom(nbt);
      }
    }
  }

  private static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) { sync(event.getEntity()); }
  private static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) { sync(event.getEntity()); }
  private static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) { sync(event.getEntity()); }
}