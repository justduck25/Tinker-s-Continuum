package slimeknights.tconstruct.fluids.fluids;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.fluid.InvertedFluidType;
import slimeknights.mantle.fluid.TextureFluidType;
import slimeknights.tconstruct.common.TinkerTags;

/** Fluid type that does not drown slimes. */
public class SlimeFluidType extends TextureFluidType {
  public SlimeFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public boolean canDrownIn(LivingEntity entity) {
    return !entity.getType().builtInRegistryHolder().is(TinkerTags.EntityTypes.SLIMES);
  }

  public static class Inverted extends InvertedFluidType {
    public Inverted(Properties properties) {
      super(properties);
    }

    @Override
    public boolean canDrownIn(LivingEntity entity) {
      return !entity.getType().builtInRegistryHolder().is(TinkerTags.EntityTypes.SLIMES);
    }
  }
}
