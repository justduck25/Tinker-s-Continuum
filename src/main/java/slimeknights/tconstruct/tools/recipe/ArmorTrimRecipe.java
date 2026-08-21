package slimeknights.tconstruct.tools.recipe;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationContainer;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.modules.cosmetic.TrimModule;

import javax.annotation.Nullable;
import java.util.List;

public class ArmorTrimRecipe implements ITinkerStationRecipe, IMultiRecipe<IDisplayModifierRecipe> {
  private static final String TEMPLATE_SUFFIX = "_armor_trim_smithing_template";
  protected static final String KEY_INVALID_MATERIAL = TConstruct.makeTranslationKey("recipe", "modifier.armor_trim.invalid_material");
  protected static final String KEY_INVALID_PATTERN = TConstruct.makeTranslationKey("recipe", "modifier.armor_trim.invalid_pattern");

  @Getter
  private final Identifier id;

  public ArmorTrimRecipe(Identifier id) {
    this.id = id;
    ModifierRecipeLookup.addRecipeModifier(null, TinkerModifiers.trim);
  }

  private record TrimItems(ItemStack template, ItemStack material) {}

  private static boolean isTrimTemplate(ItemStack stack) {
    Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
    return key != null && key.getPath().endsWith(TEMPLATE_SUFFIX);
  }

  @Nullable
  private static Holder<TrimPattern> getPattern(RegistryAccess access, ItemStack template) {
    Identifier itemId = BuiltInRegistries.ITEM.getKey(template.getItem());
    if (itemId == null || !itemId.getPath().endsWith(TEMPLATE_SUFFIX)) {
      return null;
    }
    String pattern = itemId.getPath().substring(0, itemId.getPath().length() - TEMPLATE_SUFFIX.length());
    return access.lookupOrThrow(Registries.TRIM_PATTERN).get(Identifier.fromNamespaceAndPath(itemId.getNamespace(), pattern)).map(holder -> (Holder<TrimPattern>) holder).orElse(null);
  }

