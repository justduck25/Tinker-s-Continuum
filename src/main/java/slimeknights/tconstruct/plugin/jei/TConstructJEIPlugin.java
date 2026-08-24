package slimeknights.tconstruct.plugin.jei;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.world.item.crafting.RecipeMap;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.plugin.jei.MoldingRecipeCategory;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.plugin.jei.entity.DefaultEntityMeltingRecipe;
import slimeknights.tconstruct.plugin.jei.entity.EntityMeltingRecipeCategory;
import slimeknights.tconstruct.plugin.jei.melting.MeltingFuelHandler;
import java.util.List;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.partbuilder.IDisplayPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.plugin.jei.casting.CastingBasinCategory;
import slimeknights.tconstruct.plugin.jei.casting.CastingTableCategory;
import slimeknights.tconstruct.plugin.jei.entity.SeveringCategory;
import slimeknights.tconstruct.plugin.jei.melting.FoundryCategory;
import slimeknights.tconstruct.plugin.jei.melting.MeltingCategory;
import slimeknights.tconstruct.plugin.jei.AlloyRecipeCategory;
import slimeknights.tconstruct.plugin.jei.modifiers.ModifierRecipeCategory;
import slimeknights.tconstruct.plugin.jei.modifiers.ModifierWorktableCategory;
import slimeknights.tconstruct.plugin.jei.partbuilder.PartBuilderCategory;
import slimeknights.tconstruct.plugin.jei.partbuilder.PatternIngredientHelper;
import slimeknights.tconstruct.plugin.jei.partbuilder.PatternIngredientRenderer;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialsRecipe;
import slimeknights.tconstruct.library.recipe.material.ShapelessMaterialsRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.plugin.jei.transfer.CraftingStationTransferInfo;
import slimeknights.tconstruct.plugin.jei.material.MaterialsCraftingExtension;
import slimeknights.tconstruct.plugin.jei.material.ShapedMaterialsExtension;
import slimeknights.tconstruct.plugin.jei.transfer.TinkerStationTransferInfo;
import slimeknights.tconstruct.plugin.jei.transfer.ToolInventoryTransferInfo;
import slimeknights.tconstruct.plugin.jei.util.ToolPartSubtypeInterpreter;
import slimeknights.tconstruct.plugin.jei.util.ToolSubtypeInterpreter;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.item.CreativeSlotItem;
import slimeknights.tconstruct.tools.item.ModifierCrystalItem;

