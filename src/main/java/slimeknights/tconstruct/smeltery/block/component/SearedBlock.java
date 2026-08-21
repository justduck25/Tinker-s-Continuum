package slimeknights.tconstruct.smeltery.block.component;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathType;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.smeltery.block.entity.component.SmelteryComponentBlockEntity;

import javax.annotation.Nullable;

public class SearedBlock extends Block implements EntityBlock {
  public static final BooleanProperty IN_STRUCTURE = BooleanProperty.create("in_structure");
  public static final StateArgumentPredicate<EntityType<?>> VALID_SPAWN = (s, r, p, e) -> !s.hasProperty(SearedBlock.IN_STRUCTURE) || !s.getValue(SearedBlock.IN_STRUCTURE);

  protected final boolean requiredBlockEntity;
  public SearedBlock(Properties properties, boolean requiredBlockEntity) {
    super(properties);
    this.requiredBlockEntity = requiredBlockEntity;
    this.registerDefaultState(this.defaultBlockState().setValue(IN_STRUCTURE, false));
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block,BlockState> builder) {
    builder.add(IN_STRUCTURE);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    if (requiredBlockEntity || state.getValue(IN_STRUCTURE)) {
      return new SmelteryComponentBlockEntity(pos, state);
    }
    return null;
  }

  @Override
  @Deprecated
  protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
    if (requiredBlockEntity || state.getValue(IN_STRUCTURE)) {
      SmelteryComponentBlockEntity.notifyMasterOfChangeFromNeighbors(world, pos, ((BlockGetter) world).getBlockState(pos));
      ((Level) world).removeBlockEntity(pos);
    }
  }


  @Deprecated
  public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      SmelteryComponentBlockEntity.notifyMasterOfChangeFromNeighbors(world, pos, newState);
    }
  }
  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    SmelteryComponentBlockEntity.updateNeighbors(world, pos, state);
  }

  @Override
  @Deprecated
  public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int id, int param) {
    super.triggerEvent(state, worldIn, pos, id, param);
    BlockEntity be = ((BlockGetter) worldIn).getBlockEntity(pos);
    return be != null && be.triggerEvent(id, param);
  }

  @Nullable
  public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
    return state.getValue(IN_STRUCTURE) ? PathType.FIRE : PathType.OPEN;
  }
}
