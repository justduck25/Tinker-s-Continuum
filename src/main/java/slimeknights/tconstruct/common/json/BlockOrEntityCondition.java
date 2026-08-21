package slimeknights.tconstruct.common.json;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/** Loot condition that only passes for block or entity loot contexts. */
public final class BlockOrEntityCondition implements LootItemCondition {
  public static final BlockOrEntityCondition INSTANCE = new BlockOrEntityCondition();
  public static final MapCodec<BlockOrEntityCondition> CODEC = MapCodec.unit(INSTANCE);

  private BlockOrEntityCondition() {}

  @Override
  public boolean test(LootContext context) {
    return context.hasParameter(LootContextParams.THIS_ENTITY)
      || context.hasParameter(LootContextParams.BLOCK_STATE);
  }

  @Override
  public MapCodec<? extends LootItemCondition> codec() {
    return CODEC;
  }
}
