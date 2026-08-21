package slimeknights.tconstruct.shared.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.Optional;

public class BlockContainerOpenedTrigger extends SimpleCriterionTrigger<BlockContainerOpenedTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, Block block) {
        this.trigger(player, instance -> instance.matches(block));
    }

    public record Instance(Optional<ContextAwarePredicate> player, Block block) implements SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Instance::block)
                )
                .apply(instance, Instance::new)
        );

        public static Criterion<Instance> container(Block block) {
            return CONTAINER.createCriterion(new Instance(Optional.empty(), block));
        }

        public static Criterion<Instance> container(BlockEntityType<?> type) {
            return container(type.getValidBlocks().iterator().next());
        }

        public boolean matches(Block block) {
            return this.block == block;
        }
    }

    public static final BlockContainerOpenedTrigger CONTAINER = new BlockContainerOpenedTrigger();
}
