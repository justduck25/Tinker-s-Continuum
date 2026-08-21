package slimeknights.tconstruct.shared.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.json.predicate.tool.ToolStackItemPredicate;

import java.util.Optional;

/** Advancement trigger matching a Tinkers tool stack in the player's inventory. */
public class ToolInventoryChangeTrigger extends SimpleCriterionTrigger<ToolInventoryChangeTrigger.Instance> {
  public static final ToolInventoryChangeTrigger TOOL_INVENTORY_CHANGED = new ToolInventoryChangeTrigger();
  @Override
  public Codec<Instance> codec() {
    return Instance.CODEC;
  }

  public void trigger(ServerPlayer player, ItemStack stack) {
    this.trigger(player, instance -> instance.matches(stack));
  }

  public record Instance(Optional<ContextAwarePredicate> player, ToolStackItemPredicate tool) implements SimpleInstance {
    public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
      ToolStackItemPredicate.CODEC.fieldOf("tool").forGetter(Instance::tool)
    ).apply(instance, Instance::new));

    public static Criterion<Instance> hasTool(ToolStackItemPredicate tool) {
      return TOOL_INVENTORY_CHANGED.createCriterion(new Instance(Optional.empty(), tool));
    }

    public boolean matches(ItemStack stack) {
      return tool.matches(stack);
    }
  }
}