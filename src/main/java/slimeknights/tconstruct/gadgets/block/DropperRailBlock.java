package slimeknights.tconstruct.gadgets.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class DropperRailBlock extends RailBlock {

  public DropperRailBlock(Properties properties) {
    super(properties);
  }

  public void onMinecartPass(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
    ResourceHandler<ItemResource> cartHandler = cart.getCapability(Capabilities.Item.ENTITY);
    if (cartHandler == null) {
      return;
    }
    ResourceHandler<ItemResource> blockHandler = world.getCapability(Capabilities.Item.BLOCK, pos.below(), Direction.UP);
    if (blockHandler == null) {
      return;
    }

    for (int i = 0; i < cartHandler.size(); i++) {
      ItemResource resource = cartHandler.getResource(i);
      if (resource.isEmpty()) {
        continue;
      }
      try (Transaction txn = Transaction.open(null)) {
        long extracted = cartHandler.extract(resource, 1, txn);
        if (extracted > 0) {
          long inserted = blockHandler.insert(resource, (int) extracted, txn);
          if (inserted > 0) {
            txn.commit();
            break;
          }
        }
      }
    }
  }
}
