package slimeknights.tconstruct.smeltery.block.entity.component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.model.data.ModelData;
import slimeknights.tconstruct.library.client.model.ModelProperties;

import slimeknights.tconstruct.common.multiblock.IMasterLogic;
import slimeknights.tconstruct.library.fluid.FluidStackNbt;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;
import slimeknights.tconstruct.smeltery.item.TankItem;

import javax.annotation.Nullable;

public class TankBlockEntity extends SmelteryComponentBlockEntity implements ITankBlockEntity {
  /** Max capacity for the tank */
  public static final int DEFAULT_CAPACITY = FluidType.BUCKET_VOLUME * 4;

  /**
   * Gets the capacity for the given block
   * @param block  block
   * @return  Capacity
   */
  public static int getCapacity(Block block) {
    if (block instanceof ITankBlock) {
      return ((ITankBlock) block).getCapacity();
    }
    return DEFAULT_CAPACITY;
  }

  /**
   * Gets the capacity for the given item
   * @param item  item
   * @return  Capacity
   */
  public static int getCapacity(Item item) {
    if (item instanceof BlockItem) {
      return getCapacity(((BlockItem)item).getBlock());
    }
    return DEFAULT_CAPACITY;
  }

  /** Internal fluid tank instance */
  protected final FluidTankAnimated tank;
  /** Last comparator strength to reduce block updates */
  private int lastStrength = -1;

  public FluidTankAnimated getTank() {
    return tank;
  }

  public int getLastStrength() {
    return lastStrength;
  }

  public void setLastStrength(int lastStrength) {
    this.lastStrength = lastStrength;
  }

  public TankBlockEntity(BlockPos pos, BlockState state) {
    this(pos, state, state.getBlock() instanceof ITankBlock tank
                     ? tank
                     : TinkerSmeltery.searedTank.get(TankType.FUEL_TANK));
  }

  /** Main constructor */
  public TankBlockEntity(BlockPos pos, BlockState state, ITankBlock block) {
    this(TinkerSmeltery.tank.get(), pos, state, block);
  }

