package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.tables.block.entity.table.PartBuilderBlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Syncs Part Builder button data from the server, as MC 26.1 clients no longer receive full custom recipes. */
public class UpdatePartBuilderButtonsPacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<UpdatePartBuilderButtonsPacket> TYPE = new Type<>(TConstruct.getResource("update_part_builder_buttons"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePartBuilderButtonsPacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    UpdatePartBuilderButtonsPacket::new);

  private final BlockPos pos;
  private final List<Entry> entries;

  public UpdatePartBuilderButtonsPacket(BlockPos pos, List<Entry> entries) {
    this.pos = pos;
    this.entries = List.copyOf(entries);
  }

  public UpdatePartBuilderButtonsPacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    int size = buffer.readVarInt();
    List<Entry> entries = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      entries.add(new Entry(new Pattern(buffer.readIdentifier()), buffer.readVarInt(), buffer.readBoolean(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)));
    }
    this.entries = List.copyOf(entries);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    RegistryFriendlyByteBuf registryBuffer = (RegistryFriendlyByteBuf) buffer;
    buffer.writeBlockPos(pos);
    buffer.writeVarInt(entries.size());
    for (Entry entry : entries) {
      buffer.writeIdentifier(entry.pattern().getId());
      buffer.writeVarInt(entry.cost());
      buffer.writeBoolean(entry.allowUncraftable());
      ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuffer, entry.result());
    }
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  public List<Entry> entries() {
    return entries;
  }

  public record Entry(Pattern pattern, int cost, boolean allowUncraftable, ItemStack result) {}

  private static class HandleClient {
    private static void handle(UpdatePartBuilderButtonsPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        BlockEntityHelper.get(PartBuilderBlockEntity.class, world, packet.pos).ifPresent(te -> te.updateClientButtons(packet.entries));
      }
    }
  }
}