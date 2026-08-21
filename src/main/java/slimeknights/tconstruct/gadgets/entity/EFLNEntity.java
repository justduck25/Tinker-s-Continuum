package slimeknights.tconstruct.gadgets.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import slimeknights.tconstruct.gadgets.TinkerGadgets;

/** @deprecated use {@link slimeknights.tconstruct.tools.entity.ThrownShuriken} */
@Deprecated
public class EFLNEntity extends ThrowableProjectile {
  private ItemStack item = ItemStack.EMPTY;

  public EFLNEntity(EntityType<? extends EFLNEntity> type, Level level) {
    super(type, level);
  }

  public EFLNEntity(Level level, LivingEntity thrower) {
    super(TinkerGadgets.eflnEntity.get(), thrower.getX(), thrower.getY(), thrower.getZ(), level);
  }

  public EFLNEntity(Level worldIn, double x, double y, double z) {
    super(TinkerGadgets.eflnEntity.get(), x, y, z, worldIn);
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {}

  public void setItem(ItemStack stack) {
    this.item = stack.copy();
  }

  public ItemStack getItem() {
    return item;
  }

  @Override
  protected void onHit(HitResult result) {
    if (!level().isClientSide()) {
      new EFLNExplosion(level(), position(), 4f, this, 8f, null, 1, false, BlockInteraction.DESTROY).handleServer();
      this.discard();
    }
  }
}