@JeiPlugin
@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class TConstructJEIPlugin implements IModPlugin {
  /** Client recipe map received after tags are bound; used for multi-recipe expansion. */
  private static RecipeMap clientRecipeMap = RecipeMap.EMPTY;

  @SubscribeEvent
  public static void onRecipesReceived(RecipesReceivedEvent event) {
    clientRecipeMap = event.getRecipeMap();}
  @Override
  public Identifier getPluginUid() {
    return TConstruct.getResource("jei");
  }

  @Override
  public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
    registration.getCraftingCategory().addExtension(ShapedMaterialRecipe.class, new ShapedMaterialExtension());
  }

  @Override
  public void registerIngredients(IModIngredientRegistration registration) {
    registration.register(TConstructJEIConstants.PATTERN_TYPE, List.of(),
      new PatternIngredientHelper(), PatternIngredientRenderer.INSTANCE, net.minecraft.resources.Identifier.CODEC.xmap(slimeknights.tconstruct.library.recipe.partbuilder.Pattern::new, slimeknights.tconstruct.library.recipe.partbuilder.Pattern::getId));    registration.register(TConstructJEIConstants.MODIFIER_TYPE, List.of(),
      new slimeknights.tconstruct.plugin.jei.modifiers.ModifierIngredientHelper(),
      slimeknights.tconstruct.plugin.jei.modifiers.ModifierBookmarkIngredientRenderer.INSTANCE,
      net.minecraft.resources.Identifier.CODEC.xmap(
        id -> new slimeknights.tconstruct.library.modifiers.ModifierEntry(new slimeknights.tconstruct.library.modifiers.ModifierId(id), 1),
        entry -> entry.getId().getId()));    registration.register(TConstructJEIConstants.SLOT_TYPE, List.of(),
      new slimeknights.tconstruct.plugin.jei.modifiers.SlotIngredientHelper(),
      slimeknights.tconstruct.plugin.jei.modifiers.SlotIngredientRenderer.INGREDIENT,
      com.mojang.serialization.codecs.RecordCodecBuilder.create(instance -> instance.group(
        com.mojang.serialization.Codec.STRING.xmap(
          slimeknights.tconstruct.library.tools.SlotType::getOrCreate,
          slimeknights.tconstruct.library.tools.SlotType::getName).fieldOf("type").forGetter(slots -> slots.type()),
        com.mojang.serialization.Codec.INT.fieldOf("count").forGetter(slots -> slots.count())
      ).apply(instance, slimeknights.tconstruct.library.tools.SlotType.SlotCount::new)));
  }
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    ISubtypeInterpreter<ItemStack> tables = (stack, context) ->
      context == UidContext.Ingredient ? RetexturedHelper.getTextureName(stack) : new String();
    ISubtypeInterpreter<ItemStack> anvils = (stack, context) -> {
      if (context == UidContext.Ingredient) {
        String name = RetexturedHelper.getTextureName(stack);
        if (!name.isEmpty()) {
          return '#' + name;
        }
        return ToolPartSubtypeInterpreter.INSTANCE.getSubtypeData(stack, UidContext.Ingredient).toString();
      }
      return new String();
    };
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerTables.craftingStation.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerTables.partBuilder.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerTables.tinkerStation.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerTables.modifierWorktable.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerTables.tinkersAnvil.asItem(), anvils);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerTables.scorchedAnvil.asItem(), anvils);

    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.smelteryController.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.foundryController.asItem(), tables);
    registerCustomDataSubtype(registration, TinkerSmeltery.searedMelter.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.searedHeater.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.scorchedAlloyer.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.searedTank.values().get(0).asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.scorchedTank.values().get(0).asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.searedCastingTank.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.searedFluidCannon.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.scorchedFluidCannon.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.endFluidCannon.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.searedLantern.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.scorchedLantern.asItem());
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.searedDrain.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.scorchedDrain.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.searedDuct.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.scorchedDuct.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.searedChute.asItem(), tables);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.scorchedChute.asItem(), tables);
    registerCustomDataSubtype(registration, TinkerSmeltery.searedGlass.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.searedTintedGlass.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.searedSoulGlass.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.searedGlassPane.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.searedSoulGlassPane.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.scorchedGlass.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.scorchedTintedGlass.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.scorchedSoulGlass.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.scorchedGlassPane.asItem());
    registerCustomDataSubtype(registration, TinkerSmeltery.scorchedSoulGlassPane.asItem());

    registerCustomDataSubtype(registration, TinkerSmeltery.copperCan.asItem());
    registerCustomDataSubtype(registration, TinkerFluids.venomBottle.asItem());
    registerCustomDataSubtype(registration, TinkerFluids.slimeBottle.get(SlimeType.EARTH));
    registerCustomDataSubtype(registration, TinkerFluids.slimeBottle.get(SlimeType.SKY));
    registerCustomDataSubtype(registration, TinkerFluids.slimeBottle.get(SlimeType.ENDER));
    registerCustomDataSubtype(registration, TinkerFluids.slimeBottle.get(SlimeType.ICHOR));
    registerCustomDataSubtype(registration, TinkerFluids.meatSoupBowl.asItem());
    registration.registerFromDataComponentTypes(TinkerFluids.potion.getBucket(), DataComponents.POTION_CONTENTS);
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.TOOL_PARTS)) {
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, holder.value(), ToolPartSubtypeInterpreter.INSTANCE);
    }
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.MULTIPART_TOOL)) {
      Item item = holder.value();
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item,
        holder.is(TinkerTags.Items.SINGLEPART_TOOL) ? ToolSubtypeInterpreter.FIRST : ToolSubtypeInterpreter.INGREDIENT);
    }

    // Dynamic variants use CUSTOM_DATA to distinguish their actual slot/modifier.
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerModifiers.creativeSlotItem.asItem(), (stack, context) -> {
      SlotType slotType = CreativeSlotItem.getSlot(stack);
      return slotType != null ? slotType.getName() : "";
    });
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerModifiers.modifierCrystal.asItem(), (stack, context) -> {
      var modifier = ModifierCrystalItem.getModifier(stack);
      return modifier == null ? "" : modifier.toString();
    });
  }

  private static void registerCustomDataSubtype(ISubtypeRegistration registration, Item item) {
    registration.registerFromDataComponentTypes(item, DataComponents.CUSTOM_DATA);
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(new CraftingStationTransferInfo());
    IRecipeTransferHandlerHelper helper = registration.getTransferHelper();
    registration.addRecipeTransferHandler(new ToolInventoryTransferInfo(helper), RecipeTypes.CRAFTING);
    registration.addRecipeTransferHandler(new TinkerStationTransferInfo<>(TConstructJEIConstants.TOOL_BUILDING, helper), TConstructJEIConstants.TOOL_BUILDING);
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
    registration.addRecipeCategories(new CastingBasinCategory(guiHelper));
    registration.addRecipeCategories(new CastingTableCategory(guiHelper));
    registration.addRecipeCategories(new MeltingCategory(guiHelper));
    registration.addRecipeCategories(new EntityMeltingRecipeCategory(guiHelper));
    registration.addRecipeCategories(new FoundryCategory(guiHelper));
    registration.addRecipeCategories(new AlloyRecipeCategory(guiHelper));
    registration.addRecipeCategories(new MoldingRecipeCategory(guiHelper));
    registration.addRecipeCategories(new PartBuilderCategory(guiHelper));
    registration.addRecipeCategories(new ModifierRecipeCategory(guiHelper));
    registration.addRecipeCategories(new ModifierWorktableCategory(guiHelper));
    registration.addRecipeCategories(new SeveringCategory(guiHelper));
    registration.addRecipeCategories(new ToolBuildingCategory(guiHelper));
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    Minecraft minecraft = Minecraft.getInstance();if (minecraft.level == null) {return;
    }
    MaterialRecipeCache.setDisplayRegistryAccess(minecraft.level.registryAccess());
    net.minecraft.world.item.crafting.RecipeManager manager;
    if (minecraft.level.recipeAccess() instanceof net.minecraft.world.item.crafting.RecipeManager clientManager) {
      manager = clientManager;
    } else if (minecraft.getSingleplayerServer() != null) {
      manager = minecraft.getSingleplayerServer().getRecipeManager();
    } else {return;
    }    List<IDisplayPartBuilderRecipe> recipes;
    if (!clientRecipeMap.byType(TinkerRecipeTypes.PART_BUILDER.get()).isEmpty()) {
      recipes = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(),
        clientRecipeMap.byType(TinkerRecipeTypes.PART_BUILDER.get()).stream(),
        IDisplayPartBuilderRecipe.class);
    } else {
      recipes = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager,
        TinkerRecipeTypes.PART_BUILDER.get(), IDisplayPartBuilderRecipe.class);
    }registration.addRecipes(TConstructJEIConstants.PART_BUILDER, recipes);

    // casting
    List<IDisplayableCastingRecipe> castingBasinRecipes = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.CASTING_BASIN.get(), IDisplayableCastingRecipe.class);
    registration.addRecipes(TConstructJEIConstants.CASTING_BASIN, castingBasinRecipes);
    List<IDisplayableCastingRecipe> castingTableRecipes = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.CASTING_TABLE.get(), IDisplayableCastingRecipe.class);
    registration.addRecipes(TConstructJEIConstants.CASTING_TABLE, castingTableRecipes);

    // smeltery melting and alloying
    List<MeltingRecipe> meltingRecipes = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.MELTING.get(), MeltingRecipe.class);
    // Hide compat recipes whose item tags resolve to no registered item in this runtime.
    meltingRecipes.removeIf(recipe -> MaterialRecipeCache.getDisplayItems(recipe.getInput()).isEmpty());
    registration.addRecipes(TConstructJEIConstants.MELTING, meltingRecipes);
    registration.addRecipes(TConstructJEIConstants.FOUNDRY, meltingRecipes);
    MeltingFuelHandler.setMeltngFuels(RecipeHelper.getRecipes(manager, TinkerRecipeTypes.FUEL.get(), MeltingFuel.class));

    // Entity melting: one aggregated JEI display recipe for all entity types.
    List<EntityMeltingRecipe> entityMeltingRecipes = new java.util.ArrayList<>(RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.ENTITY_MELTING.get(), EntityMeltingRecipe.class));
    entityMeltingRecipes.add(new DefaultEntityMeltingRecipe(entityMeltingRecipes));
    registration.addRecipes(TConstructJEIConstants.ENTITY_MELTING, entityMeltingRecipes);
    List<AlloyRecipe> alloyRecipes = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.ALLOYING.get(), AlloyRecipe.class);
    registration.addRecipes(TConstructJEIConstants.ALLOY, alloyRecipes);
    // molding recipes used by casting table and basin
    List<MoldingRecipe> moldingRecipes = new java.util.ArrayList<>();
    moldingRecipes.addAll(RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.MOLDING_TABLE.get(), MoldingRecipe.class));
    moldingRecipes.addAll(RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.MOLDING_BASIN.get(), MoldingRecipe.class));
    registration.addRecipes(TConstructJEIConstants.MOLDING, moldingRecipes);

    List<IDisplayModifierRecipe> modifierRecipes = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.TINKER_STATION.get(), IDisplayModifierRecipe.class)
      .stream()
      .sorted(java.util.Comparator
        .comparing(TConstructJEIPlugin::modifierSlotSortKey)
        .thenComparing(TConstructJEIPlugin::isApotheosisPostCapRecipe)
        .thenComparing(recipe -> recipe.getDisplayResult().getId().toString())
        .thenComparing(recipe -> {
          Identifier id = recipe.getRecipeId();
          return id == null ? "" : id.toString();
        }))
      .toList();
    registration.addRecipes(TConstructJEIConstants.MODIFIERS, modifierRecipes);

    List<SeveringRecipe> severingRecipes = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.SEVERING.get(), SeveringRecipe.class);
    registration.addRecipes(TConstructJEIConstants.SEVERING, severingRecipes);

    List<ToolBuildingRecipe> toolBuilding = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.TINKER_STATION.get(), ToolBuildingRecipe.class)
      .stream()
      .sorted(java.util.Comparator.comparingInt(recipe -> StationSlotLayoutLoader.getInstance().get(recipe.getLayoutSlotId()).getSortIndex()))
      .toList();
    registration.addRecipes(TConstructJEIConstants.TOOL_BUILDING, toolBuilding);

    List<IModifierWorktableRecipe> worktableRecipes = RecipeHelper.getJEIRecipes(minecraft.level.registryAccess(), manager, TinkerRecipeTypes.MODIFIER_WORKTABLE.get(), IModifierWorktableRecipe.class);
    registration.addRecipes(TConstructJEIConstants.MODIFIER_WORKTABLE, worktableRecipes);
  }

  /** Sort key for modifier recipes; null slotless recipes stay last as before. */
  private static String modifierSlotSortKey(IDisplayModifierRecipe recipe) {
    SlotType type = recipe.getSlotType();
    return type == null ? "zzzzzzzzzz" : type.getName();
  }

  /** Apotheosis post-cap recipes are kept together in JEI instead of being interleaved with the normal recipe list. */
  private static boolean isApotheosisPostCapRecipe(IDisplayModifierRecipe recipe) {
    Identifier id = recipe.getRecipeId();
    return id != null && id.getNamespace().equals(TConstruct.MOD_ID) && id.getPath().startsWith("tools/modifiers/upgrade/apotheosis/");
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(new ItemStack(TinkerTables.partBuilder), TConstructJEIConstants.PART_BUILDER);
    registration.addRecipeCatalyst(new ItemStack(TinkerTables.tinkerStation), TConstructJEIConstants.MODIFIERS, TConstructJEIConstants.TOOL_BUILDING);
    registration.addRecipeCatalyst(new ItemStack(TinkerTables.tinkersAnvil), TConstructJEIConstants.MODIFIERS, TConstructJEIConstants.TOOL_BUILDING);
    registration.addRecipeCatalyst(new ItemStack(TinkerTables.scorchedAnvil), TConstructJEIConstants.MODIFIERS, TConstructJEIConstants.TOOL_BUILDING);
    registration.addRecipeCatalyst(new ItemStack(TinkerTables.modifierWorktable), TConstructJEIConstants.MODIFIER_WORKTABLE);
    registration.addRecipeCatalyst(new ItemStack(TinkerSmeltery.searedTable), TConstructJEIConstants.CASTING_TABLE, TConstructJEIConstants.MOLDING);
    registration.addRecipeCatalyst(new ItemStack(TinkerSmeltery.searedBasin), TConstructJEIConstants.CASTING_BASIN, TConstructJEIConstants.MOLDING);
    registration.addRecipeCatalyst(new ItemStack(TinkerSmeltery.searedMelter), TConstructJEIConstants.MELTING);
    registration.addRecipeCatalyst(new ItemStack(TinkerSmeltery.smelteryController), TConstructJEIConstants.MELTING, TConstructJEIConstants.ALLOY);
    registration.addRecipeCatalyst(new ItemStack(TinkerSmeltery.scorchedAlloyer), TConstructJEIConstants.ALLOY);
    registration.addRecipeCatalyst(new ItemStack(TinkerSmeltery.foundryController), TConstructJEIConstants.FOUNDRY);
  }}
