package slimeknights.tconstruct.smeltery.block.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import slimeknights.mantle.block.RetexturedBlock;
import slimeknights.mantle.inventory.BaseContainerMenu;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.MultiblockResult;
import slimeknights.tconstruct.smeltery.network.StructureErrorPositionPacket;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Multiblock that displays the error from the tile entity on right click
 */
public abstract class HeatingControllerBlock extends ControllerBlock {
  protected HeatingControllerBlock(Properties builder) {
    super(builder);
  }

  @Override
  protected boolean openGui(Player player, Level world, BlockPos pos) {
    BlockState state = world.getBlockState(pos);
    if (state.getBlock() != this) {
      return false;
    }
    if (!canOpenGui(state)) {
      return displayStatus(player, world, pos, state);
    }
    if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      BlockEntityHelper.get(HeatingStructureBlockEntity.class, world, pos).ifPresent(te -> {
        if (!te.refreshStructureNow()) {
          displayStatus(player, world, pos, state);
          return;
        }
        serverPlayer.openMenu(te, buffer -> {
          buffer.writeBlockPos(pos);
          te.writeMenuSync(buffer);
        });
        if (player.containerMenu instanceof BaseContainerMenu<?> menu) {
          menu.syncOnOpen(serverPlayer);
        }
      });
    }
    return true;
  }

  @Override
  protected boolean displayStatus(Player player, Level world, BlockPos pos, BlockState state) {
    if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      BlockEntityHelper.get(HeatingStructureBlockEntity.class, world, pos).ifPresent(te -> {
        MultiblockResult result = te.getStructureResult();
        if (!result.isSuccess()) {
          player.sendOverlayMessage(result.getMessage());
          TinkerNetwork.getInstance().sendTo(new StructureErrorPositionPacket(pos, result.getPos()), serverPlayer);
        }
      });
    }
    return true;
  }

  public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
    List<Component> lines = new java.util.ArrayList<>();
    RetexturedHelper.addTooltip(stack, lines);
    lines.forEach(tooltip);
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(world, pos, state, placer, stack);
    RetexturedBlock.updateTextureBlock(world, pos, stack);
  }

  public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
    return RetexturedBlock.getPickBlock(world, pos, state);
  }
}
