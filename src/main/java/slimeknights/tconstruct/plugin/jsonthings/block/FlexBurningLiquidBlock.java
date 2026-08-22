package slimeknights.tconstruct.plugin.jsonthings.block;

import dev.gigaherz.jsonthings.things.blocks.FlexLiquidBlock;
import dev.gigaherz.jsonthings.things.builders.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

/** Json Things version of {@link slimeknights.tconstruct.fluids.block.BurningLiquidBlock}. */
public class FlexBurningLiquidBlock extends FlexLiquidBlock {
  private final int burnTime;
  private final float damage;

  public FlexBurningLiquidBlock(Properties properties, BlockBuilder builder, FlowingFluid fluid, int burnTime, float damage) {
    super(properties, builder, fluid);
    this.burnTime = burnTime;
    this.damage = damage;
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean useShape) {
    if (!entity.fireImmune() && level.getFluidState(pos).is(fluid)) {
      entity.setRemainingFireTicks(burnTime * 20);
      entity.hurt(entity.damageSources().lava(), damage);
      entity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + level.getRandom().nextFloat() * 0.4F);
    }
  }
}
