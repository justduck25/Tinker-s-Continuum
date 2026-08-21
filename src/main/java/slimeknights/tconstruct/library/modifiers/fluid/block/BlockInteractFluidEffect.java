package slimeknights.tconstruct.library.modifiers.fluid.block;

import lombok.Getter;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
//import net.neoforged.bus.api.Event.Result;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;

/** Fluid effect causing a ranged block interaction */
@Getter
public enum BlockInteractFluidEffect implements FluidEffect<FluidEffectContext.Block> {
  INSTANCE;

  private final SingletonLoader<BlockInteractFluidEffect> loader = new SingletonLoader<>(this);

  /** Damages the stack in the context if needed */
  private static void damageIfNeeded(UseOnContext context) {
    ItemStack stack = context.getItemInHand();
    Level level = context.getLevel();
    // vanilla tools tend not to call the proper damage methods if player is null, so just manually damage the stack
    // we expect modded items will have the same bug, so just go ahead and damage them. On the chance it works, they get 2 damage, no big deal
    // our tools we know work so ignore them
    if (!level.isClientSide() && context.getPlayer() == null && stack.isDamageableItem() && !stack.is(TinkerTags.Items.MODIFIABLE)) {
      // unable to call Forge damageItem as that needs entity access, but its just vanilla broken anyways, right?
      stack.setDamageValue(stack.getDamageValue() + 1);
      // calling methods again instead of using return as return may be incorrect for custom broken stacks
      if (stack.getDamageValue() >= stack.getMaxDamage()) {
        // but that won't happen, right? will need to consider another workaround in that case.
        stack.shrink(1);
        stack.setDamageValue(0);
        level.playSound(null, context.getClickedPos(), SoundEvents.ITEM_BREAK.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }
    }
  }

  /** Based on {@link net.minecraft.server.level.ServerPlayerGameMode#useItemOn(ServerPlayer, Level, ItemStack, InteractionHand, BlockHitResult)} */
  @Override
  public float apply(FluidStack fluid, EffectLevel level, FluidEffectContext.Block context, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction action) {
    Level world = context.getLevel();
    BlockPos pos = context.getBlockPos();
    if (!world.getWorldBorder().isWithinBounds(pos)) {
      return 0;
    }
    BlockState state = context.getBlockState();
    if (!state.getBlock().isEnabled(world.enabledFeatures())) {
      return 0;
    }
    if (action.simulate()) {
      return 1;
    }
    LivingEntity entity = context.getEntity();
    Player player = context.getPlayer();
    BlockHitResult hitResult = context.getHitResult();
    for (InteractionHand hand : entity == null ? new InteractionHand[] {InteractionHand.MAIN_HAND} : InteractionHand.values()) {
      ItemStack heldItem = entity == null ? context.getStack() : entity.getItemInHand(hand);
      if (!heldItem.isItemEnabled(world.enabledFeatures())) {
        return 0;
      }
      UseOnContext useContext = new UseOnContext(world, player, hand, heldItem, hitResult);
      if (!heldItem.isEmpty()) {
        InteractionResult result = heldItem.onItemUseFirst(useContext);
        if (result != InteractionResult.PASS) {
          if (result.consumesAction()) {
            if (entity != null) {
              entity.swing(hand, true);
            }
            damageIfNeeded(useContext);
            return 1;
          }
          return 0;
        }
      }
      if (player != null && !player.isSecondaryUseActive()) {
        InteractionResult result = state.useWithoutItem(world, player, hitResult);
        if (result.consumesAction()) {
          if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, heldItem);
          }
          player.swing(hand, true);
          return 1;
        }
      }
      InteractionResult result = heldItem.useOn(useContext);
      if (result != InteractionResult.PASS) {
        if (result.consumesAction()) {
          if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, heldItem);
          }
          if (entity != null) {
            entity.swing(hand, true);
          }
          if (player != null && !player.isCreative()) {
            damageIfNeeded(useContext);
          }
          return 1;
        }
        return 0;
      }
    }
    return 0;
  }

  @Override
  public Component getDescription(RegistryAccess registryAccess) {
    return Component.translatable(FluidEffect.getTranslationKey(getLoader()) + ".block");
  }
}
