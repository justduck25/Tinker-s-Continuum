package slimeknights.tconstruct.library.modifiers.fluid;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.TinkerEnchantmentLoadable;

import java.util.ArrayList;
import java.util.List;

/** Packet to sync fluid predicates to the client */
@Internal
public record UpdateFluidEffectsPacket(List<FluidEffects.Entry> fluids) implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<UpdateFluidEffectsPacket> TYPE = new Type<>(TConstruct.getResource("update_fluid_effects"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateFluidEffectsPacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    UpdateFluidEffectsPacket::decode);

  /** Clientside constructor, reading from the buffer */
  public static UpdateFluidEffectsPacket decode(FriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    List<FluidEffects.Entry> entries = new ArrayList<>(size);
    HolderLookup.Provider previousLookup = null;
    if (buffer instanceof RegistryFriendlyByteBuf registryBuffer) {
      previousLookup = TinkerEnchantmentLoadable.setLookupProvider(registryBuffer.registryAccess());
    }
    try {
      for (int i = 0; i < size; i++) {
        Identifier key = buffer.readIdentifier();
        try {
          FluidEffects effects = FluidEffects.LOADABLE.decode(buffer, FluidEffectManager.contextBuilder(key).build());
          entries.add(new FluidEffects.Entry(key, effects));
        } catch (RuntimeException e) {
          // put exception in the log with a bit more info
          TConstruct.LOG.error("Failed to decode fluid effects with ID {}", key, e);
          throw e;
        }
      }
    } finally {
      if (previousLookup != null) {
        TinkerEnchantmentLoadable.setLookupProvider(previousLookup);
      }
    }
    return new UpdateFluidEffectsPacket(List.copyOf(entries));
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(fluids.size());
    for (FluidEffects.Entry entry : fluids) {
      Identifier key = entry.name();
      buffer.writeIdentifier(key);
      try {
        FluidEffects.LOADABLE.encode(buffer, entry.effects());
      } catch (RuntimeException e) {
        TConstruct.LOG.error("Failed to encode fluid effects with ID {}", key, e);
        throw e;
      }
    }
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    FluidEffectManager.INSTANCE.updateFromServer(fluids);
  }
}
