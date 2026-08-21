package slimeknights.tconstruct.tables.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.tables.block.entity.chest.TinkersChestBlockEntity;

public class TinkersChestBlock extends ChestBlock {
  public TinkersChestBlock(Properties builder, BlockEntitySupplier<? extends BlockEntity> be, boolean dropsItems) {
    super(builder, be, dropsItems);
  }

  @Override
  protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (heldItem.getItem() instanceof DyeItem) {
      DyeColor dyeColor = heldItem.get(DataComponents.DYE);
      if (dyeColor != null) {
        var chest = BlockEntityHelper.get(TinkersChestBlockEntity.class, world, pos);
        if (chest.isPresent()) {
          world.playSound(player, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
          if (!world.isClientSide()) {
            chest.get().setColor(dyeColor.getTextureDiffuseColor());
            chest.get().setChangedFast();
            world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            heldItem.shrink(1);
          }
          return InteractionResult.SUCCESS;
        }
      }
    }
    return super.useItemOn(heldItem, state, world, pos, player, hand, hit);
  }

  public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
    ItemStack stack = new ItemStack(this);
    BlockEntityHelper.get(TinkersChestBlockEntity.class, world, pos).ifPresent(te -> {
      if (te.hasColor()) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(te.getColor()));
      }
    });
    return stack;
  }
}
