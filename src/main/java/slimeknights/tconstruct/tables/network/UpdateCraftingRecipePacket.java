package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;

/** Packet to send the current crafting result to a player who opens the crafting station */
public class UpdateCraftingRecipePacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<UpdateCraftingRecipePacket> TYPE = new Type<>(TConstruct.getResource("update_crafting_recipe"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateCraftingRecipePacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    UpdateCraftingRecipePacket::new);

  private final BlockPos pos;
  private final ItemStack result;

  public UpdateCraftingRecipePacket(BlockPos pos, ItemStack result) {
    this.pos = pos;
    this.result = result.copy();
  }

  public UpdateCraftingRecipePacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.result = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, result);
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
    private static void handle(UpdateCraftingRecipePacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        BlockEntityHelper.get(CraftingStationBlockEntity.class, world, packet.pos).ifPresent(te -> te.updateSyncedResult(packet.result));
      }
    }
  }
}