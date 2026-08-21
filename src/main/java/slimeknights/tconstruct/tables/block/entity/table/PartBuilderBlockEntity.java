package slimeknights.tconstruct.tables.block.entity.table;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.material.IMaterialValue;
import slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.shared.inventory.ConfigurableInvWrapperCapability;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer.ILazyCrafter;
import slimeknights.tconstruct.tables.block.entity.inventory.PartBuilderContainerWrapper;
import slimeknights.tconstruct.tables.menu.PartBuilderContainerMenu;
import slimeknights.tconstruct.tables.network.UpdatePartBuilderButtonsPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PartBuilderBlockEntity extends RetexturedTableBlockEntity implements ILazyCrafter {
  /** First slot containing materials */
  public static final int MATERIAL_SLOT = 0;
  /** Second slot containing the patterns */
  public static final int PATTERN_SLOT = 1;
  /** Title for the GUI */
  private static final Component NAME = TConstruct.makeTranslation("gui", "part_builder");

  /** Result inventory, lazy loads results */
  @Getter
  private final LazyResultContainer craftingResult;
  /** Crafting inventory for the recipe calls */
  @Getter
  private final PartBuilderContainerWrapper inventoryWrapper;

  /* Current buttons to display */
  @Nullable
  private Map<Pattern,IPartBuilderRecipe> recipes = null;
  @Nullable
  private List<Pattern> sortedButtons = null;
  /** Client-side lightweight recipe data synced from the server, as MC 26.1 clients do not receive full custom recipes. */
  private Map<Pattern,IPartBuilderRecipe> clientRecipes = Collections.emptyMap();
  private List<Pattern> clientSortedButtons = Collections.emptyList();
  /** Currently selected recipe index */
  private Pattern selectedPattern = null;
  /** Index of the currently selected pattern */
  private int selectedPatternIndex = -2;

  public PartBuilderBlockEntity(BlockPos pos, BlockState state) {
    super(TinkerTables.partBuilderTile.get(), pos, state, NAME, 2);
    this.itemHandler = new ConfigurableInvWrapperCapability(this, false, false);    this.inventoryWrapper = new PartBuilderContainerWrapper(this);
    this.craftingResult = new LazyResultContainer(this);
  }

  /**
   * Gets a map of all recipes for the current inputs
   * @return  List of recipes for the current inputs
   */
  protected Map<Pattern,IPartBuilderRecipe> getCurrentRecipes() {
    if (level == null) {
      return Collections.emptyMap();
    }
    if (recipes == null) {
      if (level.isClientSide()) {
        return clientRecipes;
      }
      // no recipes if we lack a pattern
      if (getItem(PATTERN_SLOT).isEmpty()) {
        recipes = Collections.emptyMap();
        sortedButtons = Collections.emptyList();
      } else {
        record PatternRecipe(Pattern pattern, IPartBuilderRecipe recipe) {}
        // fetch all recipes that can match these inputs, the map ensures the patterns are unique
        // The full recipe scan is server-only; clients use the lightweight entries synced by updateClientButtons().
        if (level.recipeAccess() instanceof RecipeManager manager) {
          recipes = manager.recipeMap().byType(TinkerRecipeTypes.PART_BUILDER.get()).stream()
                         .filter(r -> r.value().partialMatch(inventoryWrapper))
                         .sorted(Comparator.comparing(r -> r.id().identifier()))
                         .flatMap(r -> r.value().getPatterns(inventoryWrapper).map(p -> new PatternRecipe(p, r.value())))
                         .collect(Collectors.toMap(PatternRecipe::pattern, PatternRecipe::recipe, (a, b) -> a));
        } else {
          recipes = Collections.emptyMap();
        }
        sortedButtons = recipes.entrySet()
                               .stream()
                               .sorted(Comparator.<Entry<Pattern,IPartBuilderRecipe>>comparingInt(ent -> ent.getValue().getCost()).thenComparing((a, b) -> a.getKey().getId().compareTo(b.getKey().getId())))
                               .map(Entry::getKey).collect(Collectors.toList());
      }
    }
    return recipes;
  }

  /** Gets the list of sorted buttons */
  public List<Pattern> getSortedButtons() {
    if (level == null) {
      return Collections.emptyList();
    }
    if (level.isClientSide()) {
      return clientSortedButtons;
    }
    if (sortedButtons == null) {
      getCurrentRecipes();
    }
    return sortedButtons;
  }

  /** Sends lightweight button data to one player. */
  public void syncButtons(Player player) {
    if (level != null && !level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      List<UpdatePartBuilderButtonsPacket.Entry> entries = getSortedButtons().stream()
        .map(pattern -> {
          IPartBuilderRecipe recipe = getCurrentRecipes().get(pattern);
          ItemStack result = ItemStack.EMPTY;
          if (recipe != null && recipe.matches(inventoryWrapper, level)) {
            result = recipe.assemble(inventoryWrapper, pattern);
          }
          return new UpdatePartBuilderButtonsPacket.Entry(pattern, recipe == null ? 1 : recipe.getCost(), recipe != null && recipe.allowUncraftable(), result);
        })
        .toList();
      TinkerNetwork.getInstance().sendTo(new UpdatePartBuilderButtonsPacket(worldPosition, entries), serverPlayer);
    }
  }

  /** Sends lightweight button data to everyone viewing this table. */
  private void syncButtonsToRelevantPlayers() {
    syncToRelevantPlayers(this::syncButtons);
  }

  /** Updates client-side button data from the server. */
  public void updateClientButtons(List<UpdatePartBuilderButtonsPacket.Entry> entries) {
    Map<Pattern,IPartBuilderRecipe> recipes = entries.stream().collect(Collectors.toMap(UpdatePartBuilderButtonsPacket.Entry::pattern, ClientPartBuilderRecipe::new, (a, b) -> a));
    this.clientRecipes = recipes;
    this.clientSortedButtons = entries.stream().map(UpdatePartBuilderButtonsPacket.Entry::pattern).toList();
    this.selectedPatternIndex = -2;
    this.craftingResult.clearContent();
  }

  /** Gets the index of the selected pattern */
  public int getSelectedIndex() {
    if (selectedPatternIndex == -2) {
      if (selectedPattern != null) {
        selectedPatternIndex = getSortedButtons().indexOf(selectedPattern);
      } else {
        selectedPatternIndex = -1;
      }
    }
    return selectedPatternIndex;
  }

  /**
   * Gets the currently selected recipe
   * @return  Selected recipe, or null if invalid or no recipe
   */
  @Nullable
  public IPartBuilderRecipe getPartRecipe() {
    if (selectedPattern != null) {
      return getCurrentRecipes().get(selectedPattern);
    }
    return null;
  }

  /** Gets the first available recipe */
  @Nullable
  public IPartBuilderRecipe getFirstRecipe() {
    List<Pattern> sortedButtons = getSortedButtons();
    if (sortedButtons.isEmpty()) {
      return null;
    }
    return getCurrentRecipes().get(sortedButtons.get(0));
  }

  /**
   * Gets the material recipe for the material slot
   * @return  Material slot
   */
  @Nullable
  public IMaterialValue getMaterialRecipe() {
    return inventoryWrapper.getMaterial();
  }

  /** If true, hides the uncraftable error message on the screen */
  public boolean allowUncraftable() {
    // if a recipe is selected, its behavior dictates the message
    IPartBuilderRecipe recipe = getPartRecipe();
    if (recipe != null) {
      return recipe.allowUncraftable();
    }
    // otherwise, if any button says its allowed then its allowed
    for (IPartBuilderRecipe button : getCurrentRecipes().values()) {
      if (button.allowUncraftable()) {
        return true;
      }
    }
    // no button says its allowed? then its not
    return false;
  }

  /**
   * Refreshes the current recipe
   * @param refreshRecipeList  If true, refreshes the full recipe list too
   */
  private void refresh(boolean refreshRecipeList) {
    if (refreshRecipeList) {
      this.recipes = null;
      this.sortedButtons = null;
    }
    this.selectedPatternIndex = -2;
    this.craftingResult.clearContent();
    // update screen display
    if (refreshRecipeList) {
      syncButtonsToRelevantPlayers();
      syncScreenToRelevantPlayers();
    }
  }

  /**
   * Selects a recipe in the table
   * @param pattern  New pattern
   */
  public void selectRecipe(@Nullable Pattern pattern) {
    if (pattern != null && getCurrentRecipes().containsKey(pattern)) {
      selectedPattern = pattern;
    } else {
      selectedPattern = null;
    }
    refresh(false);
  }

  /**
   * Selects a pattern by index
   * @param index  New index
   */
  public void selectRecipe(int index) {
    if (index < 0) {
      selectedPattern = null;
    } else {
      List<Pattern> list = getSortedButtons();
      if (index < list.size()) {
        selectedPattern = list.get(index);
      } else {
        selectedPattern = null;
      }
    }
    refresh(false);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    ItemStack original = getItem(slot);
    super.setItem(slot, stack);
    if (slot == MATERIAL_SLOT) {
      // if item or NBT changed, update
      if (!ItemStack.isSameItemSameComponents(original, stack)) {
        this.inventoryWrapper.refreshMaterial();
        refresh(true);
        // if size changed, we are still the same material but might no longer have enough
        // same stack calling this method typically indicates a size change, stacks being mutable is annoying
      } else if (original.getCount() != stack.getCount() || original == stack) {
        this.craftingResult.clearContent();
        syncButtonsToRelevantPlayers();
        syncScreenToRelevantPlayers();
      }
      // any other slot, only an item change means update
    } else if (original.getItem() != stack.getItem()) {
      refresh(true);
    }
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new PartBuilderContainerMenu(menuId, playerInventory, this);
  }

  @Override
  public ItemStack calcResult(@Nullable Player player) {
    if (level != null) {
      IPartBuilderRecipe recipe = getPartRecipe();
      if (recipe != null && recipe.matches(inventoryWrapper, level)) {
        return recipe.assemble(inventoryWrapper, selectedPattern);
      }
    }
    return ItemStack.EMPTY;
  }

  /**
   * Shrinks the given slot
   * @param slot    Slot
   * @param amount  Amount to shrink
   */
  private void shrinkSlot(int slot, int amount, Player player) {
    if (amount <= 0) {
      return;
    }
    ItemStack stack = getItem(slot);
    if (!stack.isEmpty()) {
      var remainder = stack.getCraftingRemainder();
      ItemStack container = remainder != null ? remainder.create().copy() : ItemStack.EMPTY;
      if (amount > 1) {
        container.setCount(container.getCount() * amount);
      }
      if (stack.getCount() <= amount) {
        setItem(slot, container);
      } else {
        stack.shrink(amount);
        player.getInventory().placeItemBackInInventory(container);
      }
    }
  }

  @Override
  public void onCraft(Player player, ItemStack result, int amount) {
    if (amount == 0 || this.level == null) {
      return;
    }
    // the recipe should match if we got this far, but being null is a problem
    IPartBuilderRecipe recipe = getPartRecipe();
    if (recipe == null) {
      return;
    }

    // we are definitely crafting at this point
    result.onCraftedBy(player, amount);
    EventHooks.firePlayerCraftingEvent(player, result, this.inventoryWrapper);
    this.playCraftSound(player);

    // give the player any leftovers
    if (level != null && !level.isClientSide()) {
      ItemStack leftover = recipe.getLeftover(inventoryWrapper, selectedPattern);
      if (!leftover.isEmpty()) {
        player.getInventory().placeItemBackInInventory(leftover);
      }
    }

    // shrink the inputs
    shrinkSlot(MATERIAL_SLOT, recipe.getItemsUsed(inventoryWrapper), player);
    if (!getItem(PATTERN_SLOT).is(TinkerTags.Items.REUSABLE_PATTERNS)) {
      shrinkSlot(PATTERN_SLOT, 1, player);
    }

    // sync display, mainly for the material value
    syncScreenToRelevantPlayers();
  }

  /** Client-only lightweight recipe used for rendering Part Builder buttons and costs. Crafting remains server-authoritative. */
  private record ClientPartBuilderRecipe(Pattern pattern, int cost, boolean allowUncraftable, ItemStack result) implements IPartBuilderRecipe {
    private ClientPartBuilderRecipe(UpdatePartBuilderButtonsPacket.Entry entry) {
      this(entry.pattern(), entry.cost(), entry.allowUncraftable(), entry.result().copy());
    }

    @Override
    public Pattern getPattern() {
      return pattern;
    }

    @Override
    public int getCost() {
      return cost;
    }

    @Override
    public boolean allowUncraftable() {
      return allowUncraftable;
    }

    @Override
    public boolean partialMatch(slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderContainer inv) {
      return true;
    }

    @Override
    public boolean matches(slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderContainer inv, net.minecraft.world.level.Level level) {
      return !result.isEmpty();
    }

    @Override
    public ItemStack assemble(slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderContainer inv) {
      return result.copy();
    }

    @Override
    public ItemStack assemble(slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderContainer inv, Pattern pattern) {
      return result.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderContainer>> getSerializer() {
      return TinkerTables.partRecipeSerializer.get();
    }
  }
}