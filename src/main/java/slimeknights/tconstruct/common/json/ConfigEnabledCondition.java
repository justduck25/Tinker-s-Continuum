package slimeknights.tconstruct.common.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class ConfigEnabledCondition implements ICondition, LootItemCondition {
  public static final Identifier ID = TConstruct.getResource("config");
  /* Map of config names to condition cache */
  private static final Map<String, ConfigEnabledCondition> PROPS = new HashMap<>();

  private final String configName;
  private final BooleanSupplier supplier;
  private final boolean fallbackValue;

  private ConfigEnabledCondition(String configName, BooleanSupplier supplier, boolean fallbackValue) {
    this.configName = configName;
    this.supplier = supplier;
    this.fallbackValue = fallbackValue;
  }

  public Identifier getID() {
    return ID;
  }

  @Override
  public boolean test(IContext context) {
    return isEnabled();
  }

  @Override
  public boolean test(LootContext context) {
    return isEnabled();
  }

  private boolean isEnabled() {
    try {
      return supplier.getAsBoolean();
    } catch (IllegalStateException e) {
      return fallbackValue;
    }
  }

  @Override
  public MapCodec<ConfigEnabledCondition> codec() {
    return CODEC;
  }

  public static final MapCodec<ConfigEnabledCondition> CODEC = Codec.STRING
    .fieldOf("prop")
    .xmap(
      name -> {
        ConfigEnabledCondition condition = PROPS.get(name.toLowerCase(Locale.ROOT));
        if (condition == null) {
          throw new IllegalArgumentException("Invalid config property: " + name);
        }
        return condition;
      },
      condition -> condition.configName
    );

  /** Adds a condition. */
  private static ConfigEnabledCondition add(String prop, BooleanSupplier supplier) {
    return add(prop, supplier, true);
  }

  /** Adds a condition with an explicit fallback used before the config is loaded. */
  private static ConfigEnabledCondition add(String prop, BooleanSupplier supplier, boolean fallbackValue) {
    ConfigEnabledCondition conf = new ConfigEnabledCondition(prop, supplier, fallbackValue);
    PROPS.put(prop.toLowerCase(Locale.ROOT), conf);
    return conf;
  }

  @Override
  public String toString() {
    return "config_setting_enabled(\"" + this.configName + "\")";
  }

  /* Properties */
  public static final ConfigEnabledCondition SPAWN_WITH_BOOK = add("spawn_with_book", Config.COMMON.shouldSpawnWithTinkersBook);
  public static final ConfigEnabledCondition GRAVEL_TO_FLINT = add("gravel_to_flint", Config.COMMON.addGravelToFlintRecipe);
  public static final ConfigEnabledCondition CHEAPER_NETHERITE_ALLOY = add("cheaper_netherite_alloy", Config.COMMON.cheaperNetheriteAlloy);
  public static final ConfigEnabledCondition WITHER_BONE_DROP = add("wither_bone_drop", Config.COMMON.witherBoneDrop);
  /** @deprecated use datapacks to remove specific recipes */
  @Deprecated(forRemoval = true)
  public static final ConfigEnabledCondition WITHER_BONE_CONVERSION = add("wither_bone_conversion", () -> false, false);
  public static final ConfigEnabledCondition SLIME_RECIPE_FIX = add("slime_recipe_fix", Config.COMMON.glassRecipeFix);
  public static final ConfigEnabledCondition GLASS_RECIPE_FIX = add("glass_recipe_fix", Config.COMMON.glassRecipeFix);
  public static final ConfigEnabledCondition ALLOW_INGOTLESS_ALLOYS = add("allow_ingotless_alloys", Config.COMMON.allowIngotlessAlloys);
  public static final ConfigEnabledCondition FORCE_INTEGRATION_MATERIALS = add("force_integration_materials", Config.COMMON.forceIntegrationMaterials, false);
  public static final ConfigEnabledCondition SLIMY_LOOT_CHESTS = add("slimy_loot_chests", Config.COMMON.slimyLootChests);
  public static final ConfigEnabledCondition SYNC_KNOCKBACK_RESISTANCE = add("sync_knockback_resistance", Config.COMMON.syncKnockbackResistance);
  public static final ConfigEnabledCondition APOTHEOSIS_POST_CAP_RECIPES = add("apotheosis_post_cap_recipes", Config.COMMON.apotheosisPostCapRecipes);
}
