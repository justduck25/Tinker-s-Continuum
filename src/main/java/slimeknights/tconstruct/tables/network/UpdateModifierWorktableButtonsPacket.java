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
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.tables.block.entity.table.ModifierWorktableBlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Syncs Modifier Worktable button data from the server, as MC 26.1 clients no longer receive full custom recipes. */
public class UpdateModifierWorktableButtonsPacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<UpdateModifierWorktableButtonsPacket> TYPE = new Type<>(TConstruct.getResource("update_modifier_worktable_buttons"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateModifierWorktableButtonsPacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    UpdateModifierWorktableButtonsPacket::new);

  private final BlockPos pos;
  private final List<ModifierEntry> entries;
  private final int selectedIndex;
  private final ItemStack result;

  public UpdateModifierWorktableButtonsPacket(BlockPos pos, List<ModifierEntry> entries, int selectedIndex, ItemStack result) {
    this.pos = pos;
    this.entries = List.copyOf(entries);
    this.selectedIndex = selectedIndex;
    this.result = result.copy();
  }

  public UpdateModifierWorktableButtonsPacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    int size = buffer.readVarInt();
    List<ModifierEntry> entries = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Identifier id = buffer.readIdentifier();
      int level = buffer.readVarInt();
      entries.add(new ModifierEntry(new ModifierId(id), level));
    }
    this.entries = List.copyOf(entries);
    this.selectedIndex = buffer.readVarInt();
    this.result = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    RegistryFriendlyByteBuf registryBuffer = (RegistryFriendlyByteBuf) buffer;
    buffer.writeBlockPos(pos);
    buffer.writeVarInt(entries.size());
    for (ModifierEntry entry : entries) {
      buffer.writeIdentifier(entry.getId().getId());
      buffer.writeVarInt(entry.getLevel());
    }
    buffer.writeVarInt(selectedIndex);
    ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuffer, result);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  public List<ModifierEntry> entries() {
    return entries;
  }

  private static class HandleClient {
    private static void handle(UpdateModifierWorktableButtonsPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        BlockEntityHelper.get(ModifierWorktableBlockEntity.class, world, packet.pos).ifPresent(te -> te.updateClientButtons(packet.entries, packet.selectedIndex, packet.result));
      }
    }
  }
}
