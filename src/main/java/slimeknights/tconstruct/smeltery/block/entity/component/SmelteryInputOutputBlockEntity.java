package slimeknights.tconstruct.smeltery.block.entity.component;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.inventory.EmptyItemHandler;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.common.multiblock.IMasterLogic;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static slimeknights.mantle.util.RetexturedHelper.TAG_TEXTURE;

/**
 * Shared logic between drains and ducts.
 */
public abstract class SmelteryInputOutputBlockEntity<T> extends SmelteryComponentBlockEntity implements IRetexturedBlockEntity {
  /** Empty handler for in case the valid handler becomes invalid. */
  protected final T emptyInstance;
  @Nullable
  private T cachedHandler = null;

  /* Retexturing */
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;

  protected SmelteryInputOutputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, T emptyInstance) {
    super(type, pos, state);
    this.emptyInstance = emptyInstance;
  }

  /** Clears all cached handlers. */
  protected void clearHandler() {
    cachedHandler = null;
  }

  public void invalidateCaps() {
    clearHandler();
  }

  @Override
  public void onMasterLoad(IMasterLogic master) {
    clearHandler();
  }

  @Override
  protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
    assert level != null;

    boolean masterChanged = false;
    if (!Objects.equals(getMasterPos(), master)) {
      clearHandler();
      masterChanged = true;
    }
    super.setMaster(master, block);
    if (masterChanged) {
      level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }
  }

  /**
   * Gets the handler to store in this IO block.
   * @param parent  Parent block entity
   * @return Handler from parent, or empty if absent
   */
  protected T getHandler(BlockEntity parent) {
    return emptyInstance;
  }

  /** Fetches the cached handler, falling back to the empty handler when no valid master exists. */
  @Nonnull
  public T getHandler() {
    if (cachedHandler == null) {
      if (validateMaster()) {
        BlockPos master = getMasterPos();
        if (master != null && this.level != null) {
          BlockEntity te = level.getBlockEntity(master);
          if (te != null) {
            cachedHandler = getHandler(te);
            if (true) {
            }
            return cachedHandler;
          }
        }
      }
      if (true) {
      }
      cachedHandler = emptyInstance;
    }
    return cachedHandler;
  }

  /* Retexturing */

  @Override
  @Nonnull
  public net.neoforged.neoforge.model.data.ModelData getModelData() {
    return RetexturedHelper.getModelData(getTexture());
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(texture);
  }

  @Override
  public void updateTexture(String name) {
    Block oldTexture = texture;
    texture = RetexturedHelper.getBlock(name);
    if (oldTexture != texture) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }

  /* NBT */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(CompoundTag tags) {
    super.saveSynced(tags);
    if (texture != Blocks.AIR) {
      tags.putString(TAG_TEXTURE, getTextureName());
    }
  }

  public void load(CompoundTag tags) {
    if (tags.contains(TAG_TEXTURE)) {
      texture = RetexturedHelper.getBlock(tags.getString(TAG_TEXTURE).orElse(""));
      RetexturedHelper.onTextureUpdated(this);
    }
  }

  /** Fluid implementation of smeltery IO. */
  public static abstract class SmelteryFluidIO extends SmelteryInputOutputBlockEntity<IFluidHandler> {
    protected SmelteryFluidIO(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, EmptyFluidHandler.INSTANCE);
    }

    /** Wraps the given smeltery fluid handler. */
    protected IFluidHandler makeWrapper(IFluidHandler handler) {
      return handler;
    }

    @Override
    protected IFluidHandler getHandler(BlockEntity parent) {
      if (parent instanceof ISmelteryTankHandler tankHandler) {
        return makeWrapper(tankHandler.getFluidCapability());
      }
      return emptyInstance;
    }
  }

  /** Item implementation of smeltery IO. */
  public static class ChuteBlockEntity extends SmelteryInputOutputBlockEntity<IItemHandler> {
    public ChuteBlockEntity(BlockPos pos, BlockState state) {
      this(TinkerSmeltery.chute.get(), pos, state);
    }

    protected ChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, EmptyItemHandler.INSTANCE);
    }
  }
}