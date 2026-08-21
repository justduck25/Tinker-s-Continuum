package slimeknights.tconstruct.library.modifiers.fluid.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext.Entity;

public class CureEffectsFluidEffect implements FluidEffect<Entity> {
  public static final RecordLoadable<CureEffectsFluidEffect> LOADER = RecordLoadable.create(
    ItemOutput.Loadable.REQUIRED_ITEM.requiredField("item", e -> e.stack),
    CureEffectsFluidEffect::new);

  private final ItemOutput stack;

  public CureEffectsFluidEffect(ItemOutput stack) {
    this.stack = stack;
  }

  public CureEffectsFluidEffect(ItemLike item) {
    this(ItemOutput.fromItem(item));
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, Entity context, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction action) {
    LivingEntity target = context.getLivingTarget();
    if (target != null && level.isFull()) {
      if (action.simulate()) {
        return target.getActiveEffects().isEmpty() ? 0 : 1;
      }
      target.removeAllEffects();
      return 1;
    }
    return 0;
  }

  @Override
  public RecordLoadable<CureEffectsFluidEffect> getLoader() {
    return LOADER;
  }
}
