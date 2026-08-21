package slimeknights.tconstruct.smeltery.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.inventory.BaseContainerMenu;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

/**
 * Packet sent when a fluid is clicked in the smeltery UI
 */
public class SmelteryFluidClickedPacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<SmelteryFluidClickedPacket> TYPE = new Type<>(TConstruct.getResource("smeltery_fluid_clicked"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SmelteryFluidClickedPacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    SmelteryFluidClickedPacket::new);

  private final int index;

  public SmelteryFluidClickedPacket(int index) {
    this.index = index;
  }

  public SmelteryFluidClickedPacket(RegistryFriendlyByteBuf buffer) {
    index = buffer.readVarInt();
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(index);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ServerPlayer sender = context.player() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    if (sender != null && !sender.isSpectator()) {
      AbstractContainerMenu container = sender.containerMenu;
      if (container instanceof BaseContainerMenu<?> base && base.getTile() instanceof ISmelteryTankHandler tank) {
        tank.getTank().moveFluidToBottom(index);
      }
    }
  }
}