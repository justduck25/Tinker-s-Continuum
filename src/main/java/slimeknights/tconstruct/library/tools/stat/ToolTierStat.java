package slimeknights.tconstruct.library.tools.stat;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.utils.HarvestTiers;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;

/** Tool stat for comparing tool tiers */
@SuppressWarnings("ClassCanBeRecord")
@Getter @RequiredArgsConstructor
public class ToolTierStat implements IToolStat<ToolMaterial> {
  private final ToolStatId name;

  @Override
  public boolean supports(Item item) {
    return RegistryHelper.contains(TinkerTags.Items.HARVEST, item);
  }

  @Override
  public ToolMaterial getDefaultValue() {
    return HarvestTiers.minTier();
  }

  @Override
  public Object makeBuilder() {
    return new TierBuilder(getDefaultValue());
  }

  @Override
  public ToolMaterial build(ModifierStatsBuilder parent, Object builder) {
    return ((TierBuilder) builder).value;
  }

  @Override
  public void update(ModifierStatsBuilder builder, ToolMaterial value) {
    builder.<TierBuilder>updateStat(this, b -> b.value = HarvestTiers.max(b.value, value));
  }

  @Nullable
  @Override
  public ToolMaterial read(Tag tag) {
    return tag.asString().map(Identifier::parse).map(HarvestTiers::byName).orElse(null);
  }

  @Nullable
  @Override
  public Tag write(ToolMaterial value) {
    return StringTag.valueOf(HarvestTiers.getName(value).toString());
  }

  @Override
  public ToolMaterial deserialize(JsonElement json) {
    Identifier id = Identifier.parse(json.getAsString());
    ToolMaterial tier = HarvestTiers.byName(id);
    if (tier == null) {
      throw new JsonSyntaxException("Unknown harvest tier " + id);
    }
    return tier;
  }

  @Override
  public JsonElement serialize(ToolMaterial value) {
    return new JsonPrimitive(HarvestTiers.getName(value).toString());
  }

  @Override
  public ToolMaterial fromNetwork(FriendlyByteBuf buffer) {
    Identifier id = buffer.readIdentifier();
    ToolMaterial tier = HarvestTiers.byName(id);
    if (tier != null) {
      return tier;
    }
    throw new DecoderException("Unknown tool tier " + id);
  }

  @Override
  public void toNetwork(FriendlyByteBuf buffer, ToolMaterial value) {
    buffer.writeIdentifier(HarvestTiers.getName(value));
  }

  @Override
  public Component formatValue(ToolMaterial value) {
    return Component.translatable(Util.makeTranslationKey("tool_stat", getName().getId())).append(HarvestTiers.getName((Object)value));
  }

  @Override
  public String toString() {
    return "ToolTierStat{" + name + '}';
  }

  @AllArgsConstructor
  private static class TierBuilder {
    private ToolMaterial value;
  }
}
