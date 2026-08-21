package slimeknights.tconstruct.fluids.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/** Magma bottle instance, which lights the drinker on fire */
public class MagmaBottleItem extends Item {
  private final int fireTime;
  public MagmaBottleItem(Properties props, int fireTime) {
    super(props);
    this.fireTime = fireTime;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, display, tooltip, flag);
    Component duration = MobEffectUtil.formatDuration(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, fireTime * 20), 1.0F, 20.0F);
    tooltip.accept(Component.translatable(
      "potion.withDuration",
      net.minecraft.world.level.block.Blocks.FIRE.getName(),
      duration
    ).withStyle(MobEffectCategory.HARMFUL.getTooltipFormatting()));
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    player.startUsingItem(hand);
    return InteractionResult.CONSUME;
  }

  @Override
  public int getUseDuration(ItemStack pStack, LivingEntity entity) {
    return 32;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack pStack) {
    return ItemUseAnimation.DRINK;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    living.setRemainingFireTicks(fireTime * 20);
    ItemStack container = stack.getCraftingRemainder().create();
    Player player = living instanceof Player p ? p : null;
    if (player == null || !player.getAbilities().instabuild) {
      stack.shrink(1);
      container = container.copy();
      if (stack.isEmpty()) {
        return container;
      }
      if (player != null) {
        if (!player.getInventory().add(container)) {
          player.drop(container, false);
        }
      }
    }
    return stack;
  }
}
