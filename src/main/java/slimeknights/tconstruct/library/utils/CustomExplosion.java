package slimeknights.tconstruct.library.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class CustomExplosion implements Explosion {
  private static final int RAY_COUNT = 16;
  private static final int MAX_RAY = RAY_COUNT - 1;

  protected final ServerLevel level;
  protected final Vec3 center;
  protected final float radius;
  @Nullable
  protected final Entity source;
  protected final DamageSource damageSource;
  protected final ExplosionDamageCalculator damageCalculator;
  protected final boolean fire;
  protected final BlockInteraction blockInteraction;
  protected final float damage;
  protected final float knockback;
  protected final Predicate<Entity> entityPredicate;
  protected final boolean bypassInvulnerableTime;
  protected final Set<BlockPos> toBlow = new HashSet<>();

  public CustomExplosion(ServerLevel level, Vec3 center, float radius, @Nullable Entity sourceEntity,
                         @Nullable Predicate<Entity> entityPredicate, float damage, @Nullable DamageSource damageSource,
                         float knockback, @Nullable ExplosionDamageCalculator damageCalculator,
                         boolean placeFire, BlockInteraction blockInteraction, boolean bypassInvulnerableTime) {
    this.level = level;
    this.center = center;
    this.radius = radius;
    this.source = sourceEntity;
    this.damageSource = damageSource == null ? level.damageSources().explosion(this) : damageSource;
    this.damageCalculator = damageCalculator == null ? new ExplosionDamageCalculator() : damageCalculator;
    this.fire = placeFire;
    this.blockInteraction = blockInteraction;
    this.entityPredicate = Objects.requireNonNullElse(entityPredicate, e -> e != null && e.isAlive() && !e.ignoreExplosion(this) && !e.isSpectator());
    this.damage = damage;
    this.knockback = knockback;
    this.bypassInvulnerableTime = bypassInvulnerableTime;
  }

  public CustomExplosion(ServerLevel level, Vec3 center, float radius, @Nullable Entity sourceEntity,
                         @Nullable Predicate<Entity> entityPredicate, float damage, @Nullable DamageSource damageSource,
                         float knockback, @Nullable ExplosionDamageCalculator damageCalculator,
                         boolean placeFire, BlockInteraction blockInteraction) {
    this(level, center, radius, sourceEntity, entityPredicate, damage, damageSource, knockback, damageCalculator, placeFire, blockInteraction, false);
  }

  @Override
  public ServerLevel level() { return level; }

  @Override
  public BlockInteraction getBlockInteraction() { return blockInteraction; }

  @Override
  public float radius() { return radius; }

  @Override
  public Vec3 center() { return center; }

  @Override
  public boolean canTriggerBlocks() { return blockInteraction == BlockInteraction.TRIGGER_BLOCK; }

  @Override
  public boolean shouldAffectBlocklikeEntities() { return blockInteraction.shouldAffectBlocklikeEntities(); }

  @Override
  @Nullable
  public LivingEntity getIndirectSourceEntity() { return Explosion.getIndirectSourceEntity(source); }

  @Override
  @Nullable
  public Entity getDirectSourceEntity() { return source; }

  public int explode() {
    this.level.gameEvent(this.source, GameEvent.EXPLODE, this.center);
    calculateHitBlocks();
    damageAndPushEntities();
    return toBlow.size();
  }

  protected void calculateHitBlocks() {
    if (!interactsWithBlocks() && !fire) return;
    Set<BlockPos> set = new HashSet<>();
    for (int rayX = 0; rayX < RAY_COUNT; rayX++) {
      for (int rayY = 0; rayY < RAY_COUNT; rayY++) {
        for (int rayZ = 0; rayZ < RAY_COUNT; rayZ++) {
          if (rayX == 0 || rayX == MAX_RAY || rayY == 0 || rayY == MAX_RAY || rayZ == 0 || rayZ == MAX_RAY) {
            double stepX = rayX * 2.0 / MAX_RAY - 1;
            double stepY = rayY * 2.0 / MAX_RAY - 1;
            double stepZ = rayZ * 2.0 / MAX_RAY - 1;
            double stepScale = 0.3f / Math.sqrt(stepX * stepX + stepY * stepY + stepZ * stepZ);
            stepX *= stepScale;
            stepY *= stepScale;
            stepZ *= stepScale;
            double targetX = this.center.x;
            double targetY = this.center.y;
            double targetZ = this.center.z;
            var random = level.getRandom();
            for (float power = this.radius * (0.7f + random.nextFloat() * 0.6f); power > 0; power -= 0.225f) {
              BlockPos target = BlockPos.containing(targetX, targetY, targetZ);
              BlockState block = level.getBlockState(target);
              FluidState fluid = level.getFluidState(target);
              if (!level.isInWorldBounds(target)) break;
              float resistance = damageCalculator.getBlockExplosionResistance(this, level, target, block, fluid).orElse(0f);
              power -= (resistance + 0.3f) * 0.3f;
              if ((fire || !block.isAir()) && power > 0 && damageCalculator.shouldBlockExplode(this, level, target, block, power)) {
                set.add(target);
              }
              targetX += stepX;
              targetY += stepY;
              targetZ += stepZ;
            }
          }
        }
      }
    }
    toBlow.addAll(set);
  }

  protected void damageAndPushEntities() {
    if (damage <= 0 && knockback == 0) return;
    float diameter = this.radius * 2;
    List<Entity> list = this.level.getEntities(this.source,
      new AABB(Math.floor(this.center.x - diameter - 1),
               Math.floor(this.center.y - diameter - 1),
               Math.floor(this.center.z - diameter - 1),
               Math.floor(this.center.x + diameter + 1),
               Math.floor(this.center.y + diameter + 1),
               Math.floor(this.center.z + diameter + 1)),
      entityPredicate);
    for (Entity entity : list) {
      Vec3 dir = entity.position().subtract(this.center);
      double length = dir.length();
      double distance = length / diameter;
      if (distance <= 1) {
        if (!(entity instanceof PrimedTnt)) {
          dir = dir.add(0, entity.getEyeY() - entity.getY(), 0);
          length = dir.length();
        }
        if (length > 1.0E-4D) {
          double strength = (1 - distance) * 1.0f;
          if (damage > 0) {
            int toDeal = (int) ((strength * strength + strength) / 2 * damage + 1);
            if (bypassInvulnerableTime) {
              ToolAttackUtil.hurtNoInvulnerableTime(entity, this.damageSource, toDeal);
            } else {
              entity.hurtServer(this.level, this.damageSource, toDeal);
            }
          }
          if (knockback != 0) {
            double adjustedStrength = strength * knockback;
            if (entity instanceof LivingEntity living) {
              // Blast protection reduces explosion knockback by 15% per level, capped at 80%.
              var blastProtection = this.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.BLAST_PROTECTION);
              int blastProtectionLevel = 0;
              for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.isArmor()) {
                  blastProtectionLevel += EnchantmentHelper.getItemEnchantmentLevel(blastProtection, living.getItemBySlot(slot));
                }
              }
              double reduction = Math.min(0.8D, blastProtectionLevel * 0.15D);
              adjustedStrength *= 1.0D - reduction;
            }
            Vec3 velocity = dir.scale(adjustedStrength / length);
            entity.push(velocity);
          }
        }
      }
    }
  }

  protected boolean interactsWithBlocks() {
    return blockInteraction != BlockInteraction.KEEP;
  }

  public void handleServer() {
    explode();
  }
}
