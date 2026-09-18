package slimeknights.tconstruct.tools.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import slimeknights.mantle.util.CombatHelper;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.entity.ProjectileWithPower;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.shared.TinkerEffects;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;

import javax.annotation.Nullable;

/** Custom implementation of {@link net.minecraft.world.entity.projectile.SmallFireball} for the sake of modifiers. */
public class CustomFireball extends Fireball implements ProjectileWithPower {
  /** Damage type to deal from this projectile */
  private ResourceKey<DamageType> damageType = DamageTypes.FIREBALL;
  /** Damage type to deal when enderference is targeting a teleporting mob */
  private ResourceKey<DamageType> enderferenceType = DamageTypes.ON_FIRE;
  /** Amount of damage to deal */
  @Getter @Setter
  private float power = 2.5f;
  /** Damage multiplier on power. Separate from power for the sake of conditional power modules. */
  @Setter
  private float damageMultiplier = 2f;
  /** Remaining wall bounces. Slimeballs always start with one. */
  private int remainingBounces = 1;
  /** Tick we last bounced, so impact + onHit do not consume two bounces. */
  private int lastBounceTick = Integer.MIN_VALUE;
  /**
   * 1.20-style xPower/yPower/zPower: acceleration stays on the original heading after bounce,
   * so the slimeball veers left/right or up/down instead of reflecting like a thrown ball.
   */
  private Vec3 flightAcceleration = Vec3.ZERO;

  public CustomFireball(EntityType<? extends CustomFireball> type, Level level) {
    super(type, level);
  }

  public CustomFireball(Level level, LivingEntity shooter, double xOffset, double yOffset, double zOffset) {
    super(TinkerModifiers.fireball.get(), shooter, new Vec3(xOffset, yOffset, zOffset), level);
  }

  public CustomFireball(Level pLevel, double x, double y, double z, double xOffset, double yOffset, double zOffset) {
    super(TinkerModifiers.fireball.get(), x, y, z, new Vec3(xOffset, yOffset, zOffset), pLevel);
  }


  /* Behavior */

  @Override
  protected boolean shouldBurn() {
    return false;
  }

  @Override
  protected @Nullable ParticleOptions getTrailParticle() {
    return null;
  }

  @Override
  public boolean isPickable() {
    return false;
  }

  @Override
  protected Component getTypeName() {
    ItemStack stack = getItem();
    if (!stack.isEmpty()) {
      return stack.getHoverName();
    }
    return super.getTypeName();
  }


  /* Hitting */

  /** Sets the damage type on this projectile */
  public void setDamageType(ResourceKey<DamageType> damageType, ResourceKey<DamageType> enderferenceType) {
    this.damageType = damageType;
    this.enderferenceType = enderferenceType;
  }

  @Override
  public float getDamage() {
    return ProjectileWithPower.velocityScale(this, power * damageMultiplier);
  }

