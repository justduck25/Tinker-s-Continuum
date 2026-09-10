package slimeknights.tconstruct.library.utils;

import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ToolMaterial;
import slimeknights.mantle.client.ResourceColorManager;
import slimeknights.mantle.data.listener.ISafeManagerReloadListener;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Harvest level display names and ordering.
 */
public class HarvestTiers {
  private HarvestTiers() {}

  public static final ToolMaterial ALLTHEMODIUM = new ToolMaterial(
    BlockTags.create(Identifier.fromNamespaceAndPath("allthemodium", "incorrect_for_allthemodium_tool")),
    Short.MAX_VALUE, 15.0F, 12.0F, 85,
    ItemTags.create(Identifier.fromNamespaceAndPath("c", "ingots/allthemodium"))
  );

  public static final ToolMaterial VIBRANIUM = new ToolMaterial(
    BlockTags.create(Identifier.fromNamespaceAndPath("allthemodium", "incorrect_for_vibranium_tool")),
    Short.MAX_VALUE, 20.0F, 25.0F, 100,
    ItemTags.create(Identifier.fromNamespaceAndPath("c", "ingots/vibranium"))
  );

  public static final ToolMaterial UNOBTAINIUM = new ToolMaterial(
    BlockTags.create(Identifier.fromNamespaceAndPath("allthemodium", "incorrect_for_unobtainium_tool")),
    Short.MAX_VALUE, 25.0F, 35.0F, 125,
    ItemTags.create(Identifier.fromNamespaceAndPath("c", "ingots/unobtainium"))
  );

  private static final List<ToolMaterial> ORDER = List.of(
    ToolMaterial.WOOD,
    ToolMaterial.GOLD,
    ToolMaterial.STONE,
    ToolMaterial.COPPER,
    ToolMaterial.IRON,
    ToolMaterial.DIAMOND,
    ToolMaterial.NETHERITE,
    ALLTHEMODIUM,
    VIBRANIUM,
    UNOBTAINIUM
  );

  private static final Map<Identifier,ToolMaterial> BY_NAME = Map.of(
    Identifier.withDefaultNamespace("wood"), ToolMaterial.WOOD,
    Identifier.withDefaultNamespace("gold"), ToolMaterial.GOLD,
    Identifier.withDefaultNamespace("stone"), ToolMaterial.STONE,
    Identifier.withDefaultNamespace("copper"), ToolMaterial.COPPER,
    Identifier.withDefaultNamespace("iron"), ToolMaterial.IRON,
    Identifier.withDefaultNamespace("diamond"), ToolMaterial.DIAMOND,
    Identifier.withDefaultNamespace("netherite"), ToolMaterial.NETHERITE,
    Identifier.fromNamespaceAndPath("allthemodium", "allthemodium"), ALLTHEMODIUM,
    Identifier.fromNamespaceAndPath("allthemodium", "vibranium"), VIBRANIUM,
    Identifier.fromNamespaceAndPath("allthemodium", "unobtainium"), UNOBTAINIUM
  );

  /** Cache of name for each tier */
  private static final Map<Object, Component> harvestLevelNames = Maps.newHashMap();
  /** Listener to clear name cache so we get new colors */
  public static final ISafeManagerReloadListener RELOAD_LISTENER = manager -> harvestLevelNames.clear();

  /** Gets the tier ID. */
  public static Identifier getName(ToolMaterial tier) {
    if (tier == UNOBTAINIUM) {
      return Identifier.fromNamespaceAndPath("allthemodium", "unobtainium");
    }
    if (tier == VIBRANIUM) {
      return Identifier.fromNamespaceAndPath("allthemodium", "vibranium");
    }
    if (tier == ALLTHEMODIUM) {
      return Identifier.fromNamespaceAndPath("allthemodium", "allthemodium");
    }
    if (tier == ToolMaterial.NETHERITE) {
      return Identifier.withDefaultNamespace("netherite");
    }
    if (tier == ToolMaterial.DIAMOND) {
      return Identifier.withDefaultNamespace("diamond");
    }
    if (tier == ToolMaterial.IRON) {
      return Identifier.withDefaultNamespace("iron");
    }
    if (tier == ToolMaterial.COPPER) {
      return Identifier.withDefaultNamespace("copper");
    }
    if (tier == ToolMaterial.STONE) {
      return Identifier.withDefaultNamespace("stone");
    }
    if (tier == ToolMaterial.GOLD) {
      return Identifier.withDefaultNamespace("gold");
    }
    return Identifier.withDefaultNamespace("wood");
  }

