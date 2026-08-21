package slimeknights.tconstruct.tools.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tools.menu.ToolContainerMenu;

/** Packet used when a fluid is changed inside a tool container menu */
public record ToolContainerFluidUpdatePacket(FluidStack fluid) implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<ToolContainerFluidUpdatePacket> TYPE = new Type<>(TConstruct.getResource("tool_container_fluid_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf, ToolContainerFluidUpdatePacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    ToolContainerFluidUpdatePacket::new);

  public ToolContainerFluidUpdatePacket(RegistryFriendlyByteBuf buffer) {
    this(FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer));
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    FluidStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf)buffer, fluid);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(fluid);
  }

  /** Safely runs client-side menu updates from the client packet handler. */
  private static class HandleClient {
    private static void handle(FluidStack fluid) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.player.containerMenu instanceof ToolContainerMenu toolMenu) {
        toolMenu.getTank().setFluid(fluid);
      }
    }
  }
}
