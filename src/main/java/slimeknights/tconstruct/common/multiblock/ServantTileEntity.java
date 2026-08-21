package slimeknights.tconstruct.common.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import slimeknights.mantle.block.entity.MantleBlockEntity;

import javax.annotation.Nullable;

public class ServantTileEntity extends MantleBlockEntity implements IServantLogic {
  private static final String TAG_MASTER_POS = "masterOffset";
  private static final String TAG_MASTER_BLOCK = "masterBlock";

  @Nullable
  private BlockPos masterPos;

  @Nullable
  public BlockPos getMasterPos() {
    return masterPos;
  }
  @Nullable
  private Block masterBlock;

  public ServantTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  /** Checks if this servant has a master */
  public boolean hasMaster() {
    return masterPos != null;
  }

  /**
   * Called to change the master
   * @param master  New master
   * @param block   New master block
   */
  protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
    masterPos = master;
    masterBlock = block;
    this.setChangedFast();
  }

  /**
   * Checks that this servant has a valid master. Clears the master if invalid
   * @return  True if this servant has a valid master
   */
  protected boolean validateMaster() {
    if (masterPos == null) {
      return false;
    }

    // ensure the master block is correct
    if (getLevel() != null && ((BlockGetter) getLevel()).getBlockState(masterPos).getBlock() == masterBlock) {
      return true;
    }
    // master invalid, so clear
    setMaster(null, null);
    return false;
  }

  @Override
  public boolean isValidMaster(IMasterLogic master) {
    // if we have a valid master, the passed master is only valid if its our current master
    if (validateMaster()) {
      return master.getMasterPos().equals(this.masterPos);
    }
    // otherwise, we are happy with any master
    return true;
  }

  @Override
  public void notifyMasterOfChange(BlockPos pos, BlockState state) {
    if (validateMaster() && getLevel() != null) {
      assert masterPos != null;
      BlockEntity be = ((BlockGetter) getLevel()).getBlockEntity(masterPos);
      if (be instanceof IMasterLogic master) {
        master.notifyChange(pos, state);
      }
    }
  }

  @Override
  public void setPotentialMaster(IMasterLogic master) {
    BlockPos newMaster = master.getMasterPos();
    // if this is our current master, simply update the master block
    if (newMaster.equals(this.masterPos)) {
      masterBlock = master.getMasterBlock().getBlock();
      this.setChangedFast();
    // otherwise, only set if we don't have a master
    } else if (!validateMaster()) {
      setMaster(newMaster, master.getMasterBlock().getBlock());
    }
  }

  @Override
  public void removeMaster(IMasterLogic master) {
    if (masterPos != null && masterPos.equals(master.getMasterPos())) {
      setMaster(null, null);
    }
  }


  /* NBT */

  /**
   * Reads the master from NBT
   * @param tags  NBT to read
   */
  protected void readMaster(CompoundTag tags) {
    BlockPos masterPos = readOptionalPos(tags, TAG_MASTER_POS, this.worldPosition);
    Block masterBlock = null;
    // if the master position is valid, get the master block
    if (masterPos != null && tags.contains(TAG_MASTER_BLOCK)) {
      Identifier masterBlockName = Identifier.tryParse(tags.getString(TAG_MASTER_BLOCK).orElse(""));
      if (masterBlockName != null && BuiltInRegistries.BLOCK.containsKey(masterBlockName)) {
        masterBlock = BuiltInRegistries.BLOCK.getValue(masterBlockName);
      }
    }
    // if both valid, set
    if (masterBlock != null) {
      this.masterPos = masterPos;
      this.masterBlock = masterBlock;
    }
  }

  /**
   * Reads a block position from Tag
   */
  @Nullable
  private static BlockPos readOptionalPos(CompoundTag parent, String key, BlockPos offset) {
    return parent.getCompound(key)
      .map(tag -> new BlockPos(tag.getInt("x").orElse(0) + offset.getX(), tag.getInt("y").orElse(0) + offset.getY(), tag.getInt("z").orElse(0) + offset.getZ()))
      .orElse(null);
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    String blockName = input.getStringOr(TAG_MASTER_BLOCK, "");
    if (!blockName.isEmpty()) {
      int x = input.getIntOr("mx", 0);
      int y = input.getIntOr("my", 0);
      int z = input.getIntOr("mz", 0);
      BlockPos masterPos = new BlockPos(x + this.worldPosition.getX(), y + this.worldPosition.getY(), z + this.worldPosition.getZ());
      Identifier id = Identifier.tryParse(blockName);
      if (id != null && BuiltInRegistries.BLOCK.containsKey(id)) {
        Block masterBlock = BuiltInRegistries.BLOCK.getValue(id);
        this.masterPos = masterPos;
        this.masterBlock = masterBlock;
      }
    }
  }

  /**
   * Writes the master position and master block to the given output
   */
  @Override
  public void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    if (masterPos != null && masterBlock != null) {
      output.putInt("mx", masterPos.getX() - this.worldPosition.getX());
      output.putInt("my", masterPos.getY() - this.worldPosition.getY());
      output.putInt("mz", masterPos.getZ() - this.worldPosition.getZ());
      output.putString(TAG_MASTER_BLOCK, BuiltInRegistries.BLOCK.getKey(masterBlock).toString());
    }
  }
}
