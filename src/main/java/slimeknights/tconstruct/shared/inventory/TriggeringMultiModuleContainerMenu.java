package slimeknights.tconstruct.shared.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeknights.mantle.inventory.MultiModuleContainerMenu;

import javax.annotation.Nullable;

/** Container that triggers the criteria instance */
public class TriggeringMultiModuleContainerMenu<TILE extends BlockEntity> extends MultiModuleContainerMenu<TILE> {
  public TriggeringMultiModuleContainerMenu(MenuType<?> type, int id, @Nullable Inventory inv, @Nullable TILE tile) {
    super(type, id, inv, tile);
    if (tile != null && inv != null && inv.player instanceof ServerPlayer player) {
      BlockContainerOpenedTrigger.CONTAINER.trigger(player, tile.getBlockState().getBlock());
    }
  }
}
