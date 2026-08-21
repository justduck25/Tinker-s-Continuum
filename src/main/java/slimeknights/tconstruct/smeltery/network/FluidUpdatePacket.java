package slimeknights.tconstruct.smeltery.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;

public class FluidUpdatePacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<FluidUpdatePacket> TYPE = new Type<>(TConstruct.getResource("fluid_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidUpdatePacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    FluidUpdatePacket::new);

  protected final BlockPos pos;
  protected final FluidStack fluid;
  protected final int capacity;

  public FluidUpdatePacket(BlockPos pos, FluidStack fluid) {
    this(pos, fluid, 0);
  }

  public FluidUpdatePacket(BlockPos pos, FluidStack fluid, int capacity) {
    this.pos = pos;
    this.fluid = fluid.copy();
    this.capacity = capacity;
  }

  public FluidUpdatePacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    this.capacity = buffer.readVarInt();
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    FluidStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf)buffer, fluid);
    buffer.writeVarInt(capacity);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Interface to implement for anything wishing to receive fluid updates */
  public interface IFluidPacketReceiver {

    /**
     * Updates the current fluid to the specified value
     *
     * @param fluid New fluidstack
     */
    void updateFluidTo(FluidStack fluid);

    default void updateFluidTo(FluidStack fluid, int capacity) {
      updateFluidTo(fluid);
    }
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(FluidUpdatePacket packet) {
      BlockEntityHelper.get(IFluidPacketReceiver.class, Minecraft.getInstance().level, packet.pos).ifPresent(te -> {
        te.updateFluidTo(packet.fluid, packet.capacity);
        if (Minecraft.getInstance().level != null) {
          BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(packet.pos);
          if (blockEntity != null) {
            blockEntity.requestModelDataUpdate();
          }
          BlockState state = Minecraft.getInstance().level.getBlockState(packet.pos);
          Minecraft.getInstance().levelRenderer.blockChanged(Minecraft.getInstance().level, packet.pos, state, state, Block.UPDATE_CLIENTS);
        }
      });
    }
  }
}