  /** Extendable constructor */
  @SuppressWarnings("WeakerAccess")
  protected TankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ITankBlock block) {
    super(type, pos, state);
    tank = new FluidTankAnimated(block.getCapacity(), this);
  }

  /*
   * Tank methods
   */

  /** Updates the light for this tank using {@link SearedTankBlock#LIGHT} */
  public static void updateLight(BlockEntity be, IFluidTank tank) {
    Level level = be.getLevel();
    if (level != null && !level.isClientSide()) {
      FluidStack fluid = tank.getFluid();
      int light = fluid.isEmpty() ? 0 : fluid.getFluid().getFluidType().getLightLevel(fluid);
      BlockState state = be.getBlockState();
      if (light != state.getValue(SearedTankBlock.LIGHT)) {
        ((LevelAccessor) level).setBlock(be.getBlockPos(), state.setValue(SearedTankBlock.LIGHT, light), Block.UPDATE_ALL);
      }
    }
  }

    @Override
  public ModelData getModelData() {
    return ModelData.builder()
      .with(ModelProperties.FLUID_STACK, tank.getFluid())
      .with(ModelProperties.TANK_CAPACITY, tank.getCapacity())
      .build();
  }

  @Override
  public void onTankContentsChanged() {

    ITankBlockEntity.super.onTankContentsChanged();
    if (this.level != null) {
      updateLight(this, tank);
      this.requestModelDataUpdate();
      if (!this.level.isClientSide()) {
        BlockState state = getBlockState();
        this.level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
      }
    }
  }

  @Override
  public void onLoad() {
    super.onLoad();
    refreshTankRender();
    if (level != null && !level.isClientSide()) {
      BlockPos masterPos = getMasterPos();
      if (masterPos != null && ((BlockGetter) level).getBlockEntity(masterPos) instanceof IMasterLogic master) {
        master.onServantLoad(this);
      }
    }
  }

  /** Refreshes tank rendering and synced light after loading or packet updates. */
  private void refreshTankRender() {
    if (level != null) {
      updateLight(this, tank);
      requestModelDataUpdate();
      if (!level.isClientSide()) {
        BlockState state = getBlockState();
        level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
      }
    }
  }

  /*
   * NBT
   */

  /**
   * Serializes a fluid into the custom data shape {@link TankItem} uses, so a broken tank stacks with a filled one.
   * @return  Data to store, or null to clear the component for an empty tank
   */
  @Nullable
  public static CustomData writeTankData(FluidStack fluid) {
    if (fluid.isEmpty()) {
      return null;
    }
    CompoundTag tag = new CompoundTag();
    tag.put(NBTTags.TANK, FluidStackNbt.write(fluid));
    return CustomData.of(tag);
  }

  /** Reads the tank subtag out of the given components, returning an empty tag when there is nothing stored. */
  public static CompoundTag readTankData(DataComponentGetter components) {
    CustomData data = components.get(DataComponents.CUSTOM_DATA);
    if (data == null) {
      return new CompoundTag();
    }
    return data.copyTag().getCompound(NBTTags.TANK).orElseGet(CompoundTag::new);
  }

  /**
   * Sets the tag on the stack based on the contained tank.
   * The component is written even when the tank is empty, as the value the block was placed with otherwise survives
   * in the stored component patch and a drained tank drops still holding its original fluid.
   */
  @Override
  protected void collectImplicitComponents(DataComponentMap.Builder components) {
    super.collectImplicitComponents(components);
    components.set(DataComponents.CUSTOM_DATA, writeTankData(tank.getFluid()));
  }

  /** Fills the tank from a placed item, which also keeps the fluid out of the stored component patch. */
  @Override
  protected void applyImplicitComponents(DataComponentGetter components) {
    super.applyImplicitComponents(components);
    updateTank(readTankData(components));
  }

  public void setTankTag(ItemStack stack) {
    TankItem.setTank(stack, tank);
  }

  /**
   * Updates the tank from an NBT tag, used in the block
   * @param nbt  tank NBT
   */
  public void updateTank(CompoundTag nbt) {
    if (nbt.isEmpty()) {
      tank.setFluid(FluidStack.EMPTY);
    } else {
      FluidStack fluid = FluidStackNbt.read(nbt);
      if (fluid.isEmpty()) {
        fluid = nbt.getCompound("fluid")
          .map(FluidStackNbt::read)
          .orElseGet(() -> nbt.getCompound("Fluid")
            .map(FluidStackNbt::read)
            .orElse(FluidStack.EMPTY));
      }
      tank.setFluid(fluid);
    }
    onTankContentsChanged();
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  public void handleUpdateTag(CompoundTag tag) {
    tag.getCompound(NBTTags.TANK).ifPresent(this::updateTank);
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    tank.setCapacity(getCapacity(getBlockState().getBlock()));
    input.child(NBTTags.TANK).flatMap(child -> child.read("fluid", FluidStack.OPTIONAL_CODEC)).ifPresent(tank::setFluid);
    refreshTankRender();
  }

  @Override
  public void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    if (!tank.isEmpty()) {
      output.child(NBTTags.TANK).store("fluid", FluidStack.OPTIONAL_CODEC, tank.getFluid());
    }
  }

  @Override
  public void saveSynced(CompoundTag tag) {
    super.saveSynced(tag);
    if (!tank.isEmpty()) {
      CompoundTag tankTag = new CompoundTag();
      tankTag.put("fluid", FluidStackNbt.write(tank.getFluid()));
      tag.put(NBTTags.TANK, tankTag);
    }
  }

  /** Interface for blocks to return their capacity */
  public interface ITankBlock {
    /** Gets the capacity for this tank */
    int getCapacity();
  }
}
