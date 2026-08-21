package slimeknights.tconstruct.gadgets.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import slimeknights.mantle.item.TooltipItem;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerEffect;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.gadgets.capability.PiggybackCapability;
import slimeknights.tconstruct.gadgets.capability.PiggybackHandler;

public class PiggyBackPackItem extends TooltipItem {
  private static final int MAX_ENTITY_STACK = 3;

  public PiggyBackPackItem(Properties props) {
    super(props);
  }

  @Override
  public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
    if (player.level().isClientSide()) {
      return InteractionResult.SUCCESS;
    }

    ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
    if (chestArmor.getItem() != this && player.getInventory().getFreeSlot() == -1) {
      return InteractionResult.PASS;
    }

    if (pickupEntity(player, target)) {
      if (chestArmor.getItem() != this) {
        if (!chestArmor.isEmpty()) {
          player.getInventory().placeItemBackInInventory(chestArmor);
        }
        chestArmor = ItemStack.EMPTY;
      }
      if (chestArmor.isEmpty()) {
        player.setItemSlot(EquipmentSlot.CHEST, stack.split(1));
      } else if (chestArmor.getCount() < this.getEntitiesCarriedCount(player)) {
        stack.split(1);
        chestArmor.grow(1);
      }
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.CONSUME;
  }

  public static InteractionResult useWornBackpack(Player player, LivingEntity target) {
    ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
    if (chestArmor.getItem() != TinkerGadgets.piggyBackpack.get()) {
      return InteractionResult.PASS;
    }
    if (player.level().isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    return pickupEntity(player, target) ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
  }

  private static boolean isVehicle(Entity entity, Entity possibleVehicle) {
    for (Entity vehicle = entity.getVehicle(); vehicle != null; vehicle = vehicle.getVehicle()) {
      if (vehicle == possibleVehicle) {
        return true;
      }
    }
    return false;
  }

  private static boolean pickupEntity(Player player, Entity target) {
    if (player.level().isClientSide() || target.typeHolder().is(TinkerTags.EntityTypes.PIGGYBACKPACK_BLACKLIST)) {
      return false;
    }
    if (isVehicle(player, target) || isVehicle(target, player)) {
      return false;
    }

    int count = 0;
    Entity toRide = player;
    while (toRide.isVehicle() && count < MAX_ENTITY_STACK) {
      toRide = toRide.getPassengers().get(0);
      count++;
      if (toRide instanceof Player && target instanceof Player) {
        return false;
      }
    }

    if (!toRide.isVehicle() && count < MAX_ENTITY_STACK) {
      boolean mounted = target.startRiding(toRide, true, true);
      if (!mounted && toRide instanceof Player) {
        mounted = forcePlayerMount(target, toRide);
      }
      if (mounted) {
        if (player instanceof ServerPlayer serverPlayer) {
          TinkerNetwork.getInstance().sendVanillaPacket(serverPlayer, new ClientboundSetPassengersPacket(serverPlayer));
        }
        return true;
      }
    }
    return false;
  }

  private static boolean forcePlayerMount(Entity target, Entity toRide) {
    if (!EventHooks.canMountEntity(target, toRide, true)) {
      return false;
    }
    if (target.isPassenger()) {
      target.stopRiding();
    }
    target.setPose(Pose.STANDING);
    target.vehicle = toRide;
    toRide.addPassenger(target);
    target.level().gameEvent(target, GameEvent.ENTITY_MOUNT, toRide.position());
    if (toRide instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.START_RIDING_TRIGGER.trigger(serverPlayer);
    }
    return true;
  }

  private int getEntitiesCarriedCount(LivingEntity player) {
    int count = 0;
    Entity ridden = player;
    while (ridden.isVehicle()) {
      count++;
      ridden = ridden.getPassengers().get(0);
    }
    return count;
  }

  public void matchCarriedEntitiesToCount(LivingEntity player, int maxCount) {
    int count = 0;
    Entity ridden = player;
    while (ridden.isVehicle()) {
      ridden = ridden.getPassengers().get(0);
      count++;
      if (count > maxCount) {
        dropCarriedEntity(player, ridden);
      }
    }
  }

  private static void dropCarriedEntity(LivingEntity carrier, Entity carried) {
    Vec3 look = carrier.getLookAngle();
    Vec3 drop = carrier.position().add(look.x * 0.8D, 0.0D, look.z * 0.8D);
    carried.stopRiding();
    carried.teleportTo(drop.x, carrier.getY(), drop.z);
    carried.setDeltaMovement(Vec3.ZERO);
    carried.hurtMarked = true;
  }

  @Override
  public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
    if (slot == EquipmentSlot.CHEST && entity instanceof LivingEntity livingEntity && livingEntity.getItemBySlot(EquipmentSlot.CHEST) == stack && entity.isVehicle()) {
      int amplifier = this.getEntitiesCarriedCount(livingEntity) - 1;
      livingEntity.addEffect(new MobEffectInstance(TinkerGadgets.carryEffect, 2, amplifier, true, false, true));
    }
  }

  public static class CarryPotionEffect extends TinkerEffect {
    private static final Identifier ATTRIBUTE_ID = TConstruct.getResource("carry");

    public CarryPotionEffect() {
      super(MobEffectCategory.NEUTRAL, true);
      this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ATTRIBUTE_ID, -0.05D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
      ItemStack chestArmor = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
      if (chestArmor.isEmpty() || chestArmor.getItem() != TinkerGadgets.piggyBackpack.get()) {
        TinkerGadgets.piggyBackpack.get().matchCarriedEntitiesToCount(livingEntity, 0);
      } else {
        TinkerGadgets.piggyBackpack.get().matchCarriedEntitiesToCount(livingEntity, chestArmor.getCount());
        PiggybackHandler handler = livingEntity.getCapability(PiggybackCapability.PIGGYBACK);
        if (handler != null) {
          handler.updatePassengers();
        }
      }
    }
  }
}
