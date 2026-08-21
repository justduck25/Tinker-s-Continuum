package slimeknights.tconstruct.smeltery.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet sent whenever the contents of the smeltery tank change
 */
public class SmelteryTankUpdatePacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<SmelteryTankUpdatePacket> TYPE = new Type<>(TConstruct.getResource("smeltery_tank_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SmelteryTankUpdatePacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    SmelteryTankUpdatePacket::new);

  private final BlockPos pos;
  private final List<FluidStack> fluids;

  public SmelteryTankUpdatePacket(BlockPos pos, List<FluidStack> fluids) {
    this.pos = pos;
    this.fluids = fluids.stream().filter(fluid -> !fluid.isEmpty()).map(FluidStack::copy).toList();
  }

  public SmelteryTankUpdatePacket(RegistryFriendlyByteBuf buffer) {
    pos = buffer.readBlockPos();
    int size = buffer.readVarInt();
    fluids = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      FluidStack fluid = FluidStack.STREAM_CODEC.decode(buffer);
      if (!fluid.isEmpty()) {
        fluids.add(fluid);
      }
    }
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeVarInt(fluids.size());
    for (FluidStack fluid : fluids) {
      FluidStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf)buffer, fluid);
    }
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  private static class HandleClient {
    private static void handle(SmelteryTankUpdatePacket packet) {
      BlockEntityHelper.get(ISmelteryTankHandler.class, Minecraft.getInstance().level, packet.pos).ifPresent(te -> te.updateFluidsFromPacket(packet.fluids));
    }
  }
}