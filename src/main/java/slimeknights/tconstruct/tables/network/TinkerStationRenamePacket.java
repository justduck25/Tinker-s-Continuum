package slimeknights.tconstruct.tables.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

/** Packet to send to the server to update the name in the UI */
@RequiredArgsConstructor
public class TinkerStationRenamePacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<TinkerStationRenamePacket> TYPE = new Type<>(TConstruct.getResource("tinker_station_rename"));
  public static final StreamCodec<RegistryFriendlyByteBuf, TinkerStationRenamePacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    TinkerStationRenamePacket::new);

  private final String name;

  public TinkerStationRenamePacket(RegistryFriendlyByteBuf buf) {
    this.name = buf.readUtf(Short.MAX_VALUE);
  }

  @Override
  public void encode(FriendlyByteBuf buf) {
    buf.writeUtf(name);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ServerPlayer sender = (ServerPlayer) context.player();
    if (sender != null && sender.containerMenu instanceof TinkerStationContainerMenu station) {
      TinkerStationBlockEntity tile = station.getTile();
      if (tile != null) {
        station.getTile().setItemName(name);
      }
    }
  }
}