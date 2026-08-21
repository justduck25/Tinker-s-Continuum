package slimeknights.tconstruct.library.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Local helpers for BlockPos NBT while Minecraft's NbtUtils signatures are changing. */
public final class BlockPosNbt {
  private BlockPosNbt() {}

  public static CompoundTag write(BlockPos pos) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("x", pos.getX());
    tag.putInt("y", pos.getY());
    tag.putInt("z", pos.getZ());
    return tag;
  }

  public static BlockPos read(CompoundTag tag) {
    return new BlockPos(tag.getInt("x").orElse(0), tag.getInt("y").orElse(0), tag.getInt("z").orElse(0));
  }
}