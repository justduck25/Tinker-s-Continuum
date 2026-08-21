package slimeknights.tconstruct.library.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;

/** Helpers for serializing NeoForge FluidStack values into legacy TCon NBT containers. */
public final class FluidStackNbt {
  private FluidStackNbt() {}

  /** Reads a fluid stack from a compound tag. */
  public static FluidStack read(CompoundTag tag) {
    if (tag.isEmpty()) {
      return FluidStack.EMPTY;
    }
    return FluidStack.CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(FluidStack.EMPTY);
  }

  /** Reads a named child fluid stack from a compound tag. */
  public static FluidStack read(CompoundTag tag, String key) {
    return tag.getCompound(key).map(FluidStackNbt::read).orElse(FluidStack.EMPTY);
  }

  /** Writes a fluid stack into a compound tag. */
  public static CompoundTag write(FluidStack stack) {
    if (stack.isEmpty()) {
      return new CompoundTag();
    }
    Tag encoded = FluidStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).result().orElseGet(CompoundTag::new);
    return encoded instanceof CompoundTag compound ? compound : new CompoundTag();
  }
}