  @Override
  protected void onHitEntity(EntityHitResult hit) {
    super.onHitEntity(hit);

    // based on SmallFireball, uses custom damage type and power though
    if (!this.level().isClientSide()) {
      Entity target = hit.getEntity();
      Entity owner = this.getOwner();
      DamageSource source = CombatHelper.damageSource(TinkerEffects.needsEnderferenceOverride(target) ? enderferenceType : damageType, this, owner);
      if (target.hurtOrSimulate(source, getDamage()) && this.level() instanceof ServerLevel serverLevel) {
        EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, target, source, this.getItem());
      }
    }
  }

  @Override
  protected void onHit(HitResult pResult) {
    if (pResult.getType() == HitResult.Type.BLOCK && bounceOff((BlockHitResult) pResult)) {
      return;
    }
    super.onHit(pResult);
    if (!this.level().isClientSide()) {
      this.discard();
    }
  }

  /**
   * Matches 3.12 launch: xPower = normalize(look) * 0.1 * velocity, initial motion stays 0.
   * 26.1's constructor seeds motion at 0.1, which made the slimeball almost twice as fast on the first ticks.
   */
  public void initOriginalFlight(float velocity) {
    Vec3 direction = getDeltaMovement();
    if (direction.lengthSqr() < 1.0E-8) {
      direction = getLookAngle();
    }
    if (direction.lengthSqr() < 1.0E-8) {
      direction = new Vec3(0, 0, 1);
    }
    double accel = 0.1 * Math.max(velocity, 0.01f);
    flightAcceleration = direction.normalize().scale(accel);
    accelerationPower = 0.0;
    setDeltaMovement(Vec3.ZERO);
  }

  public void applyTinkersModifiers(ModifierNBT modifiers) {
    EntityModifierCapability.getCapability(this).setModifiers(modifiers);
    int bounceLevel = 0;
    for (ModifierEntry entry : modifiers.getModifiers()) {
      if (entry.matches(ModifierIds.bounce)) {
        bounceLevel += entry.getLevel();
      }
    }
    remainingBounces = bounceLevel <= 0 ? 1 : Math.max(1, -1 + 2 * bounceLevel);
  }

  /**
   * 3.12 bounce: reverse only the hit axis, keep the rest of the speed.
   * Flight acceleration stays on the original heading so the slimeball veers after the bounce.
   */
  public boolean bounceOff(BlockHitResult hit) {
    if (!isAlive()) {
      return false;
    }
    if (tickCount == lastBounceTick) {
      return true;
    }
    if (remainingBounces <= 0) {
      return false;
    }

    Vec3 motion = getDeltaMovement();
    Axis axis = hit.getDirection().getAxis();
    double amount = axis.choose(motion.x, motion.y, motion.z);
    lastBounceTick = tickCount;
    remainingBounces--;
    motion = motion.scale(0.9f).with(axis, amount * -1f);
    setDeltaMovement(motion);
    Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i());
    setPos(position().add(normal.scale(0.1)));
    setYRot((float)(Mth.atan2(motion.x, motion.z) * (180 / Math.PI)));
    setXRot((float)(Mth.atan2(motion.y, motion.horizontalDistance()) * (180 / Math.PI)));
    yRotO = getYRot();
    xRotO = getXRot();
    hurtMarked = true;
    needsSync = true;
    ModDataNBT persistentData = PersistentDataCapability.getOrWarn(this);
    Identifier bounceKey = ModifierIds.bounce.getId();
    persistentData.putInt(bounceKey, persistentData.getInt(bounceKey) + 1);
    if (!level().isClientSide()) {
      playSound(Sounds.SLIMY_BOUNCE.getSound());
    }
    return true;
  }


  /* Despawn */

  @Override
  public void tick() {
    // 1.20 fireball: add fixed heading acceleration, then vanilla 0.95 drag. Do not accelerate along current velocity.
    accelerationPower = 0.0;
    if (flightAcceleration.lengthSqr() > 0) {
      setDeltaMovement(getDeltaMovement().add(flightAcceleration));
    }
    super.tick();
    if (tickCount > 2400) {
      this.discard();
    }
  }

  @Override
  public void checkBelowWorld() {
    // despawn if going too high or low, otherwise projectile may live forever going into the sky
    double y = getY();
    Level level = level();
    if (y < (level.getMinY() - 64) || y > level.getMaxY() + 64) {
      onBelowWorld();
    }
  }


  /* NBT */
  private static final String TAG_POWER = "damage_power"; // "power" is taken by the velocity
  private static final String TAG_MULTIPLIER = "damage_multiplier";
  private static final String TAG_DAMAGE_TYPE = "damage_type";
  private static final String TAG_ENDERFERENCE_TYPE = "enderference_type";
  private static final String TAG_BOUNCES = "remaining_bounces";
  private static final String TAG_ACCEL_X = "flight_accel_x";
  private static final String TAG_ACCEL_Y = "flight_accel_y";
  private static final String TAG_ACCEL_Z = "flight_accel_z";

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.putFloat(TAG_POWER, power);
    output.putFloat(TAG_MULTIPLIER, damageMultiplier);
    output.putString(TAG_DAMAGE_TYPE, damageType.identifier().toString());
    output.putString(TAG_ENDERFERENCE_TYPE, enderferenceType.identifier().toString());
    output.putInt(TAG_BOUNCES, remainingBounces);
    output.putDouble(TAG_ACCEL_X, flightAcceleration.x);
    output.putDouble(TAG_ACCEL_Y, flightAcceleration.y);
    output.putDouble(TAG_ACCEL_Z, flightAcceleration.z);
  }

  /** Parses the given damage type */
  private static ResourceKey<DamageType> parseDamageType(String damageStr, ResourceKey<DamageType> fallback) {
    if (!damageStr.isEmpty()) {
      Identifier damageLoc = Identifier.tryParse(damageStr);
      if (damageLoc != null) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, damageLoc);
      }
    }
    return fallback;
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    power = input.getFloatOr(TAG_POWER, 2.5f);
    damageMultiplier = input.getFloatOr(TAG_MULTIPLIER, 2f);
    remainingBounces = input.getIntOr(TAG_BOUNCES, 1);
    flightAcceleration = new Vec3(
      input.getDoubleOr(TAG_ACCEL_X, 0),
      input.getDoubleOr(TAG_ACCEL_Y, 0),
      input.getDoubleOr(TAG_ACCEL_Z, 0));
    damageType = parseDamageType(input.getString(TAG_DAMAGE_TYPE).orElse(""), DamageTypes.FIREBALL);
    enderferenceType = parseDamageType(input.getString(TAG_ENDERFERENCE_TYPE).orElse(""), DamageTypes.ON_FIRE);
  }
}
