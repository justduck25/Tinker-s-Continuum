package slimeknights.tconstruct.gadgets.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;
import java.util.function.Consumer;

public class FoodCakeBlock extends CakeBlock {
  private final FoodProperties food;
  private final EffectCombination combination;
  private final List<FoodEffect> effects;

  public FoodCakeBlock(Properties properties, FoodProperties food, EffectCombination combination, List<FoodEffect> effects) {
    super(properties);
    this.food = food;
    this.combination = combination;
    this.effects = effects;
  }

  @Deprecated(forRemoval = true)
  public FoodCakeBlock(Properties properties, FoodProperties food) {
    this(properties, food, EffectCombination.BLOCK, List.of());
  }

  public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable net.minecraft.world.level.BlockGetter pLevel, List<Component> tooltip, TooltipFlag pFlag) {
    for (FoodEffect pair : effects) {
      MobEffectInstance effect = pair.effect();
      if (effect != null) {
        tooltip.add(Component.translatable(effect.getDescriptionId()));
      }
    }
  }

  @Override
  public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
    InteractionResult result = this.eatSlice(world, pos, state, player);
    if (result.consumesAction()) {
      return result;
    }
    if (world.isClientSide()) {
      return InteractionResult.CONSUME;
    }
    return InteractionResult.PASS;
  }

  private boolean hasAllEffects(Player player) {
    for (FoodEffect pair : effects) {
      if (pair.effect() != null) {
        MobEffectInstance current = player.getEffect(pair.effect().getEffect());
        if (current == null || current.getDuration() < 100) {
          return false;
        }
      }
    }
    return true;
  }

  private InteractionResult eatSlice(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
    if (!player.canEat(false) && !food.canAlwaysEat()) {
      return InteractionResult.PASS;
    }
    if (combination == EffectCombination.BLOCK && hasAllEffects(player)) {
      return InteractionResult.PASS;
    }
    player.awardStat(Stats.EAT_CAKE_SLICE);
    player.getFoodData().eat(food.nutrition(), food.saturation());
    for (FoodEffect pair : effects) {
      if (!world.isClientSide() && pair.effect() != null && world.getRandom().nextFloat() < pair.probability()) {
        MobEffectInstance effect = new MobEffectInstance(pair.effect());
        if (combination == EffectCombination.ADD) {
          MobEffectInstance current = player.getEffect(effect.getEffect());
          if (current != null && current.getAmplifier() == effect.getAmplifier()) {
            effect = new MobEffectInstance(effect.getEffect(), (int)(effect.getDuration() + current.getDuration()), effect.getAmplifier());
          }
        }
        player.addEffect(effect);
      }
    }
    int i = state.getValue(BITES);
    if (i < 6) {
      world.setBlock(pos, state.setValue(BITES, i + 1), 3);
    } else {
      world.removeBlock(pos, false);
    }
    return InteractionResult.SUCCESS;
  }

  public record FoodEffect(MobEffectInstance effect, float probability) {}

  public enum EffectCombination {
    SET,
    ADD,
    BLOCK
  }
}
