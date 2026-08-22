package slimeknights.tconstruct.plugin.jsonthings;

import dev.gigaherz.jsonthings.things.serializers.FlexItemType;
import dev.gigaherz.jsonthings.things.serializers.IItemSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.common.util.Lazy;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableArrowItem;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.item.ModifiableShurikenItem;
import slimeknights.tconstruct.library.tools.item.armor.DummyArmorMaterial;
import slimeknights.tconstruct.library.tools.item.armor.ModifiableArmorItem;
import slimeknights.tconstruct.library.tools.item.armor.MultilayerArmorItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.plugin.jsonthings.item.FlexPartCastItem;
import slimeknights.tconstruct.plugin.jsonthings.item.IMaterialItemFactory;
import slimeknights.tconstruct.plugin.jsonthings.item.IToolItemFactory;
import slimeknights.tconstruct.tools.item.ModifiableSwordItem;
import slimeknights.tconstruct.tools.item.RepairKitItem;

/** Collection of custom item types added by Tinkers. */
@SuppressWarnings("unused")
public class FlexItemTypes {
  /** Initializes the item types. */
  public static void init() {
    /* Register a tool part to create new tools. */
    register("tool_part", data -> {
      MaterialStatsId statType = new MaterialStatsId(JsonHelper.getResourceLocation(data, "stat_type"));
      return (IMaterialItemFactory<ToolPartItem>)(props, builder) -> new ToolPartItem(props, statType);
    });

    /* Register an item that can be used to repair tools. */
    register("repair_kit", data -> {
      float repairAmount = GsonHelper.getAsFloat(data, "repair_amount");
      return (IMaterialItemFactory<RepairKitItem>)(props, builder) -> new RepairKitItem(props, repairAmount);
    });

    /* Register a modifiable tool instance for melee/harvest tools. */
    register("tool", data -> {
      boolean breakBlocksInCreative = GsonHelper.getAsBoolean(data, "break_blocks_in_creative", true);
      int stackSize = GsonHelper.getAsInt(data, "max_stack_size", 1);
      return (IToolItemFactory<ModifiableItem>)(props, builder) -> {
        props.durability(-1).stacksTo(stackSize);
        ToolDefinition definition = ToolDefinition.create(builder.getRegistryName());
        return breakBlocksInCreative ? new ModifiableItem(props, definition, stackSize) : new ModifiableSwordItem(props, definition, stackSize);
      };
    });

    /* Register a modifiable tool instance for bow-like items. */
    register("bow", data -> {
      boolean storeDrawingItem = GsonHelper.getAsBoolean(data, "store_drawing_item", false);
      return (IToolItemFactory<ModifiableBowItem>)(props, builder) -> new ModifiableBowItem(props, ToolDefinition.create(builder.getRegistryName()), storeDrawingItem);
    });

    /* Register a modifiable tool instance for crossbow-like items. */
    register("crossbow", data -> {
      boolean allowFireworks = GsonHelper.getAsBoolean(data, "allow_fireworks");
      boolean storeDrawingItem = GsonHelper.getAsBoolean(data, "store_drawing_item", false);
      return (IToolItemFactory<ModifiableCrossbowItem>)(props, builder) -> new ModifiableCrossbowItem(props, ToolDefinition.create(builder.getRegistryName()), allowFireworks ? ProjectileWeaponItem.ARROW_OR_FIREWORK : ProjectileWeaponItem.ARROW_ONLY, storeDrawingItem);
    });

    /* Register a modifiable arrow item. */
    register("arrow", data -> (IToolItemFactory<ModifiableArrowItem>)(props, builder) -> new ModifiableArrowItem(props, ToolDefinition.create(builder.getRegistryName())));

    /* Register a modifiable shuriken item. */
    register("shuriken", data -> (IToolItemFactory<ModifiableShurikenItem>)(props, builder) -> new ModifiableShurikenItem(props, ToolDefinition.create(builder.getRegistryName())));

    /* Register a cast item that shows a part cost in the tooltip. */
    register("part_cast", data -> {
      Identifier partId = JsonHelper.getResourceLocation(data, "part");
      return (props, builder) -> new FlexPartCastItem(props, builder, Lazy.of(() -> Loadables.ITEM.fromKey(partId, "part")));
    });

    /* Simple armor type with a flat texture. */
    register("basic_armor", data -> {
      Identifier name = JsonHelper.getResourceLocation(data, "texture_name");
      SoundEvent sound = Loadables.SOUND_EVENT.getOrDefault(data, "equip_sound", SoundEvents.ARMOR_EQUIP_GENERIC.value());
      ArmorType slot = TinkerLoadables.ARMOR_SLOT.getIfPresent(data, "slot");
      return (IToolItemFactory<ModifiableArmorItem>)(props, builder) -> new ModifiableArmorItem(new DummyArmorMaterial(name, sound), slot, props, ToolDefinition.create(builder.getRegistryName()));
    });

    /* Layered armor type, used for golden, dyeable, and similar armor. */
    register("multilayer_armor", data -> {
      Identifier name = JsonHelper.getResourceLocation(data, "model_name");
      SoundEvent sound = Loadables.SOUND_EVENT.getOrDefault(data, "equip_sound", SoundEvents.ARMOR_EQUIP_GENERIC.value());
      ArmorType slot = TinkerLoadables.ARMOR_SLOT.getIfPresent(data, "slot");
      return (IToolItemFactory<MultilayerArmorItem>)(props, builder) -> new MultilayerArmorItem(ModifiableArmorMaterial.create(name, sound, slot), slot, props, ToolDefinition.create(builder.getRegistryName()), name);
    });
  }

  /** Local helper to register our stuff. */
  private static <T extends Item> void register(String name, IItemSerializer<T> factory) {
    FlexItemType.register(TConstruct.resourceString(name), factory);
  }
}
