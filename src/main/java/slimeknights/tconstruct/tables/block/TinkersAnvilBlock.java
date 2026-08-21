package slimeknights.tconstruct.tables.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;

public class TinkersAnvilBlock extends TinkerStationBlock {
  private static final VoxelShape PART_BASE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
  private static final VoxelShape X_AXIS_AABB = Shapes.or(
    PART_BASE,
    Block.box(4.0D, 4.0D, 3.0D, 12.0D, 5.0D, 13.0D),
    Block.box(6.0D, 5.0D, 4.0D, 10.0D, 10.0D, 12.0D),
    Block.box(3.0D, 10.0D, 0.0D, 13.0D, 16.0D, 16.0D));
  private static final VoxelShape Z_AXIS_AABB = Shapes.or(
    PART_BASE,
    Block.box(3.0D, 4.0D, 4.0D, 13.0D, 5.0D, 12.0D),
    Block.box(4.0D, 5.0D, 6.0D, 12.0D, 10.0D, 10.0D),
    Block.box(0.0D, 10.0D, 3.0D, 16.0D, 16.0D, 13.0D));

  public TinkersAnvilBlock(Properties builder, int slotCount) {
    super(builder, slotCount);
  }

  @Override
  @Deprecated
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    Direction direction = state.getValue(FACING);
    return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
  }

  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    if (level.getBlockEntity(pos) instanceof TinkerStationBlockEntity be) {
      String block = RetexturedHelper.getTextureName(stack);
      if (!block.isEmpty()) {
        be.updateTexture(block);
      } else {
        MaterialVariantId material = IMaterialItem.getMaterialFromStack(stack);
        if (material != IMaterial.UNKNOWN_ID) {
          be.setMaterial(material);
        }
      }
    }
  }
}