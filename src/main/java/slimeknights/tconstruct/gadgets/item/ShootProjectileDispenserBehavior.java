package slimeknights.tconstruct.gadgets.item;

import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;

/** Entity type based projectile shooting dispenser behavior */
public class ShootProjectileDispenserBehavior extends DefaultDispenseItemBehavior {
  private final EntityType<? extends ThrowableProjectile> entity;
  private final float power;
  private final float inaccuracy;

  public ShootProjectileDispenserBehavior(EntityType<? extends ThrowableProjectile> entity, float power, float inaccuracy) {
    this.entity = entity;
    this.power = power;
    this.inaccuracy = inaccuracy;
  }

  public ShootProjectileDispenserBehavior(EntityType<? extends ThrowableProjectile> entity) {
    this(entity, 1.1f, 6.0f);
  }

  @Override
  public ItemStack execute(BlockSource source, ItemStack stack) {
    Level level = source.level();
    ThrowableProjectile projectile = entity.create((net.minecraft.server.level.ServerLevel) level, EntitySpawnReason.EVENT);
    if (projectile != null) {
      Position position = DispenserBlock.getDispensePosition(source);
      Direction direction = source.state().getValue(DispenserBlock.FACING);
      projectile.setPos(position.x(), position.y(), position.z());
      projectile.shoot(direction.getStepX(), ((float)direction.getStepY() + 0.1F), direction.getStepZ(), power, inaccuracy);
      level.addFreshEntity(projectile);
      stack.shrink(1);
    }
    return stack;
  }

  @Override
  protected void playSound(BlockSource pSource) {
    pSource.level().levelEvent(LevelEvent.SOUND_DISPENSER_PROJECTILE_LAUNCH, pSource.pos(), 0);
  }
}
