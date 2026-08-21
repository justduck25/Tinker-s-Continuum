package slimeknights.tconstruct.tools.modifiers.loot;

import com.mojang.serialization.MapCodec;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;


import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

/** Condition to check if a held tool has the given modifier */
@RequiredArgsConstructor
public class HasModifierLootCondition implements LootItemCondition {
  private final ModifierId modifier;

  public static final MapCodec<HasModifierLootCondition> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
      net.minecraft.resources.Identifier.CODEC.xmap(id -> new ModifierId(id.toString()), id -> id.getId()).fieldOf("modifier").forGetter(c -> c.modifier)
  ).apply(inst, HasModifierLootCondition::new));

  @Override
  public MapCodec<? extends LootItemCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(LootContext context) {
    ItemInstance toolInstance = context.getOptionalParameter(LootContextParams.TOOL);
    ItemStack tool = toolInstance instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    if (tool.isEmpty()) {
      Player player = context.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER);
      if (player != null) {
        tool = player.getMainHandItem();
      }
    }
    return tool.is(TinkerTags.Items.MODIFIABLE) && ModifierUtil.getModifierLevel(tool, modifier) > 0;
  }
}
