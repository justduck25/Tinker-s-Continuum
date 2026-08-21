package slimeknights.tconstruct.smeltery.block.entity.tank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import slimeknights.tconstruct.library.fluid.FluidStackNbt;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class CastingFluidHandler implements IFluidHandler {
  private final CastingBlockEntity tile;
  @Getter @Setter
  private FluidStack fluid = FluidStack.EMPTY;
  @Setter
  private int capacity = 0;
  private FluidStack filter = FluidStack.EMPTY;

  /** Checks if the given fluid is valid */
  public boolean isFluidValid(FluidStack stack) {
    return !stack.isEmpty() && (filter.isEmpty() || FluidStack.isSameFluidSameComponents(filter, stack));
  }

  /** Checks if the fluid is empty */
  public boolean isEmpty() {
    return fluid.isEmpty();
  }

  /** Gets the current capacity of this fluid handler */
  public int getCapacity() {
    if (capacity == 0) {
      return fluid.getAmount();
    }
    return capacity;
  }

  /** Resets the tanks filter */
  public void reset() {
    capacity = 0;
    fluid = FluidStack.EMPTY;
    filter = FluidStack.EMPTY;
  }

  /** Snapshot used by NeoForge transfer transactions. */
  public State createSnapshot() {
    return new State(fluid.copy(), capacity, filter.copy(), tile.createCastingStateSnapshot());
  }

  /** Restores all non-fluid side state after a rolled back NeoForge transfer transaction. */
  public void restoreSnapshot(State state) {
    fluid = state.fluid.copy();
    capacity = state.capacity;
    filter = state.filter.copy();
    tile.restoreCastingStateSnapshot(state.tileState);
  }

  public record State(FluidStack fluid, int capacity, FluidStack filter, CastingBlockEntity.CastingState tileState) {}

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !isFluidValid(resource)) {
      return 0;
    }

    int capacity = this.capacity;
    if (filter.isEmpty() || this.capacity == 0) {
      capacity = tile.initNewCasting(resource, action);
      if (capacity <= 0) {
        return 0;
      }
      if (action.execute()) {
        this.capacity = capacity;
        this.filter = resource.copyWithAmount(1);
      }
    }

    if (fluid.isEmpty()) {
      int amount = Math.min(capacity, resource.getAmount());
      if (action.execute()) {
        fluid = resource.copyWithAmount(amount);
        tile.onContentsChanged();
      }
      return amount;
    }

    if (!FluidStack.isSameFluidSameComponents(resource, fluid)) {
      return 0;
    }

    int space = capacity - fluid.getAmount();
    if (space <= 0) {
      return 0;
    }
    int amount = resource.getAmount();
    if (amount < space) {
      if (action.execute()) {
        fluid.grow(amount);
        tile.onContentsChanged();
      }
      return amount;
    }

    if (action.execute()) {
      fluid.setAmount(capacity);
      tile.onContentsChanged();
    }
    return space;
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, fluid)) {
      return FluidStack.EMPTY;
    }
    return this.drain(resource.getAmount(), action);
  }

  @Nonnull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    int drained = Math.min(fluid.getAmount(), maxDrain);
    if (drained <= 0) {
      return FluidStack.EMPTY;
    }

    FluidStack stack = fluid.copyWithAmount(drained);
    if (action.execute()) {
      fluid.shrink(drained);
      if (fluid.isEmpty()) {
        tile.reset();
      } else {
        tile.onContentsChanged();
      }
    }
    return stack;
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    if (tank == 0) {
      return fluid;
    }
    return FluidStack.EMPTY;
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public int getTankCapacity(int tank) {
    return getCapacity();
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return tank == 0 && isFluidValid(stack);
  }

  private static final String TAG_FLUID = "fluid";
  private static final String TAG_FILTER = "filter";
  private static final String TAG_FILTER_FLUID = "filter_fluid";
  private static final String TAG_CAPACITY = "capacity";

  /** Reads the tank from Tag */
  public void readFromTag(CompoundTag nbt) {
    capacity = nbt.getInt(TAG_CAPACITY).orElse(0);
    if (nbt.contains(TAG_FLUID)) {
      setFluid(FluidStackNbt.read(nbt, TAG_FLUID));
    }
    if (nbt.contains(TAG_FILTER_FLUID)) {
      filter = FluidStackNbt.read(nbt, TAG_FILTER_FLUID).copyWithAmount(1);
    } else if (nbt.contains(TAG_FILTER)) {
      net.minecraft.world.level.material.Fluid legacyFilter = BuiltInRegistries.FLUID.getValue(Identifier.parse(nbt.getString(TAG_FILTER).orElse("")));
      if (legacyFilter != null) {
        filter = new FluidStack(legacyFilter, 1);
      }
    }
  }

  /** Reads the tank from persistent value input. */
  public void readFromInput(ValueInput input) {
    capacity = input.getIntOr(TAG_CAPACITY, 0);
    setFluid(input.read(TAG_FLUID, FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
    filter = input.read(TAG_FILTER_FLUID, FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY).copyWithAmount(1);
    if (filter.isEmpty()) {
      String filterName = input.getStringOr(TAG_FILTER, "");
      if (!filterName.isEmpty()) {
        net.minecraft.world.level.material.Fluid legacyFilter = BuiltInRegistries.FLUID.getValue(Identifier.parse(filterName));
        if (legacyFilter != null) {
          filter = new FluidStack(legacyFilter, 1);
        }
      }
    }
  }

  /** Writes the tank to persistent value output. */
  public void writeToOutput(ValueOutput output) {
    output.putInt(TAG_CAPACITY, capacity);
    if (!fluid.isEmpty()) {
      output.store(TAG_FLUID, FluidStack.OPTIONAL_CODEC, fluid);
    }
    if (!filter.isEmpty()) {
      output.store(TAG_FILTER_FLUID, FluidStack.OPTIONAL_CODEC, filter);
    }
  }

  /** Write the tank from NBT */
  @SuppressWarnings("deprecation")
  public CompoundTag writeToTag(CompoundTag nbt) {
    nbt.putInt(TAG_CAPACITY, capacity);
    if (!fluid.isEmpty()) {
      nbt.put(TAG_FLUID, FluidStackNbt.write(fluid));
    }
    if (!filter.isEmpty()) {
      nbt.put(TAG_FILTER_FLUID, FluidStackNbt.write(filter));
    }
    return nbt;
  }
}
