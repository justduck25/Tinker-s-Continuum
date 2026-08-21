package slimeknights.tconstruct.fluids.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Implements filling a bucket with an NBT fluid */
public class PotionBucketItem extends PotionItem {
  private final Supplier<? extends Fluid> supplier;
  public PotionBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
    super(builder);
    this.supplier = supplier;
  }

  public Fluid getFluid() {
    return supplier.get();
  }

  public String getDescriptionId(ItemStack stack) {
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    String bucketKey = contents.getName(getDescriptionId() + ".effect.").getString();
    if (Util.canTranslate(bucketKey)) {
      return bucketKey;
    }
    return super.getDescriptionId();
  }

  @Override
  public Component getName(ItemStack stack) {
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    String bucketKey = contents.getName(getDescriptionId() + ".effect.").getString();
    if (Util.canTranslate(bucketKey)) {
      return Component.translatable(bucketKey);
    }
    return Component.translatable(getDescriptionId() + ".contents", contents.getName("item.minecraft.potion.effect."));
  }

  @Override
  public ItemStack getDefaultInstance() {
    return PotionContents.createItemStack(this, Potions.AWKWARD);
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    player.startUsingItem(hand);
    return InteractionResult.CONSUME;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack stack) {
    return ItemUseAnimation.DRINK;
  }
  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    Player player = living instanceof Player p ? p : null;
    if (player instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
    }

    // effects are 2x duration
    if (!level.isClientSide()) {
      PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
      for (MobEffectInstance effect : contents.getAllEffects()) {
        if (effect.getEffect().value().isInstantenous()) {
          effect.getEffect().value().applyInstantenousEffect((ServerLevel) level, player, player, living, effect.getAmplifier(), 2.5D);
        } else {
          MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(), (int)((long)effect.getDuration() * 5 / 2), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
          living.addEffect(newEffect);
        }
      }
    }

    if (player != null) {
      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }
    }

    if (player == null || !player.getAbilities().instabuild) {
      if (stack.isEmpty()) {
        return new ItemStack(Items.BUCKET);
      }
      if (player != null) {
        player.getInventory().add(new ItemStack(Items.BUCKET));
      }
    }
    living.gameEvent(GameEvent.DRINK);
    return stack;
  }

  @Override
  public void appendHoverText(ItemStack pStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipConsumer, TooltipFlag pFlag) {
    PotionContents contents = pStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    PotionContents.addPotionTooltip(contents.getAllEffects(), tooltipConsumer, 2.5f, context.tickRate());
  }

  @Override
  public int getUseDuration(ItemStack pStack, LivingEntity entity) {
    return 96; // 3x duration of potion bottles
  }

  private FluidStack getFluidStack(ItemStack stack) {
    FluidStack fluid = new FluidStack(getFluid(), FluidType.BUCKET_VOLUME);
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    if (!contents.equals(PotionContents.EMPTY)) {
      fluid.set(DataComponents.POTION_CONTENTS, contents);
    }
    return fluid;
  }

  /** NeoForge 26 item transfer capability for potion buckets, replacing Forge's old FluidBucketWrapper. */
  public static class PotionBucketResourceHandler extends ItemAccessResourceHandler<FluidResource> {
    public PotionBucketResourceHandler(ItemAccess itemAccess) {
      super(itemAccess, 1);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
      Objects.checkIndex(index, size());
      if (accessResource.getItem() instanceof PotionBucketItem bucket) {
        return FluidResource.of(bucket.getFluidStack(accessResource.toStack()));
      }
      return FluidResource.EMPTY;
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
      return getResourceFrom(accessResource, index).isEmpty() ? 0 : FluidType.BUCKET_VOLUME;
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
      Objects.checkIndex(index, size());
      if (newAmount == 0) {
        return ItemResource.of(Items.BUCKET);
      }
      if (newAmount != FluidType.BUCKET_VOLUME || !(accessResource.getItem() instanceof PotionBucketItem bucket) || newResource.getFluid() != bucket.getFluid()) {
        return ItemResource.EMPTY;
      }
      FluidStack stack = newResource.toStack(newAmount);
      return ItemResource.of(stack.getFluidType().getBucket(stack));
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
      Objects.checkIndex(index, size());
      return !resource.isEmpty() && itemAccess.getResource().getItem() instanceof PotionBucketItem bucket && resource.getFluid() == bucket.getFluid();
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
      Objects.checkIndex(index, size());
      return FluidType.BUCKET_VOLUME;
    }
  }
}
