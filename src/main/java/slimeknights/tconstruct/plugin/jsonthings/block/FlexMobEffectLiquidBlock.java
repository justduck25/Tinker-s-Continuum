package slimeknights.tconstruct.plugin.jsonthings.block;

import dev.gigaherz.jsonthings.things.blocks.FlexLiquidBlock;
import dev.gigaherz.jsonthings.things.builders.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

/** Json Things version of the TConstruct mob-effect liquid block. */
public class FlexMobEffectLiquidBlock extends FlexLiquidBlock {
  private final Supplier<MobEffectInstance> effect;

  public FlexMobEffectLiquidBlock(Properties properties, BlockBuilder builder, FlowingFluid fluid, Supplier<MobEffectInstance> effect) {
    super(properties, builder, fluid);
    this.effect = effect;
  }

  @Override
  protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean useShape) {
    if (level.getFluidState(pos).is(fluid) && entity instanceof LivingEntity living) {
      living.addEffect(this.effect.get());
    }
  }
}
