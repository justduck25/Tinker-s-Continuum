package slimeknights.tconstruct.common.data.loot;

import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import slimeknights.mantle.loot.AddEntryLootModifier;
import slimeknights.mantle.loot.ReplaceItemLootModifier;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.ContainsItemModifierLootCondition;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;
import slimeknights.mantle.loot.entry.TagPreferenceLootEntry;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.json.BlockOrEntityCondition;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.common.json.TagFilledLootCondition;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.smeltery.data.SmelteryCompat;
import slimeknights.tconstruct.smeltery.data.SmelteryCompat.CompatType;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.modifiers.ModifierLootModifier;
import slimeknights.tconstruct.tools.modifiers.loot.ChrysophiliteBonusFunction;
import slimeknights.tconstruct.tools.modifiers.loot.ChrysophiliteLootCondition;
import slimeknights.tconstruct.tools.modifiers.loot.HasModifierLootCondition;
import slimeknights.tconstruct.tools.modifiers.loot.ModifierBonusLootFunction;

import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.Mantle.commonResource;

public class GlobalLootModifiersProvider extends GlobalLootModifierProvider {
  public GlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries, TConstruct.MOD_ID);
  }

  @Override
  protected void start() {
    add("wither_bone", ReplaceItemLootModifier.builder(Ingredient.of(Items.BONE), ItemOutput.fromItem(TinkerMaterials.necroticBone))
      .addCondition(LootTableIdCondition.builder(Identifier.parse("entities/wither_skeleton")).build())
      .addCondition(ConfigEnabledCondition.WITHER_BONE_DROP)
      .build());

    // Generic modifier hook for process-loot modifiers such as Autosmelt, Melting, Severing, and loot-to-capacity.
    add("modifier_hook", ModifierLootModifier.builder().addCondition(BlockOrEntityCondition.INSTANCE).build());

    // Tasty drops more bacon from entities in the bacon producer tag.
    add("tasty_bacon", AddEntryLootModifier.builder(LootItem.lootTableItem(TinkerCommons.bacon))
      .addCondition(HasLootContextSetCondition.builder(LootContextParamSets.ENTITY).build())
      .addCondition(LootItemEntityPropertyCondition.hasProperties(EntityTarget.THIS,
        EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(registries.lookupOrThrow(Registries.ENTITY_TYPE), TinkerTags.EntityTypes.BACON_PRODUCER))).build())
      .addCondition(new HasModifierLootCondition(ModifierIds.tasty))
      .addFunction(SetItemCountFunction.setCount(UniformGenerator.between(-2, 1)).build())
      .addFunction(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(0, 1)).build())
      .build());

    // Chrysophilite modifier hook for tagged gold ores.
    add("chrysophilite_modifier", AddEntryLootModifier.builder(LootItem.lootTableItem(Items.GOLD_NUGGET))
      .addCondition(new BlockTagLootCondition(TinkerTags.Blocks.CHRYSOPHILITE_ORES))
      .addCondition(new ContainsItemModifierLootCondition(Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(TinkerTags.Items.CHRYSOPHILITE_ORES))).inverted())
      .addCondition(ChrysophiliteLootCondition.INSTANCE)
      .addFunction(SetItemCountFunction.setCount(UniformGenerator.between(2, 6)).build())
      .addFunction(ChrysophiliteBonusFunction.oreDrops(false).build())
      .addFunction(ApplyExplosionDecay.explosionDecay().build())
      .build());

    addLustrous("iron", false);
    addLustrous("gold", false);
    addLustrous("copper", false);
    addLustrous("cobalt", false);
    addLustrous("netherite_scrap", false);
    for (SmelteryCompat compat : SmelteryCompat.values()) {
      if (compat.getType() == CompatType.ORE) {
        addLustrous(compat.getName(), true);
      }
    }
  }

  /** Adds the lustrous modifier for an ore. */
  private void addLustrous(String name, boolean optional) {
    TagKey<Item> nuggets = TagKey.create(Registries.ITEM, commonResource("nuggets/" + name));
    Identifier ores = commonResource("ores/" + name);
    AddEntryLootModifier.Builder builder = AddEntryLootModifier.builder(TagPreferenceLootEntry.tagPreference(nuggets));
    builder.addCondition(new BlockTagLootCondition(TagKey.create(Registries.BLOCK, ores)))
      .addCondition(new ContainsItemModifierLootCondition(Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(TagKey.create(Registries.ITEM, ores)))).inverted());
    if (optional) {
      builder.addCondition(new TagFilledLootCondition(nuggets));
    }
    add("lustrous/" + name, builder.addCondition(new HasModifierLootCondition(ModifierIds.lustrous))
      .addFunction(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)).build())
      .addFunction(ModifierBonusLootFunction.oreDrops(ModifierIds.lustrous, false).build())
      .addFunction(ApplyExplosionDecay.explosionDecay().build())
      .build());
  }
}
