package slimeknights.tconstruct.library.data.recipe;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.registration.object.FluidObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Helper to generate tags for cost variants of items, typically used for melting */
@SuppressWarnings("removal")
@CanIgnoreReturnValue
public class CostTagAppender {
  private final String metal;
  private final Identifier prefix;
  private final String suffix;
  private final Function<Identifier, TagAppender<ResourceKey<Item>, Item>> tag;
  private final Map<Integer, TagAppender<ResourceKey<Item>, Item>> tags = new HashMap<>();

  public CostTagAppender(String metal, Identifier prefix, String suffix, Function<Identifier, TagAppender<ResourceKey<Item>, Item>> tag) {
    this.metal = metal;
    this.prefix = prefix;
    this.suffix = suffix;
    this.tag = tag;
  }

  /** Creates a builder for a molten gear */
  public static CostTagAppender moltenToolMelting(FluidObject<?> fluid, Function<Identifier, TagAppender<ResourceKey<Item>, Item>> tag) {
    Identifier id = fluid.getId();
    String metal = id.getPath().substring("molten_".length());
    return moltenToolMelting(id.getNamespace(), metal, tag);
  }

  /** Creates a builder for a molten gear */
  public static CostTagAppender moltenToolMelting(String domain, String metal, Function<Identifier, TagAppender<ResourceKey<Item>, Item>> tag) {
    return new CostTagAppender(metal, Identifier.fromNamespaceAndPath(domain, "melting/" + metal + "/tools_costing_"), "", tag);
  }

  /** Creates a tag for the given cost */
  @CheckReturnValue
  public TagAppender<ResourceKey<Item>, Item> tag(int cost) {
    TagAppender<ResourceKey<Item>, Item> appender = tags.get(cost);
    if (appender == null) {
      appender = this.tag.apply(prefix.withSuffix(cost + suffix));
      this.tags.put(cost, appender);
    }
    return appender;
  }

  /** Adds the passed items to the tag. */
  public CostTagAppender add(int cost, Item... items) {
    TagAppender<ResourceKey<Item>, Item> appender = tag(cost);
    for (Item item : items) {
      appender.add(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow());
    }
    return this;
  }

  /** Adds the passed items to the tag. */
  public CostTagAppender add(int cost, boolean optional, Identifier prefix, String... suffixes) {
    TagAppender<ResourceKey<Item>, Item> tag = tag(cost);
    if (optional) {
      if (suffixes.length == 0) {
        tag.addOptional(ResourceKey.create(Registries.ITEM, prefix));
      } else for (String path : suffixes) {
        tag.addOptional(ResourceKey.create(Registries.ITEM, prefix.withSuffix('_' + path)));
      }
    } else {
      if (suffixes.length == 0) {
        tag.add(ResourceKey.create(Registries.ITEM, prefix));
      } else for (String path : suffixes) {
        tag.add(ResourceKey.create(Registries.ITEM, prefix.withSuffix('_' + path)));
      }
    }
    return this;
  }

  /** Adds the passed items by ID with the metal as the prefix */
  public CostTagAppender optionalMetal(int cost, String domain, String... suffixes) {
    return add(cost, true, Identifier.fromNamespaceAndPath(domain, metal), suffixes);
  }

  /** Adds the given optional tag to the builder with the given prefix using our metal */
  public CostTagAppender metalTag(int cost, String prefix, String... names) {
    TagAppender<ResourceKey<Item>, Item> tag = tag(cost);
    for (String name : names) {
      tag.addOptionalTag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Mantle.COMMON, prefix + name + '/' + metal)));
    }
    return this;
  }

  /** Adds the given optional tool tag to the builder with the given prefix using our metal */
  public CostTagAppender toolTag(int cost, String... names) {
    return metalTag(cost, "tools/", names);
  }

  /** Adds the given optional armor tag to the builder with the given prefix using our metal */
  public CostTagAppender armorTag(int cost, String... names) {
    return metalTag(cost, "armors/", names);
  }


  /* Vanilla tools */

  /** Adds common gear items from tags */
  @Internal
  public CostTagAppender minecraft(String metal) {
    Identifier prefix = Identifier.parse(metal);
    add(1, false, prefix, "shovel");
    add(2, false, prefix, "sword", "hoe");
    add(3, false, prefix, "pickaxe", "axe");
    add(7, false, prefix, "leggings");
    toolTag(7, "paxels");
    optionalMetal(1, "tools_complement", "knife");
    optionalMetal(3, "tools_complement", "sickle");
    return this;
  }

  @Internal
  public CostTagAppender minecraft() {
    return minecraft(metal);
  }


  /* Modded tools */

  public CostTagAppender toolTags() {
    toolTag(2, "swords", "hoes");
    toolTag(3, "pickaxes", "axes");
    return this;
  }

  @Internal
  public CostTagAppender toolsComplement() {
    toolTag(1, "shovels");
    optionalMetal(1, "tools_complement", "knife");
    optionalMetal(3, "tools_complement", "sickle");
    return this;
  }

  public CostTagAppender leggingsPaxel() {
    return armorTag(7, "leggings").toolTag(7, "paxels");
  }

  public CostTagAppender fdKnife() {
    return optionalMetal(1, "farmersdelight", "knife");
  }

  @Internal
  public CostTagAppender crowbar() {
    return optionalMetal(3, "railcraft", "crowbar");
  }

  @Internal
  public CostTagAppender excavatorSpikeMaul() {
    return optionalMetal(11, "tools_complement", "excavator").optionalMetal(11, "railcraft", "spike_maul");
  }
}
