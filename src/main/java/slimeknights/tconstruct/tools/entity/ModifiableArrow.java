package slimeknights.tconstruct.tools.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.entity.ReusableProjectile;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ScheduledProjectileTaskModifierHook;
import slimeknights.tconstruct.library.tools.IndestructibleItemEntity;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.Schedule;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.data.ModifierIds;

import javax.annotation.Nullable;

/** Arrow with material variants */
public class ModifiableArrow extends AbstractArrow implements ToolProjectile, ReusableProjectile {
  /** Key to sync the stack to the client */
  protected static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(ModifiableArrow.class, EntityDataSerializers.ITEM_STACK);
  /** Movement speed in water */
  protected static final EntityDataAccessor<Float> WATER_INERTIA = SynchedEntityData.defineId(ModifiableArrow.class, EntityDataSerializers.FLOAT);

  private ItemStack stack = ItemStack.EMPTY;
  private IToolStackView tool = null;
  private boolean reclaim = false;
  private boolean dealtDamage = false;
  private boolean consumedPickup = false;
  private int knockback = 0;
  private byte extraPierce = 0;
  /** Tasks queued by modifiers */
  private Schedule tasks = Schedule.EMPTY;
  /** Color of the tipped potion trail, or -1 if untipped. */
  private int potionColor = -1;

  public ModifiableArrow(EntityType<? extends AbstractArrow> type, Level level) {
    super(type, level);
  }

  public ModifiableArrow(Level level, double pX, double pY, double pZ) {
    super(TinkerTools.materialArrow.get(), pX, pY, pZ, level, ItemStack.EMPTY, null);
  }

  public ModifiableArrow(Level level, LivingEntity shooter) {
    super(TinkerTools.materialArrow.get(), shooter, level, ItemStack.EMPTY, null);
  }


  /* Stack */

  @Override
  public ItemStack getPickupItem() {
    return stack.copy();
  }

  /** Updates the stack on the arrow */
  private void setStack(ItemStack stack) {
    this.stack = stack;
    this.entityData.set(STACK, stack);
    this.reclaim = ModifierUtil.checkVolatileFlag(stack, IndestructibleItemEntity.INDESTRUCTIBLE_ENTITY);
  }

  /** Gets the tool instance, ensuring its created */
  private IToolStackView getTool() {
    if (tool == null) {
      tool = ToolStack.from(stack);
    }
    return tool;
  }