  @Nullable
  private static TrimItems findInputs(ITinkerStationContainer inv) {
    ItemStack template = ItemStack.EMPTY;
    ItemStack material = ItemStack.EMPTY;
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty()) {
        if (isTrimTemplate(stack)) {
          if (!template.isEmpty()) {
            return null;
          }
          template = stack;
        }
        if (stack.has(DataComponents.PROVIDES_TRIM_MATERIAL)) {
          if (!material.isEmpty()) {
            return null;
          }
          material = stack;
        }
      }
    }
    if (!material.isEmpty() && !template.isEmpty()) {
      return new TrimItems(template, material);
    }
    return null;
  }

  @Override
  public boolean matches(ITinkerStationContainer inv, Level world) {
    if (!inv.getTinkerableStack().is(TinkerTags.Items.TRIM)) {
      return false;
    }
    return findInputs(inv) != null;
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(ITinkerStationContainer inv, RegistryAccess access) {
    TrimItems trimItems = findInputs(inv);
    if (trimItems == null) {
      return RecipeResult.pass();
    }

    Holder<TrimMaterial> material = trimItems.material.get(DataComponents.PROVIDES_TRIM_MATERIAL);
    if (material == null) {
      return RecipeResult.failure(KEY_INVALID_MATERIAL, trimItems.material.getDisplayName());
    }
    ToolStack original = inv.getTinkerable();
    Holder<TrimPattern> pattern = null;
    if (!original.hasTag(TinkerTags.Items.TRIM_NO_PATTERN)) {
      pattern = getPattern(access, trimItems.template);
      if (pattern == null) {
        return RecipeResult.failure(KEY_INVALID_PATTERN, trimItems.template.getDisplayName());
      }
    }

    ToolStack tool = inv.getTinkerable().copy();
    ModDataNBT persistentData = tool.getPersistentData();
    ModifierId modifier = TinkerModifiers.trim.getId();
    persistentData.putString(TrimModule.materialKey(modifier), material.unwrapKey().map(key -> key.identifier().toString()).orElse(""));
    if (pattern != null) {
      persistentData.putString(TrimModule.patternKey(modifier), pattern.unwrapKey().map(key -> key.identifier().toString()).orElse(""));
    }

    if (tool.getModifierLevel(modifier) == 0) {
      tool.addModifier(modifier, 1);
    }
    ItemStack originalStack = inv.getTinkerableStack();
    ItemStack stack = tool.copyStack(originalStack, Math.min(originalStack.getCount(), DEFAULT_TOOL_STACK_SIZE));
    if (pattern != null) {
      stack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
    }
    return LazyToolStack.success(stack);
  }

  @Override
  public RecipeSerializer<? extends Recipe<ITinkerStationContainer>> getSerializer() {
    return TinkerModifiers.armorTrimSerializer.get();
  }

  private List<IDisplayModifierRecipe> displayRecipes = null;

  @Override
  public List<IDisplayModifierRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      List<ItemStack> trims = BuiltInRegistries.ITEM.stream().filter(item -> BuiltInRegistries.ITEM.getKey(item).getPath().endsWith(TEMPLATE_SUFFIX)).map(ItemStack::new).toList();
      List<ItemStack> toolInputs = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.TRIM).map(IModifiableDisplay::getDisplayStack).toList();
      List<ItemStack> materials = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, ItemTags.TRIM_MATERIALS).map(ItemStack::new).filter(stack -> stack.has(DataComponents.PROVIDES_TRIM_MATERIAL)).toList();
      if (!trims.isEmpty() && !toolInputs.isEmpty() && !materials.isEmpty()) {
        Identifier id = getId();
        displayRecipes = materials.stream().<IDisplayModifierRecipe>map(stack -> new DisplayRecipe(id, toolInputs, trims, stack, stack.get(DataComponents.PROVIDES_TRIM_MATERIAL), access)).toList();
      } else {
        displayRecipes = List.of();
      }
    }
    return displayRecipes;
  }

  private static class DisplayRecipe implements IDisplayModifierRecipe {
    private static final IntRange LEVELS = new IntRange(1, 1);
    private final ModifierEntry RESULT = new ModifierEntry(TinkerModifiers.trim, 1);

    @Getter
    private final Identifier recipeId;
    @Getter
    private final List<ItemStack> toolWithoutModifier;
    @Getter
    private final List<ItemStack> toolWithModifier;
    private final List<ItemStack> trim;
    private final List<ItemStack> material;
    @Getter
    private final Component variant;

    public DisplayRecipe(Identifier id, List<ItemStack> tools, List<ItemStack> trim, ItemStack materialStack, Holder<TrimMaterial> holder, RegistryAccess access) {
      this.recipeId = id;
      TrimMaterial material = holder.value();
      toolWithoutModifier = tools;
      this.trim = trim;
      this.material = List.of(materialStack);
      this.variant = material.description().plainCopy();

      String materialName = holder.unwrapKey().map(key -> key.identifier().toString()).orElse("");
      List<ModifierEntry> results = List.of(RESULT);
      Identifier key = TrimModule.materialKey(TinkerModifiers.trim.getId());
      toolWithModifier = tools.stream().map(stack -> {
        ItemStack result = IDisplayModifierRecipe.withModifiers(stack, results, data -> data.putString(key, materialName));
        if (!trim.isEmpty()) {
          Holder<TrimPattern> pattern = getPattern(access, trim.get(0));
          if (pattern != null) {
            result.set(DataComponents.TRIM, new ArmorTrim(holder, pattern));
          }
        }
        return result;
      }).toList();
    }

    @Override
    public int getInputCount() {
      return 2;
    }

    @Override
    public List<ItemStack> getDisplayItems(int slot) {
      return switch (slot) {
        case 0 -> trim;
        case 1 -> material;
        default -> List.of();
      };
    }

    @Override
    public ModifierEntry getDisplayResult() {
      return RESULT;
    }

    @Override
    public IntRange getLevel() {
      return LEVELS;
    }
  }
}
