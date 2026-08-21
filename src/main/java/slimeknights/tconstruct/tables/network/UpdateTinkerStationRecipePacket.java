package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.client.inventory.TinkerStationScreen;

import javax.annotation.Nullable;
import java.util.Optional;

/** Packet to send the current crafting recipe result to a player who opens the tinker station */
public class UpdateTinkerStationRecipePacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<UpdateTinkerStationRecipePacket> TYPE = new Type<>(TConstruct.getResource("update_tinker_station_recipe"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTinkerStationRecipePacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    UpdateTinkerStationRecipePacket::new);

  private final BlockPos pos;
  @Nullable
  private final Identifier recipe;
  private final ItemStack result;
  @Nullable
  private final Component error;

  public UpdateTinkerStationRecipePacket(BlockPos pos, @Nullable Identifier recipe, ItemStack result, @Nullable Component error) {
    this.pos = pos;
    this.recipe = recipe;
    this.result = result.copy();
    this.error = error;
  }

  public UpdateTinkerStationRecipePacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.recipe = buffer.readBoolean() ? buffer.readIdentifier() : null;
    this.result = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    this.error = ComponentSerialization.OPTIONAL_STREAM_CODEC.decode(buffer).orElse(null);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    RegistryFriendlyByteBuf registryBuffer = (RegistryFriendlyByteBuf) buffer;
    buffer.writeBlockPos(pos);
    buffer.writeBoolean(recipe != null);
    if (recipe != null) {
      buffer.writeIdentifier(recipe);
    }
    ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuffer, result);
    ComponentSerialization.OPTIONAL_STREAM_CODEC.encode(registryBuffer, Optional.ofNullable(error));
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(UpdateTinkerStationRecipePacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        boolean handled = false;
        if (Minecraft.getInstance().screen instanceof TinkerStationScreen stationScreen) {
          TinkerStationBlockEntity te = stationScreen.getTileEntity();
          if (te.getBlockPos().equals(packet.pos)) {
            te.updateSyncedResult(packet.result, packet.error);
            stationScreen.updateDisplay();
            handled = true;
          }
        }
        if (!handled) {
          BlockEntityHelper.get(TinkerStationBlockEntity.class, world, packet.pos).ifPresent(te -> te.updateSyncedResult(packet.result, packet.error));
        }
      }
    }
  }
}