  /** Gets potion contents from the tipped modifier data. */
  private PotionContents getPotionContents(IToolStackView tool) {
    String potionId = tool.getPersistentData().getString(ModifierIds.tipped.getId());
    if (!potionId.isEmpty()) {
      Identifier id = Identifier.tryParse(potionId);
      if (id != null) {
        Potion potion = BuiltInRegistries.POTION.getValue(id);
        if (potion != null) {
          return new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion));
        }
      }
    }
    return PotionContents.EMPTY;
  }

  /** Updates the cached potion color for the clientside trail. */
  private void updatePotionColor(IToolStackView tool) {
    PotionContents contents = getPotionContents(tool);
    potionColor = contents.equals(PotionContents.EMPTY) ? -1 : contents.getColor();
  }

  /** Adds vanilla-style tipped arrow particles. */
  private void makePotionParticles(int count) {
    if (potionColor != -1 && count > 0) {
      for (int i = 0; i < count; i++) {
        this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, potionColor), getRandomX(0.75), getRandomY(), getRandomZ(0.75), 0, 0, 0);
      }
    }
  }

  /** If true, render using the vanilla tipped arrow texture. */
  public boolean hasPotionEffects() {
    return potionColor != -1;
  }

  /**
   * Called when the arrow is created to set initial properties.
   * @see ThrownShuriken#onCreate(ItemStack, LivingEntity)
   */
  public IToolStackView onCreate(ItemStack stack, @Nullable LivingEntity shooter) {
    stack = stack.copyWithCount(1);
    setStack(stack);
    setPickupItemStack(stack);
    // initialize arrow stats
    IToolStackView tool = getTool();
    updatePotionColor(tool);
    EntityModifierCapability.getCapability(this).addModifiers(tool.getModifiers());
    setBaseDamage(ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.PROJECTILE_DAMAGE));
    this.entityData.set(WATER_INERTIA, ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.WATER_INERTIA));
    return tool;
  }

  /** @see ThrownShuriken#shoot(double, double, double, float, float)  */
  @Override
  public void shoot(double pX, double pY, double pZ, float velocity, float inaccuracy) {
    if (!stack.isEmpty()) {
      IToolStackView tool = getTool();
      // apply accuracy, no need to compute this earlier nor store it
      LivingEntity shooter = ModifierUtil.asLiving(getOwner());
      velocity *= ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.VELOCITY);
      inaccuracy *= ModifierUtil.getInaccuracy(tool, shooter);

      // shoot with new information
      super.shoot(pX, pY, pZ, velocity, inaccuracy);

      // run modifier hooks from the arrow's perspective
      ModDataNBT arrowData = PersistentDataCapability.getOrWarn(this);
      for (ModifierEntry entry : tool.getModifiers()) {
        entry.getHook(ModifierHooks.PROJECTILE_SHOT).onProjectileShoot(tool, entry, shooter, stack, this, this, arrowData, true);
      }

      // schedule tasks
      this.tasks = ScheduledProjectileTaskModifierHook.createSchedule(tool, stack, this, this, arrowData);
    } else {
      super.shoot(pX, pY, pZ, velocity, inaccuracy);
    }
  }

  @Override
  public void tick() {
    super.tick();
    if (level().isClientSide()) {
      if (isInGround()) {
        if (inGroundTime % 5 == 0) {
          makePotionParticles(1);
        }
      } else {
        makePotionParticles(4);
      }
    }
    // check if any tasks are ready
    if (!tasks.isEmpty() && !stack.isEmpty()) {
      ScheduledProjectileTaskModifierHook.checkSchedule(getTool(), stack, this, this, tasks);
    }
  }

  /* Stats */

  @Override
  protected float getWaterInertia() {
    return entityData.get(WATER_INERTIA);
  }

  // need to replace some setters with adders so vanilla bows work with our logic

  public void setKnockback(int knockback) {
    this.knockback += knockback;
  }

  public void setPierceLevel(byte pierceLevel) {
    this.extraPierce += pierceLevel;
  }

  @Override
  public byte getPierceLevel() {
    return (byte)(super.getPierceLevel() + extraPierce);
  }

  @Override
  public void doKnockback(LivingEntity mob, DamageSource damageSource) {
    super.doKnockback(mob, damageSource);
    if (knockback > 0) {
      double resistance = Math.max(0, 1 - mob.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
      Vec3 motion = this.getDeltaMovement().multiply(1, 0, 1).normalize().scale(knockback * 0.6 * resistance);
      if (motion.lengthSqr() > 0) {
        mob.push(motion.x, 0.1f, motion.z);
      }
    }
  }

  @Override
  public void doPostHurtEffects(LivingEntity mob) {
    super.doPostHurtEffects(mob);
    IToolStackView tool = getTool();
    if (tool.getModifierLevel(ModifierIds.tipped) > 0) {
      int divisor = 1 << Math.max(4 - tool.getModifierLevel(ModifierIds.tipped), 0);
      getPotionContents(tool).forEachEffect(effect -> mob.addEffect(effect, getEffectSource()), 1f / divisor);
    }
  }

  /* Despawn */

  @Override
  public boolean isReusable() {
    return reclaim;
  }

  @Override
  public void tickDespawn() {
    // if we can pick up the arrows, don't despawn with worldbound
    if (pickup != Pickup.ALLOWED || !reclaim) {
      super.tickDespawn();
    }
  }

  private enum CaptureDiscard { NOT_CAPTURING,  CAPTURING,  DISCARDED }
  private CaptureDiscard captureDiscard = CaptureDiscard.NOT_CAPTURING;

  @Override
  protected void onHitEntity(EntityHitResult result) {
    if (reclaim) {
      // prevent the entity from being discarded for a bit
      captureDiscard = CaptureDiscard.CAPTURING;
    }

    super.onHitEntity(result);
    if (!reclaim) {
      consumedPickup = true;
    }

    // if we tried to discard it, back off the movement and mark it to prevent further damage
    if (captureDiscard == CaptureDiscard.DISCARDED) {
      dealtDamage = true;
      setDeltaMovement(getDeltaMovement().multiply(-0.01, -0.1, -0.01));
    }
    captureDiscard = CaptureDiscard.NOT_CAPTURING;
  }

  @Override
  public void remove(RemovalReason reason) {
    // capturing is used for worldbound to keep the ammo around after hit
    // however, there is a single case where we don't want to stick around, and that is when we failed to hit a target and the movement is now too small
    if (reason == RemovalReason.DISCARDED && captureDiscard != CaptureDiscard.NOT_CAPTURING && getDeltaMovement().lengthSqr() >= 1.0E-7D) {
      captureDiscard = CaptureDiscard.DISCARDED;
    } else {
      super.remove(reason);
    }
  }

  @Override
  @Nullable
  protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
    return this.dealtDamage ? null : super.findHitEntity(pStartVec, pEndVec);
  }

  @Override
  protected boolean tryPickup(Player player) {
    return !consumedPickup && (!dealtDamage || reclaim) && super.tryPickup(player);
  }


  /* Client */

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(STACK, ItemStack.EMPTY);
    builder.define(WATER_INERTIA, 0.6f);
  }

  @Override
  public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
    super.onSyncedDataUpdated(key);
    if (STACK.equals(key)) {
      this.stack = this.entityData.get(STACK);
      this.tool = null;
      updatePotionColor(getTool());
    }
  }

  @Override
  protected ItemStack getDefaultPickupItem() {
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack getDisplayTool() {
    ItemStack display = this.entityData.get(STACK);
    return display.isEmpty() ? this.stack : display;
  }

  @Override
  public Component getDisplayName() {
    return getDisplayTool().getDisplayName();
  }


  /* NBT */
  private static final String KEY_STACK = "stack";
  private static final String KEY_WATER_INERTIA = "water_inertia";
  private static final String KEY_DEALT_DAMAGE = "dealt_damage";
  private static final String KEY_CONSUMED_PICKUP = "consumed_pickup";
  private static final String KEY_TASKS = "tasks";

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.store(KEY_STACK, ItemStack.CODEC, this.stack);
    output.putFloat(KEY_WATER_INERTIA, this.entityData.get(WATER_INERTIA));
    output.putBoolean(KEY_DEALT_DAMAGE, dealtDamage);
    output.putBoolean(KEY_CONSUMED_PICKUP, consumedPickup);
    output.putInt("knockback", knockback);
    output.putByte("extra_pierce", extraPierce);
    if (!this.tasks.isEmpty()) {
      CompoundTag tasksTag = new CompoundTag();
      tasksTag.put(KEY_TASKS, this.tasks.serialize());
      output.store(KEY_TASKS, CompoundTag.CODEC, tasksTag);
    }
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    input.read(KEY_STACK, ItemStack.CODEC).ifPresent(stack -> {
      setStack(stack);
      setPickupItemStack(stack);
      updatePotionColor(getTool());
    });
    this.entityData.set(WATER_INERTIA, input.getFloatOr(KEY_WATER_INERTIA, 0.6f));
    this.dealtDamage = input.getBooleanOr(KEY_DEALT_DAMAGE, false);
    this.consumedPickup = input.getBooleanOr(KEY_CONSUMED_PICKUP, false);
    this.knockback = input.getIntOr("knockback", 0);
    this.extraPierce = input.getByteOr("extra_pierce", (byte)0);
    input.read(KEY_TASKS, CompoundTag.CODEC).map(tag -> tag.getListOrEmpty(KEY_TASKS)).ifPresent(list -> this.tasks = Schedule.deserialize(list));
  }
}
