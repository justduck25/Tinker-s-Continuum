package slimeknights.tconstruct.common.data.loot;

import net.minecraft.data.PackOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.loot.AbstractLootTableInjectionProvider;
import slimeknights.mantle.loot.LootTableInjection;
import slimeknights.mantle.loot.function.SetFluidLootFunction;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.FakeRegistryEntry;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.json.loot.AddToolDataFunction;
import slimeknights.tconstruct.library.json.loot.ToolPartLootEntry;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.RandomMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.block.FoliageType;

import java.lang.reflect.Constructor;

/** Add all relevant loot to loot tables */
public class LootTableInjectionProvider extends AbstractLootTableInjectionProvider {
  public LootTableInjectionProvider(PackOutput packOutput) {
    super(packOutput, TConstruct.MOD_ID);
  }

  @SuppressWarnings("removal")
  @Override
  protected void addTables() {
    // slimy foliage injections
    // earth/sky
    inject("slimy_foliage_dungeon", "chests/simple_dungeon", ConfigEnabledCondition.SLIMY_LOOT_CHESTS)
      .addToPool("main", makeSapling(FoliageType.EARTH, 3), makeSapling(FoliageType.SKY, 7))
      .addToPool("pool1", makeSeed(FoliageType.EARTH, 3), makeSeed(FoliageType.SKY, 7));
    // blood
    inject("slimy_foliage_nether_fortress", "chests/nether_bridge", ConfigEnabledCondition.SLIMY_LOOT_CHESTS)
      .addToPool("main", makeSeed(FoliageType.BLOOD, 5));
    inject("slimy_foliage_bastion", "chests/bastion_bridge", ConfigEnabledCondition.SLIMY_LOOT_CHESTS)
      .addToPool("main", makeSapling(FoliageType.BLOOD, 1));
    // ender
    inject("slimy_foliage_end_city", "chests/end_city_treasure", ConfigEnabledCondition.SLIMY_LOOT_CHESTS)
      .addToPool("main", makeSeed(FoliageType.ENDER, 5), makeSapling(FoliageType.ENDER, 3));

    // bartering
    IJsonPredicate<MaterialVariantId> includeInLoot = MaterialPredicate.tag(TinkerTags.Materials.EXCLUDE_FROM_LOOT).inverted();
    RandomMaterial random = RandomMaterial.ancient();
    AddToolDataFunction.Builder ancientToolData2 = AddToolDataFunction.builder().addMaterial(random).addMaterial(random);
    AddToolDataFunction.Builder commonToolData2 = commonToolData2();
    AddToolDataFunction.Builder commonHarvestData = commonHarvestData();
    AddToolDataFunction.Builder commonWeaponData = commonWeaponData();
    AddToolDataFunction.Builder commonShieldData = commonShieldData();
    AddToolDataFunction.Builder commonArmorData = commonArmorData();
    AddToolDataFunction.Builder commonArrowData = commonArrowData();
    injectGameplay("piglin_bartering")
      .addToPool("main", LootItem.lootTableItem(TinkerSmeltery.scorchedLantern).setWeight(20)
                                 .apply(SetFluidLootFunction.builder(fluidStackSafe(TinkerFluids.blazingBlood.get(), FluidValues.LANTERN_CAPACITY)))
                                 .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))
                                 .build())
      .addToPool("main", LootItem.lootTableItem(TinkerTools.battlesign.get())
                                 .setWeight(5)
                                 .apply(ancientToolData2)
                                 .build())
      .addToPool("main", ToolPartLootEntry.entry(TinkerTags.Items.BARTERED_PARTS, RandomMaterial.random().tag(TinkerTags.Materials.BARTERED).allowHidden().build())
                                 .setWeight(8) // same weight as soulspeed boots
                                 .build());

    // spawn chest
    RandomMaterial randomTier1 = RandomMaterial.random().tier(0, 1).material(includeInLoot).build();
    RandomMaterial firstWithStat = RandomMaterial.firstWithStat(); // should be wood
    injectChest("spawn_bonus_chest")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.handAxe.get())
                                 .setWeight(2)
                                 .apply(AddToolDataFunction.builder()
                                                           .addMaterial(randomTier1)
                                                           .addMaterial(firstWithStat)
                                                           .addMaterial(randomTier1))
                                 .build())
      .addToPool("pool1", LootItem.lootTableItem(TinkerTools.pickaxe.get())
                                 .setWeight(2)
                                 .apply(AddToolDataFunction.builder()
                                                           .addMaterial(randomTier1)
                                                           .addMaterial(firstWithStat)
                                                           .addMaterial(randomTier1))
                                 .build());

    // ruined portals give a free flint and brick, because you need one of course
    AddToolDataFunction.Builder buildData = AddToolDataFunction.builder();
    injectChest("ruined_portal").addToPool("main", LootItem.lootTableItem(TinkerTools.flintAndBrick.get())
                                                           .apply(buildData)
                                                           .setWeight(45).build());
    // nether fortress bridge is another place to get flint and brick
    injectChest("nether_bridge").addToPool("main", LootItem.lootTableItem(TinkerTools.flintAndBrick.get())
                                                           .apply(buildData)
                                                           .setWeight(8).build());

    // frypans just show up in some assorted locations
    injectChest("simple_dungeon")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.meltingPan.get())
                                 .setWeight(4)
                                 .apply(commonToolData2)
                                 .build());
    injectChest("igloo_chest")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.meltingPan.get())
                                 .setWeight(2)
                                 .apply(commonToolData2)
                                 .build());
    inject("hero_of_the_armorer", "gameplay/hero_of_the_village/armorer_gift")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.meltingPan.get())
                                 .setWeight(1) // 1 in 5 chance of a melting pan compared to the chainmail
                                 .apply(ancientToolData2)
                                 .build());

    AddToolDataFunction.Builder ancientToolData3 = AddToolDataFunction.builder().addMaterial(random).addMaterial(random).addMaterial(random);
    injectChest("pillager_outpost")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.battlesign.get())
                                 .apply(commonShieldData)
                                 .build());
    injectChest("abandoned_mineshaft")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.pickaxe.get())
                                 .setWeight(3)
                                 .apply(commonHarvestData)
                                 .build());
    injectChest("woodland_mansion")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.battlesign.get())
                                 .setWeight(3)
                                 .apply(commonShieldData)
                                 .build());
    inject("hero_of_the_weaponsmith", "gameplay/hero_of_the_village/weaponsmith_gift")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.sword.get())
                                 .setWeight(1) // makes it a 1 in 4 chance of a Tinkers sword
                                 .apply(ancientToolData3)
                                 .build());
    LootTableInjection.Builder bastion = injectChest("bastion_treasure")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.battlesign.get())
                                 .setWeight(8)
                                 .apply(ancientToolData2)
                                .build());
    injectChest("bastion_other")
      .addToPool("pool1", LootItem.lootTableItem(TinkerTools.battlesign.get())
                                 .setWeight(5) // a bit more common than an iron sword
                                 .apply(ancientToolData2)
                                 .apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.1f, 0.9f)))
                                 .build());
    // diamond armor shows in bastions, add in some plate with similar weight to enchanted version
    RandomMaterial randomHighTier = RandomMaterial.random().allowHidden().tier(3, 4).material(includeInLoot).build();
    for (ArmorType slot : slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial.ARMOR_TYPES) {
      bastion.addToPool("main", LootItem.lootTableItem(TinkerTools.plateArmor.get(slot))
                                        .setWeight(8)
                                        .apply(AddToolDataFunction.builder()
                                                                  .addMaterial(randomHighTier)
                                                                  .addMaterial(randomHighTier))
                                        .build());
    }

    AddToolDataFunction.Builder endCityCleaverData = rareMeleeWeaponData(randomHighTier, 4, 4, 3, 2, true);
    AddToolDataFunction.Builder endCitySledgeHammerData = rareBroadHarvestData(randomHighTier, 4, 4, 3, 2, true);
    AddToolDataFunction.Builder endCityExcavatorData = rareBroadHarvestData(randomHighTier, 4, 4, 3, 2, true);
    AddToolDataFunction.Builder ominousBattlesignData = rareShieldData(randomHighTier, 4, 3);
    AddToolDataFunction.Builder ominousSledgeHammerData = rareBroadHarvestData(randomHighTier, 4, 3, 2, 2, true);
    AddToolDataFunction.Builder ominousExcavatorData = rareBroadHarvestData(randomHighTier, 4, 4, 3, 2, true);
    AddToolDataFunction.Builder ominousCleaverData = rareMeleeWeaponData(randomHighTier, 4, 4, 3, 2, true);

    // swashers are found in the ocean in all sorts of places, maybe there were pirates once
    LootItemConditionalFunction.Builder<?> setFluid = SetFluidLootFunction.builder(fluidStackSafe(Fluids.LAVA, FluidType.BUCKET_VOLUME));
    injectChest("buried_treasure")
      .addToPool("pool3", LootItem.lootTableItem(TinkerTools.swasher.get())
                                  .setWeight(2)
                                  .apply(commonWeaponData)
                                  .apply(setFluid)
                                  .build());
    injectChest("shipwreck_treasure")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.swasher.get())
                                  .setWeight(4)
                                  .apply(commonWeaponData)
                                 .apply(setFluid)
                                  .build());
    inject("fishing_treasure", Identifier.parse("gameplay/fishing/treasure"))
      .addToPool("main", LootItem.lootTableItem(TinkerTools.swasher.get())
                                 .setWeight(1) // all treasure from fishing is the same weight
                                 .apply(commonWeaponData)
                                 .apply(setFluid)
                                 .build());


    // broaden vanilla chest coverage for the NeoForge port, keeping entries themed and still weighted.
    injectChest("ancient_city")
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 10, ancientToolData2))
      .addToPool("main", ancientTool(TinkerTools.plateArmor.get(ArmorType.CHESTPLATE), 6, AddToolDataFunction.builder().addMaterial(randomHighTier).addMaterial(randomHighTier)));
    injectChest("ancient_city_ice_box")
      .addToPool("main", ancientTool(TinkerTools.meltingPan.get(), 6, ancientToolData2));
    injectChest("bastion_bridge")
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 6, ancientToolData2));
    injectChest("bastion_hoglin_stable")
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 8, ancientToolData2));
    injectChest("end_city_treasure")
      .addToPool("main", ancientTool(TinkerTools.cleaver.get(), 6, endCityCleaverData))
      .addToPool("main", ancientTool(TinkerTools.sledgeHammer.get(), 6, endCitySledgeHammerData))
      .addToPool("main", ancientTool(TinkerTools.excavator.get(), 6, endCityExcavatorData));
    injectChest("desert_pyramid")
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 3, commonShieldData));
    injectChest("jungle_temple")
      .addToPool("main", ancientTool(TinkerTools.kama.get(), 3, commonHarvestData));
    injectChest("jungle_temple_dispenser")
      .addToPool("main", ancientTool(TinkerTools.arrow.get(), 5, commonArrowData));
    injectChest("trial_chambers/reward")
      .addToPool("main", ancientTool(TinkerTools.dagger.get(), 3, commonWeaponData));
    injectChest("shipwreck_map")
      .addToPool("main", ancientTool(TinkerTools.swasher.get(), 3, commonWeaponData, setFluid));
    injectChest("shipwreck_supply")
      .addToPool("main", ancientTool(TinkerTools.meltingPan.get(), 3, commonToolData2));
    injectChest("stronghold_corridor")
      .addToPool("main", ancientTool(TinkerTools.sword.get(), 3, commonWeaponData));
    injectChest("stronghold_crossing")
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 3, commonShieldData));
    injectChest("stronghold_library")
      .addToPool("main", ancientTool(TinkerTools.arrow.get(), 5, commonArrowData));
    injectChest("underwater_ruin_big")
      .addToPool("main", ancientTool(TinkerTools.swasher.get(), 3, commonWeaponData, setFluid));
    injectChest("underwater_ruin_small")
      .addToPool("main", ancientTool(TinkerTools.swasher.get(), 2, commonWeaponData, setFluid));

    injectChest("trial_chambers/corridor")
      .addToPool("main", ancientTool(TinkerTools.dagger.get(), 3, commonWeaponData));
    injectChest("trial_chambers/entrance")
      .addToPool("main", ancientTool(TinkerTools.handAxe.get(), 3, commonHarvestData));
    injectChest("trial_chambers/intersection")
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 3, commonShieldData));
    injectChest("trial_chambers/intersection_barrel")
      .addToPool("main", ancientTool(TinkerTools.meltingPan.get(), 3, commonToolData2));
    injectChest("trial_chambers/supply")
      .addToPool("main", ancientTool(TinkerTools.arrow.get(), 5, commonArrowData));
    injectChest("trial_chambers/reward_common")
      .addToPool("main", ancientTool(TinkerTools.dagger.get(), 3, commonWeaponData));
    injectChest("trial_chambers/reward_rare")
      .addToPool("main", ancientTool(TinkerTools.sledgeHammer.get(), 6, ancientToolData3));
    injectChest("trial_chambers/reward_unique")
      .addToPool("main", ancientTool(TinkerTools.cleaver.get(), 6, ancientToolData2));
    injectChest("trial_chambers/reward_ominous_common")
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 6, ominousBattlesignData))
      .addToPool("main", ancientTool(TinkerTools.sledgeHammer.get(), 4, ominousSledgeHammerData));
    injectChest("trial_chambers/reward_ominous")
      .addToPool("main", ancientTool(TinkerTools.sledgeHammer.get(), 6, ominousSledgeHammerData))
      .addToPool("main", ancientTool(TinkerTools.excavator.get(), 4, ominousExcavatorData));
    injectChest("trial_chambers/reward_ominous_rare")
      .addToPool("main", ancientTool(TinkerTools.excavator.get(), 5, ominousExcavatorData))
      .addToPool("main", ancientTool(TinkerTools.sledgeHammer.get(), 5, ominousSledgeHammerData));
    injectChest("trial_chambers/reward_ominous_unique")
      .addToPool("main", ancientTool(TinkerTools.cleaver.get(), 6, ominousCleaverData))
      .addToPool("main", ancientTool(TinkerTools.excavator.get(), 4, ominousExcavatorData))
      .addToPool("main", ancientTool(TinkerTools.sledgeHammer.get(), 4, ominousSledgeHammerData));

    injectChest("village/village_armorer")
      .addToPool("main", ancientTool(TinkerTools.plateArmor.get(ArmorType.CHESTPLATE), 2, commonArmorData));
    injectChest("village/village_butcher")
      .addToPool("main", ancientTool(TinkerTools.meltingPan.get(), 2, commonToolData2));
    injectChest("village/village_cartographer")
      .addToPool("main", ancientTool(TinkerTools.swasher.get(), 2, commonWeaponData, setFluid));
    injectChest("village/village_desert_house")
      .addToPool("main", ancientTool(TinkerTools.flintAndBrick.get(), 8, buildData));
    injectChest("village/village_fisher")
      .addToPool("main", ancientTool(TinkerTools.swasher.get(), 2, commonWeaponData, setFluid));
    injectChest("village/village_fletcher")
      .addToPool("main", ancientTool(TinkerTools.arrow.get(), 5, commonArrowData));
    injectChest("village/village_mason")
      .addToPool("main", ancientTool(TinkerTools.sledgeHammer.get(), 2, commonHarvestData));
    injectChest("village/village_plains_house")
      .addToPool("main", ancientTool(TinkerTools.handAxe.get(), 2, commonHarvestData));
    injectChest("village/village_savanna_house")
      .addToPool("main", ancientTool(TinkerTools.kama.get(), 2, commonHarvestData));
    injectChest("village/village_shepherd")
      .addToPool("main", ancientTool(TinkerTools.scythe.get(), 2, commonHarvestData));
    injectChest("village/village_snowy_house")
      .addToPool("main", ancientTool(TinkerTools.meltingPan.get(), 2, commonToolData2));
    injectChest("village/village_taiga_house")
      .addToPool("main", ancientTool(TinkerTools.handAxe.get(), 2, commonHarvestData));
    injectChest("village/village_tannery")
      .addToPool("main", ancientTool(TinkerTools.dagger.get(), 2, commonWeaponData));
    injectChest("village/village_temple")
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 2, commonShieldData));
    injectChest("village/village_toolsmith")
      .addToPool("main", ancientTool(TinkerTools.pickaxe.get(), 3, commonHarvestData));
    injectChest("village/village_weaponsmith")
      .addToPool("main", ancientTool(TinkerTools.sword.get(), 3, commonWeaponData));

    addWorldLootIntegrations(randomHighTier, ancientToolData2, ancientToolData3, setFluid);

    // fletchers give you some arrows
    inject("hero_of_the_fletcher", "gameplay/hero_of_the_village/fletcher_gift")
      .addToPool("main", LootItem.lootTableItem(TinkerTools.arrow.get())
        .setWeight(10) // bit more rare than tipped arrows
        .apply(commonArrowData)
        .build());

    // twilight forest - minotaur axe
    String tf = "twilightforest";
    ICondition tfLoaded = new ModLoadedCondition(tf);
    LootPoolEntryContainer minotaurAxe = LootItem.lootTableItem(FakeRegistryEntry.item(TinkerTools.minotaurAxe.getId()))
      .setWeight(1) // TF tends to use 1 for its weight
      .apply(ancientToolData3)
      .build();
    inject("labyrinth_room", Identifier.fromNamespaceAndPath(tf, "chests/labyrinth_room"), tfLoaded)
      .addToPool("pool1", minotaurAxe)
      .addToPool("pool2", minotaurAxe);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Loot Table Injections";
  }

  /** Low-tier two-material profile for ordinary overworld loot. */
  private static AddToolDataFunction.Builder commonToolData2() {
    return AddToolDataFunction.builder()
      .addMaterial(MaterialIds.iron)
      .addMaterial(MaterialIds.wood);
  }

  /** Low-tier harvest profile for ordinary overworld loot. */
  private static AddToolDataFunction.Builder commonHarvestData() {
    return AddToolDataFunction.builder()
      .addMaterial(MaterialIds.rock)
      .addMaterial(MaterialIds.wood)
      .addMaterial(MaterialIds.iron);
  }

  /** Low-tier weapon profile for ordinary overworld loot. */
  private static AddToolDataFunction.Builder commonWeaponData() {
    return AddToolDataFunction.builder()
      .addMaterial(MaterialIds.flint)
      .addMaterial(MaterialIds.wood)
      .addMaterial(MaterialIds.iron);
  }

  /** Low-tier shield profile for ordinary overworld loot. */
  private static AddToolDataFunction.Builder commonShieldData() {
    return AddToolDataFunction.builder()
      .addMaterial(MaterialIds.wood)
      .addMaterial(MaterialIds.iron)
      .addMaterial(MaterialIds.wood);
  }

  /** Low-tier armor profile for ordinary overworld loot. */
  private static AddToolDataFunction.Builder commonArmorData() {
    return AddToolDataFunction.builder()
      .addMaterial(MaterialIds.iron)
      .addMaterial(MaterialIds.leather);
  }

  /** Low-tier ammo profile for ordinary overworld loot. */
  private static AddToolDataFunction.Builder commonArrowData() {
    return AddToolDataFunction.builder()
      .addMaterial(MaterialIds.flint)
      .addMaterial(MaterialIds.wood)
      .addMaterial(MaterialIds.feather);
  }


  /** Builds rare loot tool data with high-tier materials. */
  private static AddToolDataFunction.Builder rareToolData(RandomMaterial material) {
    return AddToolDataFunction.builder()
      .addMaterial(material)
      .addMaterial(material)
      .addMaterial(material)
      .addMaterial(material);
  }

  /** Common high-tier setup safe for all rare loot tools. The random high-tier materials are already the main reward. */
  private static AddToolDataFunction.Builder rareBaseData(RandomMaterial material, int upgradeSlots, int abilitySlots) {
    return rareToolData(material)
      .addUpgradeSlots(upgradeSlots)
      .addAbilitySlots(abilitySlots);
  }

  /** Profile for melee weapons such as swords and cleavers. Uses luck so the tool displays Looting by role. */
  private static AddToolDataFunction.Builder rareMeleeWeaponData(RandomMaterial material, int upgradeSlots, int abilitySlots, int sharpness, int luck, boolean sweeping) {
    AddToolDataFunction.Builder builder = rareBaseData(material, upgradeSlots, abilitySlots)
      .randomModifierCount(2, 3)
      .addRandomModifier(ModifierIds.sharpness, sharpness)
      .addRandomModifier(ModifierIds.luck, luck)
      .addRandomModifier(ModifierIds.knockback, 2)
      .addRandomModifier(ModifierIds.fiery, 2);
    if (sweeping) {
      builder.addRandomModifier(ModifierIds.sweeping, 1);
    }
    return builder;
  }

  /** Profile for broad harvest tools such as sledge hammers and excavators. Uses luck so the tool displays Fortune by role. */
  private static AddToolDataFunction.Builder rareBroadHarvestData(RandomMaterial material, int upgradeSlots, int abilitySlots, int haste, int luck, boolean expanded) {
    AddToolDataFunction.Builder builder = rareBaseData(material, upgradeSlots, abilitySlots)
      .randomModifierCount(2, 3)
      .addRandomModifier(ModifierIds.haste, haste)
      .addRandomModifier(ModifierIds.luck, luck)
      .addRandomModifier(ModifierIds.magnetic, 2)
      .addRandomModifier(ModifierIds.reinforced, 2);
    if (expanded) {
      builder.addRandomModifier(ModifierIds.expanded, 1);
    }
    return builder;
  }

  /** Profile for shield-style loot such as battlesigns. */
  private static AddToolDataFunction.Builder rareShieldData(RandomMaterial material, int upgradeSlots, int abilitySlots) {
    return rareBaseData(material, upgradeSlots, abilitySlots)
      .randomModifierCount(2, 3)
      .addRandomModifier(ModifierIds.blocking, 1)
      .addRandomModifier(ModifierIds.reinforced, 2)
      .addRandomModifier(ModifierIds.blockade, 2)
      .addRandomModifier(ModifierIds.reflecting, 1)
      .addRandomModifier(ModifierIds.boundless, 1);
  }

  /** Makes an ancient tool loot entry. */
  private static LootPoolEntryContainer ancientTool(ItemLike item, int weight, AddToolDataFunction.Builder data) {
    return LootItem.lootTableItem(item).setWeight(weight).apply(data).build();
  }

  /** Makes an ancient tool loot entry with fluid. */
  private static LootPoolEntryContainer ancientTool(ItemLike item, int weight, AddToolDataFunction.Builder data, LootItemConditionalFunction.Builder<?> fluid) {
    return LootItem.lootTableItem(item).setWeight(weight).apply(data).apply(fluid).build();
  }

  /** Adds low-rate Tinkers' gear to common structure mods, keeping each entry themed to the source table. */
  private void addWorldLootIntegrations(RandomMaterial highTierMaterial, AddToolDataFunction.Builder ancientToolData2, AddToolDataFunction.Builder ancientToolData3, LootItemConditionalFunction.Builder<?> fluid) {
    AddToolDataFunction.Builder rareWeapon = rareMeleeWeaponData(highTierMaterial, 3, 2, 3, 2, true);
    AddToolDataFunction.Builder rareHarvest = rareBroadHarvestData(highTierMaterial, 3, 2, 3, 2, true);
    AddToolDataFunction.Builder rareShield = rareShieldData(highTierMaterial, 3, 2);
    AddToolDataFunction.Builder rareArmor = AddToolDataFunction.builder()
      .addMaterial(highTierMaterial)
      .addMaterial(highTierMaterial)
      .addUpgradeSlots(3)
      .addAbilitySlots(2)
      .randomModifierCount(2, 3)
      .addRandomModifier(ModifierIds.protection, 3)
      .addRandomModifier(ModifierIds.revitalizing, 2)
      .addRandomModifier(ModifierIds.knockbackResistance, 2)
      .addRandomModifier(ModifierIds.fireProtection, 2);

    ICondition terralith = new ModLoadedCondition("terralith");
    inject("compat/terralith/mage_treasure", Identifier.fromNamespaceAndPath("terralith", "mage/treasure"), terralith)
      .addToPool("main", ancientTool(TinkerTools.plateArmor.get(ArmorType.CHESTPLATE), 2, rareArmor))
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 2, rareShield));
    inject("compat/terralith/spire_treasure", Identifier.fromNamespaceAndPath("terralith", "spire/treasure"), terralith)
      .addToPool("main", ancientTool(TinkerTools.cleaver.get(), 2, rareWeapon))
      .addToPool("main", ancientTool(TinkerTools.sledgeHammer.get(), 2, rareHarvest));
    inject("compat/terralith/underground_chest", Identifier.fromNamespaceAndPath("terralith", "underground/chest"), terralith)
      .addToPool("main", ancientTool(TinkerTools.pickaxe.get(), 3, ancientToolData3));
    inject("compat/terralith/fortified_village_treasure", Identifier.fromNamespaceAndPath("terralith", "village/fortified/treasure"), terralith)
      .addToPool("main", ancientTool(TinkerTools.sword.get(), 3, ancientToolData2))
      .addToPool("main", ancientTool(TinkerTools.plateArmor.get(ArmorType.CHESTPLATE), 2, rareArmor));
    inject("compat/terralith/desert_village_treasure", Identifier.fromNamespaceAndPath("terralith", "village/desert/treasure"), terralith)
      .addToPool("main", ancientTool(TinkerTools.handAxe.get(), 3, ancientToolData2));
    inject("compat/terralith/witch_hut", Identifier.fromNamespaceAndPath("terralith", "witch_hut"), terralith)
      .addToPool("main", ancientTool(TinkerTools.meltingPan.get(), 2, ancientToolData2));

    ICondition dungeonsAndTaverns = new ModLoadedCondition("mr_dungeons_andtaverns");
    inject("compat/dungeons_and_taverns/badland_miner_forge", Identifier.fromNamespaceAndPath("nova_structures", "chests/badland_miner_outpost_forge"), dungeonsAndTaverns)
      .addToPool("main", ancientTool(TinkerTools.pickaxe.get(), 3, ancientToolData3));
    inject("compat/dungeons_and_taverns/catacomb_generic", Identifier.fromNamespaceAndPath("nova_structures", "chests/catacomb/catacomb_generic"), dungeonsAndTaverns)
      .addToPool("main", ancientTool(TinkerTools.dagger.get(), 3, ancientToolData2));
    inject("compat/dungeons_and_taverns/creeping_crypt_vault", Identifier.fromNamespaceAndPath("nova_structures", "chests/creeping_crypt/vault_creeping"), dungeonsAndTaverns)
      .addToPool("main", ancientTool(TinkerTools.sword.get(), 2, rareWeapon));
    inject("compat/dungeons_and_taverns/desert_ruin_temple", Identifier.fromNamespaceAndPath("nova_structures", "chests/desert_ruins/desert_ruin_main_temple"), dungeonsAndTaverns)
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 2, rareShield));
    inject("compat/dungeons_and_taverns/end_castle_greater_loot", Identifier.fromNamespaceAndPath("nova_structures", "chests/end_castle/greater_loot"), dungeonsAndTaverns)
      .addToPool("main", ancientTool(TinkerTools.cleaver.get(), 2, rareWeapon))
      .addToPool("main", ancientTool(TinkerTools.excavator.get(), 2, rareHarvest));
    inject("compat/dungeons_and_taverns/illager_barracks_generic", Identifier.fromNamespaceAndPath("nova_structures", "chests/illager_barracks/generic"), dungeonsAndTaverns)
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 3, ancientToolData2));
    inject("compat/dungeons_and_taverns/illager_mansion_smithing", Identifier.withDefaultNamespace("chests/illager_mansion/smithing_room"), dungeonsAndTaverns)
      .addToPool("main", ancientTool(TinkerTools.sword.get(), 3, ancientToolData2));
    inject("compat/dungeons_and_taverns/nether_fortress_inside", Identifier.withDefaultNamespace("chests/nether_fortress/fort_inside"), dungeonsAndTaverns)
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 2, ancientToolData2));

    ICondition betterDesertTemples = new ModLoadedCondition("betterdeserttemples");
    inject("compat/yung/desert_tomb_pharaoh", Identifier.fromNamespaceAndPath("betterdeserttemples", "chests/tomb_pharaoh"), betterDesertTemples)
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 2, rareShield));
    inject("compat/yung/desert_lab", Identifier.fromNamespaceAndPath("betterdeserttemples", "chests/lab"), betterDesertTemples)
      .addToPool("main", ancientTool(TinkerTools.meltingPan.get(), 2, ancientToolData2));

    ICondition betterDungeons = new ModLoadedCondition("betterdungeons");
    inject("compat/yung/skeleton_dungeon_middle", Identifier.fromNamespaceAndPath("betterdungeons", "skeleton_dungeon/chests/middle"), betterDungeons)
      .addToPool("main", ancientTool(TinkerTools.sword.get(), 3, ancientToolData2));
    inject("compat/yung/zombie_dungeon_special", Identifier.fromNamespaceAndPath("betterdungeons", "zombie_dungeon/chests/special"), betterDungeons)
      .addToPool("main", ancientTool(TinkerTools.cleaver.get(), 2, rareWeapon));
    inject("compat/yung/small_nether_dungeon_common", Identifier.fromNamespaceAndPath("betterdungeons", "small_nether_dungeon/chests/common"), betterDungeons)
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 2, ancientToolData2));

    inject("compat/yung/jungle_temple_treasure", Identifier.fromNamespaceAndPath("betterjungletemples", "chests/treasure"), new ModLoadedCondition("betterjungletemples"))
      .addToPool("main", ancientTool(TinkerTools.kama.get(), 2, ancientToolData2));
    inject("compat/yung/nether_fortress_keep", Identifier.fromNamespaceAndPath("betterfortresses", "chests/keep"), new ModLoadedCondition("betterfortresses"))
      .addToPool("main", ancientTool(TinkerTools.battlesign.get(), 2, rareShield));
    inject("compat/yung/ocean_monument_upper_side_chamber", Identifier.fromNamespaceAndPath("betteroceanmonuments", "chests/upper_side_chamber"), new ModLoadedCondition("betteroceanmonuments"))
      .addToPool("main", ancientTool(TinkerTools.swasher.get(), 2, ancientToolData3, fluid));

    ICondition betterStrongholds = new ModLoadedCondition("betterstrongholds");
    inject("compat/yung/stronghold_armoury", Identifier.fromNamespaceAndPath("betterstrongholds", "chests/armoury"), betterStrongholds)
      .addToPool("main", ancientTool(TinkerTools.plateArmor.get(ArmorType.CHESTPLATE), 2, rareArmor));
    inject("compat/yung/stronghold_treasure", Identifier.fromNamespaceAndPath("betterstrongholds", "chests/treasure"), betterStrongholds)
      .addToPool("main", ancientTool(TinkerTools.sword.get(), 2, rareWeapon))
      .addToPool("main", ancientTool(TinkerTools.sledgeHammer.get(), 2, rareHarvest));

    inject("compat/yung/witch_hut", Identifier.fromNamespaceAndPath("betterwitchhuts", "chests/hut_0"), new ModLoadedCondition("betterwitchhuts"))
      .addToPool("main", ancientTool(TinkerTools.meltingPan.get(), 2, ancientToolData2));
  }

  /** Makes a seed injection loot entry */
  private static LootPoolEntryContainer makeSeed(FoliageType type, int weight) {
    return LootItem.lootTableItem(TinkerWorld.slimeGrassSeeds.get(type)).setWeight(weight)
                   .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))).build();
  }

  /** Makes a sapling injection loot entry */
  private static LootPoolEntryContainer makeSapling(FoliageType type, int weight) {
    return LootItem.lootTableItem(TinkerWorld.slimeSapling.get(type)).setWeight(weight).build();
  }

  /** Creates a fluid stack during datagen before custom fluid components are bound. */
  private static FluidStack fluidStackSafe(Fluid fluid, int amount) {
    Holder.Reference<Fluid> holder = fluid.builtInRegistryHolder();
    if (holder.areComponentsBound()) {
      return new FluidStack(holder, amount);
    }
    try {
      Constructor<FluidStack> constructor = FluidStack.class.getDeclaredConstructor(Holder.class, int.class, PatchedDataComponentMap.class);
      constructor.setAccessible(true);
      return constructor.newInstance(holder, amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to create datagen fluid stack for " + fluid, e);
    }
  }
}
