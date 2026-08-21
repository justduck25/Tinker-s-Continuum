package slimeknights.tconstruct.tables.block.entity.table;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.shared.inventory.ConfigurableInvWrapperCapability;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.inventory.CraftingContainerWrapper;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer.ILazyCrafter;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;
import slimeknights.tconstruct.tables.network.UpdateCraftingRecipePacket;

import javax.annotation.Nullable;
import java.util.List;

public class CraftingStationBlockEntity extends RetexturedTableBlockEntity implements ILazyCrafter {
  public static final Component UNCRAFTABLE = TConstruct.makeTranslation("gui", "crafting_station.uncraftable");
  private static final Component NAME = TConstruct.makeTranslation("gui", "crafting_station");

  /** Last crafted crafting recipe */
  @Nullable
  private CraftingRecipe lastRecipe;
  /** Last synced crafting result, used for client display */
  private ItemStack lastResult = ItemStack.EMPTY;
  /** Result inventory, lazy loads results */
  @Getter
  private final LazyResultContainer craftingResult;
  /** Crafting inventory for the recipe calls */
  @Getter
  private final CraftingContainerWrapper craftingInventory;

  public CraftingStationBlockEntity(BlockPos pos, BlockState state) {
    super(TinkerTables.craftingStationTile.get(), pos, state, NAME, 9);
    this.itemHandler = new ConfigurableInvWrapperCapability(this, false, false);
    this.craftingInventory = new CraftingContainerWrapper(this, 3, 3);
    this.craftingResult = new LazyResultContainer(this);
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new CraftingStationContainerMenu(menuId, playerInventory, this);
  }

  @Override
  public AABB getRenderBoundingBox() {
    return new AABB(Vec3.atLowerCornerOf(worldPosition), Vec3.atLowerCornerOf(worldPosition.offset(1, 2, 1)));
  }

  /* Crafting */

  @Override
  public ItemStack calcResult(@Nullable Player player) {
    if (this.level == null || isEmpty()) {
      return ItemStack.EMPTY;
    }
    // assume empty unless we learn otherwise
    ItemStack result = ItemStack.EMPTY;
    CraftingInput input = CraftingInput.of(craftingInventory.getWidth(), craftingInventory.getHeight(), craftingInventory.getItems());
    if (!this.level.isClientSide() && this.level.getServer() != null) {
      RecipeManager manager = this.level.getServer().getRecipeManager();

      CommonHooks.setCraftingPlayer(player);
      try {
        // first, try the cached recipe
        CraftingRecipe recipe = lastRecipe;
        // if it does not match, find a new recipe
        // note we intentionally have no player access during matches, that could lead to an unstable recipe
        if (recipe == null || !recipe.matches(input, this.level)) {
          var holder = manager.getRecipeFor(RecipeType.CRAFTING, input, this.level);
          recipe = holder.map(RecipeHolder::value).orElse(null);
        }

        // if we have a recipe, fetch its result
        if (recipe != null) {
          result = recipe.assemble(input);

          // sync if the recipe or result is different
          boolean changed = recipe != lastRecipe || !ItemStack.matches(result, this.lastResult);
          if (changed) {
            this.lastRecipe = recipe;
            this.lastResult = result.copy();
            this.syncToRelevantPlayers(this::syncRecipe);
          }
        } else if (this.lastRecipe != null || !this.lastResult.isEmpty()) {
          this.lastRecipe = null;
          this.lastResult = ItemStack.EMPTY;
          this.syncToRelevantPlayers(this::syncRecipe);
        }
      } finally {
        CommonHooks.setCraftingPlayer(null);
      }
    }
    else if (this.lastRecipe != null && this.lastRecipe.matches(input, this.level)) {
      CommonHooks.setCraftingPlayer(player);
      try {
        result = this.lastRecipe.assemble(input);
      } finally {
        CommonHooks.setCraftingPlayer(null);
      }
    }
    return result;
  }
  /**
   * Gets the player sensitive crafting result, also validating the player has access to this recipe
   * @param player  Player
   * @return  Player sensitive result
   */
  public ItemStack getResultForPlayer(Player player) {
    CommonHooks.setCraftingPlayer(player);
    try {
      CraftingRecipe recipe = this.lastRecipe; // local variable just to prevent race conditions if the field changes, though that is unlikely
      CraftingInput input = CraftingInput.of(craftingInventory.getWidth(), craftingInventory.getHeight(), craftingInventory.getItems());

      // try matches again now that we have player access
      if (recipe == null || this.level == null || !recipe.matches(input, level)) {
        return ItemStack.EMPTY;
      }

      // check if the player has access to the recipe, if not give up
      // Disabled because this is an absolute mess of logic, and the gain is rather small, treating this like a furnace instead
      // note the gamerule is client side only anyways, so you would have to sync it, such as in the container
      // if you want limited crafting, disable the crafting station, the design of the station is incompatible with the game rule and vanilla syncing
//    if (!recipe.isDynamic() && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)) {
//      // mojang, why can't PlayerEntity just have a RecipeBook getter, why must I go through the sided classes? grr
//      boolean locked;
//      if (!world.isRemote) {
//        locked = player instanceof ServerPlayerEntity && !((ServerPlayerEntity) player).getRecipeBook().isUnlocked(recipe);
//      } else {
//        locked = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> player instanceof ClientPlayerEntity && !((ClientPlayerEntity) player).getRecipeBook().isUnlocked(recipe));
//      }
//      // if the player cannot craft this, block crafting
//      if (locked) {
//        ForgeHooks.setCraftingPlayer(null);
//        return ItemStack.EMPTY;
//      }
//    }

      ItemStack result = recipe.assemble(input);
      return result;
    } finally {
      CommonHooks.setCraftingPlayer(null);
    }
  }
  /**
   * Removes the result from this inventory, updating inputs and triggering recipe hooks
   * @param player  Player taking result
   * @param result  Result removed
   * @param amount  Number of times crafted
   */
  public void takeResult(Player player, ItemStack result, int amount) {
    CraftingRecipe recipe = this.lastRecipe; // local variable just to prevent race conditions if the field changes, though that is unlikely
    if (recipe == null || this.level == null) {
      return;
    }

    // fire crafting events
    if (!recipe.isSpecial() && level != null && !level.isClientSide()) {
      CraftingInput input = CraftingInput.of(craftingInventory.getWidth(), craftingInventory.getHeight(), craftingInventory.getItems());
      level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).ifPresent(holder -> {
        player.awardRecipes(List.of(holder));
      });
    }
    result.onCraftedBy(player, amount);
    EventHooks.firePlayerCraftingEvent(player, result, this.craftingInventory);

