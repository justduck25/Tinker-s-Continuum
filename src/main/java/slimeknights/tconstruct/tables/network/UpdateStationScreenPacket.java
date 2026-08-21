package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;

public class UpdateStationScreenPacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<UpdateStationScreenPacket> TYPE = new Type<>(TConstruct.getResource("update_station_screen"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateStationScreenPacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    buffer -> new UpdateStationScreenPacket());
  public static final UpdateStationScreenPacket INSTANCE = new UpdateStationScreenPacket();

  private UpdateStationScreenPacket() {}

  @Override
  public void encode(FriendlyByteBuf packetBuffer) {}

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle();
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle() {
      Screen screen = Minecraft.getInstance().screen;
      if (screen instanceof BaseTabbedScreen<?,?> tabbedScreen) {
        tabbedScreen.updateDisplay();
      }
    }
  }
}