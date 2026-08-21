package slimeknights.tconstruct.library.utils;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Small bridge for legacy ItemStack NBT storage during the NeoForge port. */
public final class ItemStackNbtHelper {
  private static final RegistryAccess REGISTRY_ACCESS = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

  private ItemStackNbtHelper() {}

  public static CompoundTag save(ItemStack stack) {
    CompoundTag tag = new CompoundTag();
    tag.put("stack", ItemStack.OPTIONAL_CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, REGISTRY_ACCESS), stack).result().orElse(new CompoundTag()));
    tag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    tag.putInt("count", stack.getCount());
    return tag;
  }

  public static ItemStack parse(CompoundTag tag) {
    Tag encoded = tag.get("stack");
    if (encoded != null) {
      ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, REGISTRY_ACCESS), encoded).result().orElse(ItemStack.EMPTY);
      if (!stack.isEmpty()) {
        return stack;
      }
    }

    String id = tag.getString("id").orElse("");
    if (!id.isEmpty()) {
      Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
      if (item != Items.AIR) {
        return new ItemStack(item, Math.max(1, tag.getInt("count").orElse(1)));
      }
    }
    return ItemStack.EMPTY;
  }
}