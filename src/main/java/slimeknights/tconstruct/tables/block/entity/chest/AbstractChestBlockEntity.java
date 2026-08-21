package slimeknights.tconstruct.tables.block.entity.chest;

import lombok.Getter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.block.entity.NameableBlockEntity;
import slimeknights.tconstruct.library.utils.ItemStackNbtHelper;
import slimeknights.tconstruct.tables.block.entity.inventory.IChestItemHandler;
import slimeknights.tconstruct.tables.menu.TinkerChestContainerMenu;

import javax.annotation.Nullable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Shared base logic for all Tinkers' chest tile entities */
public abstract class AbstractChestBlockEntity extends NameableBlockEntity {
  private static final String KEY_ITEMS = "Items";

  @Getter
  private final IChestItemHandler itemHandler;
  protected AbstractChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component name, IChestItemHandler itemHandler) {
    super(type, pos, state, name);
    itemHandler.setParent(this);
    this.itemHandler = itemHandler;
  }
@Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new TinkerChestContainerMenu(menuId, playerInventory, this);
  }

  /**
   * Checks if the given item should be inserted into the chest on interact
   * @param player    Player inserting
   * @param heldItem  Stack to insert
   * @return  Return true
   */
  public boolean canInsert(Player player, ItemStack heldItem) {
    return true;
  }

  @Override
  public void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    writeInventory(output);
  }

  @Override
  public void saveSynced(CompoundTag tags) {
    super.saveSynced(tags);
    writeInventory(tags);
  }

  @Override
  protected void collectImplicitComponents(DataComponentMap.Builder components) {
    super.collectImplicitComponents(components);
    CompoundTag inventory = new CompoundTag();
    writeInventory(inventory);
    if (!inventory.isEmpty()) {
      CompoundTag tag = new CompoundTag();
      tag.put("TinkerData", inventory);
      components.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
  }

  /** Writes the inventory to persistent data. */
  private void writeInventory(ValueOutput output) {
    ValueOutput.TypedOutputList<ItemStack> list = output.list(KEY_ITEMS, ItemStack.OPTIONAL_CODEC);
    for (int i = 0; i < itemHandler.getSlots(); i++) {
      ItemStack stack = itemHandler.getStackInSlot(i);
      if (!stack.isEmpty()) {
        list.add(stack);
      } else if (i < itemHandler.getVisualSize()) {
        list.add(ItemStack.EMPTY);
      }
    }
  }

  /** Writes the inventory to legacy/custom-data NBT for dropped chest items. */
  private void writeInventory(CompoundTag tags) {
    ListTag list = new ListTag();
    for (int i = 0; i < itemHandler.getSlots(); i++) {
      ItemStack stack = itemHandler.getStackInSlot(i);
      if (!stack.isEmpty()) {
        CompoundTag stackTag = ItemStackNbtHelper.save(stack);
        stackTag.putInt("Slot", i);
        list.add(stackTag);
      }
    }
    if (!list.isEmpty()) {
      tags.put(KEY_ITEMS, list);
    }
  }

  /** Reads the inventory from NBT */
  public void readInventory(CompoundTag tags) {
    for (int i = 0; i < itemHandler.getSlots(); i++) {
      itemHandler.setStackInSlot(i, ItemStack.EMPTY);
    }
    ListTag list = tags.getList(KEY_ITEMS).orElseGet(ListTag::new);
    for (int i = 0; i < list.size(); i++) {
      CompoundTag stackTag = list.getCompound(i).orElseGet(CompoundTag::new);
      int slot = stackTag.getInt("Slot").orElse(i);
      if (slot >= 0 && slot < itemHandler.getSlots()) {
        ItemStack stack = ItemStackNbtHelper.parse(stackTag);
        if (!stack.isEmpty()) {
          itemHandler.setStackInSlot(slot, stack);
        }
      }
    }
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    int slot = 0;
    for (ItemStack stack : input.listOrEmpty(KEY_ITEMS, ItemStack.OPTIONAL_CODEC)) {
      if (slot >= itemHandler.getSlots()) {
        break;
      }
      itemHandler.setStackInSlot(slot++, stack);
    }
  }
}