    // update all slots in the inventory
    // remove remaining items
    NonNullList<ItemStack> remaining;
    CommonHooks.setCraftingPlayer(player);
    try {
      remaining = recipe.getRemainingItems(CraftingInput.of(craftingInventory.getWidth(), craftingInventory.getHeight(), craftingInventory.getItems()));
    } finally {
      CommonHooks.setCraftingPlayer(null);
    }
    for (int i = 0; i < remaining.size(); ++i) {
      ItemStack original = this.getItem(i);
      ItemStack newStack = remaining.get(i);

      // if empty or size 1, set directly (decreases by 1)
      if (original.isEmpty() || original.getCount() == 1) {
        this.setItem(i, newStack);
      }
      else if (ItemStack.isSameItemSameComponents(original, newStack)) {
        // if matching, merge (decreasing by 1
        newStack.grow(original.getCount() - 1);
        this.setItem(i, newStack);
      }
      else {
        // directly update the slot
        this.setItem(i, copyStackWithSize(original, original.getCount() - 1));
        // otherwise, drop the item as the player
        if (!newStack.isEmpty() && !player.getInventory().add(newStack)) {
          player.drop(newStack, false);
        }
      }
    }
  }

  /** Sends a message alerting the player this item is currently uncraftable, typically due to gamerules */
  public void notifyUncraftable(Player player) {
    // if empty, send a message so the player is more aware of why they cannot craft it, sent to chat as status bar is not visible
    // TODO: consider moving into the UI somewhere
    if (level != null && !level.isClientSide()) {
      player.sendSystemMessage(CraftingStationBlockEntity.UNCRAFTABLE);
    }
  }

  @Override
  public void onCraft(Player player, ItemStack result, int amount) {
    // update the inputs and trigger recipe hooks
    if (amount != 0 && !result.isEmpty()) {
      takeResult(player, result, amount);
    }
  }

  @Override
  public void setItem(int slot, ItemStack itemstack) {
    super.setItem(slot, itemstack);
    // clear the crafting result when the matrix changes so we recalculate the result
    this.craftingResult.clearContent();
    if (this.level != null && !this.level.isClientSide()) {
      syncToRelevantPlayers(this::refreshResult);
    }
  }

  /* Syncing */

  /** Recalculates and sends the current crafting result to one player. */
  public void refreshResult(Player player) {
    if (this.level != null && !this.level.isClientSide()) {
      this.craftingResult.clearContent();
      ItemStack result = calcResult(player);
      this.lastResult = result.copy();
      syncRecipe(player);
    }
  }
  /**
   * Sends the current recipe to the given player
   * @param player  Player to send an update to
   */
  public void syncRecipe(Player player) {
    // must have a server world
    if (this.level != null && !this.level.isClientSide() && player instanceof ServerPlayer) {
      TinkerNetwork.getInstance().sendTo(new UpdateCraftingRecipePacket(this.worldPosition, this.lastResult), (ServerPlayer) player);
    }
  }
  /**
   * Updates the recipe from the server
   * @param recipe  New recipe
   */
  public void updateRecipe(CraftingRecipe recipe) {
    this.lastRecipe = recipe;
    this.craftingResult.clearContent();
  }

  /** Updates the displayed crafting result from the server. Crafting remains server-authoritative. */
  public void updateSyncedResult(ItemStack result) {
    this.lastResult = result.copy();
    this.craftingResult.setSyncedResult(result);
  }
  private static ItemStack copyStackWithSize(ItemStack stack, int size) {
    return stack.copyWithCount(size);
  }


}
