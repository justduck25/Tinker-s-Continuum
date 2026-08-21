package slimeknights.tconstruct.library.materials.traits;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class UpdateMaterialTraitsPacket implements IThreadsafePacket, CustomPacketPayload {
  public static final Type<UpdateMaterialTraitsPacket> TYPE = new Type<>(TConstruct.getResource("update_material_traits"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMaterialTraitsPacket> STREAM_CODEC = StreamCodec.of(
    (buffer, packet) -> packet.encode(buffer),
    UpdateMaterialTraitsPacket::new);

  protected final Map<MaterialId,MaterialTraits> materialToTraits;

  public UpdateMaterialTraitsPacket(RegistryFriendlyByteBuf buffer) {
    int materialCount = buffer.readInt();
    materialToTraits = new HashMap<>(materialCount);
    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readIdentifier());
      MaterialTraits traits = MaterialTraits.read(buffer);
      materialToTraits.put(id, traits);
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    RegistryFriendlyByteBuf buf = (RegistryFriendlyByteBuf) buffer;
    buf.writeInt(materialToTraits.size());
    materialToTraits.forEach((materialId, traits) -> {
      buf.writeIdentifier(materialId.getId());
      traits.write(buf);
    });
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    MaterialRegistry.updateMaterialTraitsFromServer(this);
  }
}
