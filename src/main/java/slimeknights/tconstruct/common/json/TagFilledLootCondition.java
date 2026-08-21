package slimeknights.tconstruct.common.json;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/** Loot condition that passes when an item tag contains at least one registered item. */
public record TagFilledLootCondition(TagKey<Item> tag) implements LootItemCondition {
  public static final MapCodec<TagFilledLootCondition> CODEC = TagKey.codec(Registries.ITEM)
    .fieldOf("tag")
    .xmap(TagFilledLootCondition::new, TagFilledLootCondition::tag);

  @Override
  public boolean test(LootContext context) {
    return context.getLevel().registryAccess().lookupOrThrow(Registries.ITEM).get(tag).isPresent();
  }

  @Override
  public MapCodec<? extends LootItemCondition> codec() {
    return CODEC;
  }
}
