package slimeknights.tconstruct.gadgets.entity.shuriken;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.tools.entity.ThrownShuriken;
import slimeknights.tconstruct.tools.entity.ToolProjectile;

/** @deprecated use {@link ThrownShuriken} */
@Deprecated
public abstract class ShurikenEntityBase extends ThrowableProjectile implements ToolProjectile {

  public ShurikenEntityBase(EntityType<? extends ShurikenEntityBase> type, Level worldIn) {
    super(type, worldIn);
  }

  public ShurikenEntityBase(EntityType<? extends ShurikenEntityBase> type, double x, double y, double z, Level worldIn) {
    super(type, x, y, z, worldIn);
  }

  public ShurikenEntityBase(EntityType<? extends ShurikenEntityBase> type, LivingEntity livingEntityIn, Level worldIn) {
    super(type, livingEntityIn.getX(), livingEntityIn.getY(), livingEntityIn.getZ(), worldIn);
  }

  public abstract float getDamage();

  public abstract float getKnockback();

  @Override
  protected void onHit(HitResult result) {
    super.onHit(result);

    Level level = level();
    if (!level.isClientSide()) {
      level.broadcastEntityEvent(this, (byte) 3);
      this.discard();
    }
  }

  @Override
  protected void onHitBlock(BlockHitResult result) {
    super.onHitBlock(result);

    ItemStack stack = getItem();
    if (!stack.isEmpty()) {
      if (!level().isClientSide()) {
        level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level(), getX(), getY(), getZ(), stack));
      }
    }
  }

  @Override
  protected void onHitEntity(EntityHitResult result) {
    Entity entity = result.getEntity();
    entity.hurt(damageSources().thrown(this, this.getOwner()), this.getDamage());

    if (!level().isClientSide() && entity instanceof LivingEntity) {
      Vec3 motion = this.getDeltaMovement().normalize();
      ((LivingEntity) entity).knockback(this.getKnockback(), -motion.x, -motion.z);
    }
  }

  @Override
  public ItemStack getDisplayTool() {
    return getItem();
  }

  public void setItem(ItemStack stack) {
    // handled by subclasses that need item tracking
  }

  public ItemStack getItem() {
    return ItemStack.EMPTY;
  }
}
