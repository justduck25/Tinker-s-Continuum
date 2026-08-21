package slimeknights.tconstruct.gadgets.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.shared.TinkerCommons;

/** @deprecated use {@link slimeknights.tconstruct.tools.entity.ThrownShuriken} */
@Deprecated
public class GlowballEntity extends ThrowableProjectile {
  private ItemStack item = ItemStack.EMPTY;

  public GlowballEntity(EntityType<? extends GlowballEntity> p_i50159_1_, Level p_i50159_2_) {
    super(p_i50159_1_, p_i50159_2_);
  }

  public GlowballEntity(Level worldIn, LivingEntity throwerIn) {
    super(TinkerGadgets.glowBallEntity.get(), throwerIn.getX(), throwerIn.getY(), throwerIn.getZ(), worldIn);
  }

  public GlowballEntity(Level worldIn, double x, double y, double z) {
    super(TinkerGadgets.glowBallEntity.get(), x, y, z, worldIn);
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {}

  public void setItem(ItemStack stack) {
    this.item = stack.copy();
  }

  public ItemStack getItem() {
    return item;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  protected void onHit(HitResult result) {
    if (!level().isClientSide()) {
      BlockPos position = null;
      Direction direction = Direction.DOWN;

      if (result.getType() == HitResult.Type.ENTITY) {
        position = ((EntityHitResult) result).getEntity().blockPosition();
      }

      if (result.getType() == HitResult.Type.BLOCK) {
        BlockHitResult blockHit = (BlockHitResult) result;
        position = blockHit.getBlockPos().relative(blockHit.getDirection());
        direction = blockHit.getDirection().getOpposite();
      }

      if (position != null) {
        TinkerCommons.glowBlock.get().addGlow(level(), position, direction);
      }
    }

    if (!level().isClientSide()) {
      level().broadcastEntityEvent(this, (byte) 3);
      this.discard();
    }
  }
}