  /** Gets the tier for an ID. */
  @Nullable
  public static ToolMaterial byName(Identifier name) {
    return BY_NAME.get(name);
  }

  private static int rank(ToolMaterial material) {
    if (material == UNOBTAINIUM) {
      return 9;
    }
    if (material == VIBRANIUM) {
      return 8;
    }
    if (material == ALLTHEMODIUM) {
      return 7;
    }
    if (material == ToolMaterial.NETHERITE) {
      return 6;
    }
    if (material == ToolMaterial.DIAMOND) {
      return 5;
    }
    if (material == ToolMaterial.IRON) {
      return 4;
    }
    if (material == ToolMaterial.COPPER) {
      return 3;
    }
    if (material == ToolMaterial.STONE) {
      return 2;
    }
    if (material == ToolMaterial.GOLD) {
      return 1;
    }
    return 0;
  }

  /** Makes a translation key for the given name */
  private static MutableComponent makeLevelKey(Object tier) {
    Identifier id = tier instanceof ToolMaterial material ? getName(material) : Identifier.withDefaultNamespace("wood");
    String key = Util.makeTranslationKey("harvest_tier", id);
    TextColor color = ResourceColorManager.getTextColor(key);
    return TConstruct.makeTranslation("stat", key).withStyle(style -> style.withColor(color));
  }

  /**
   * Gets the harvest level name for the given level number
   * @param tier  Tier
   * @return  Level name
   */
  public static Component getName(Object tier) {
    return harvestLevelNames.computeIfAbsent(tier, n ->  makeLevelKey(tier));
  }

  /** Gets the larger of two tiers */
  public static ToolMaterial max(ToolMaterial a, ToolMaterial b) {
    return rank(a) >= rank(b) ? a : b;
  }

  /** Gets the smaller of two tiers */
  public static ToolMaterial min(ToolMaterial a, ToolMaterial b) {
    return rank(a) <= rank(b) ? a : b;
  }


  /** Checks if the tier is correct for drops under the vanilla 1.21 incorrect-for-tool tags. */
  public static boolean isCorrectTierForDrops(Object tier, BlockState state) {
    ToolMaterial material = tier instanceof ToolMaterial toolMaterial ? toolMaterial : ToolMaterial.WOOD;
    if (material == UNOBTAINIUM || material == VIBRANIUM || material == ALLTHEMODIUM) {
      return !state.is(material.incorrectBlocksForDrops());
    }
    if (material == ToolMaterial.NETHERITE) {
      return !state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);
    }
    if (material == ToolMaterial.DIAMOND) {
      return !state.is(BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
    }
    if (material == ToolMaterial.IRON) {
      return !state.is(BlockTags.INCORRECT_FOR_IRON_TOOL);
    }
    if (material == ToolMaterial.COPPER) {
      return !state.is(BlockTags.INCORRECT_FOR_COPPER_TOOL);
    }
    if (material == ToolMaterial.STONE) {
      return !state.is(BlockTags.INCORRECT_FOR_STONE_TOOL);
    }
    if (material == ToolMaterial.GOLD) {
      return !state.is(BlockTags.INCORRECT_FOR_GOLD_TOOL);
    }
    return !state.is(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
  }
  /** Gets the smallest tier in the sorting registry */
  public static ToolMaterial minTier() {
    return ORDER.getFirst();
  }
}
