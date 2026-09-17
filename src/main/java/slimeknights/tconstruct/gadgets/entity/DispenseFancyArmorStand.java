package slimeknights.tconstruct.gadgets.entity;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.gadgets.entity.FancyArmorStandEntity.StandType;

/** Dispenser behavior for fancy armor stand. Based on vanilla armor stand dispenser behavior */
@RequiredArgsConstructor
public class DispenseFancyArmorStand extends DefaultDispenseItemBehavior {
  private final StandType type;

  @Override
  protected ItemStack execute(BlockSource source, ItemStack stack) {
    Direction direction = source.state().getValue(DispenserBlock.FACING);
    BlockPos blockpos = source.pos().relative(direction);
    ServerLevel server = source.level();
    FancyArmorStandEntity stand = TinkerGadgets.armorStandEntity.get().spawn(server, stack, null, blockpos, EntitySpawnReason.DISPENSER, false, false);
    if (stand != null) {
      stack.shrink(1);
      stand.setYRot(direction.toYRot());
      type.onPlace(stand);
    }
    return stack;
  }
}
