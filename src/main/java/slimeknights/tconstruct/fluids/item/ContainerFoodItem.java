package slimeknights.tconstruct.fluids.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import slimeknights.tconstruct.fluids.util.ConstantFluidContainerWrapper;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ContainerFoodItem extends Item {
  public ContainerFoodItem(Properties props) {
    super(props);
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return 32;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack stack) {
    return ItemUseAnimation.DRINK;
  }

  /** Adds effects to the tooltip */
  public static void addEffectTooltip(Consumable consumable, Consumer<Component> tooltip) {
    for (ConsumeEffect effect : consumable.onConsumeEffects()) {
      if (effect instanceof ApplyStatusEffectsConsumeEffect statusEffect) {
        for (MobEffectInstance instance : statusEffect.effects()) {
          MutableComponent mutable = Component.translatable(instance.getDescriptionId());
          if (instance.getAmplifier() > 0) {
            mutable = Component.translatable("potion.withAmplifier", mutable, Component.translatable("potion.potency." + instance.getAmplifier()));
          }
          if (instance.getDuration() > 20) {
            mutable = Component.translatable("potion.withDuration", mutable, MobEffectUtil.formatDuration(instance, 1.0f, 20.0f));
          }
          tooltip.accept(mutable.withStyle(instance.getEffect().value().getCategory().getTooltipFormatting()));
        }
      }
    }
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    Consumable consumable = stack.get(DataComponents.CONSUMABLE);
    if (consumable != null) {
      addEffectTooltip(consumable, tooltip);
    }
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
    ItemStack container = remainder.create();
    ItemStack result = super.finishUsingItem(stack, level, living);
    Player player = living instanceof Player p ? p : null;
    if (player == null || !player.getAbilities().instabuild) {
      container = container.copy();
      if (result.isEmpty()) {
        return container;
      }
      if (player != null) {
        if (!player.getInventory().add(container)) {
          player.drop(container, false);
        }
      }
    }
    return result;
  }

  public static class FluidContainerFoodItem extends ContainerFoodItem {
    private final Supplier<FluidStack> fluid;
    public FluidContainerFoodItem(Properties props, Supplier<FluidStack> fluid) {
      super(props);
      this.fluid = fluid;
    }

    public ResourceHandler<FluidResource> createFluidHandler(ItemAccess access) {
      return new ConstantFluidContainerWrapper(access.oneByOne(), fluid.get(), getCraftingRemainder().create());
    }
  }
}
