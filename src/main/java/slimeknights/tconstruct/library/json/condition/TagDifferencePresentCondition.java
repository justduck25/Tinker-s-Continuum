package slimeknights.tconstruct.library.json.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.tconstruct.TConstruct;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Condition that checks whether a base tag has at least one loaded entry outside the subtracted tags. */
public record TagDifferencePresentCondition<T>(TagKey<T> base, List<TagKey<T>> subtracted) implements ICondition {
  public static final Identifier ID = TConstruct.getResource("tag_difference_present");
  private static final Codec<List<Identifier>> TAG_LIST = Codec.withAlternative(Identifier.CODEC.listOf(), Identifier.CODEC, List::of);
  public static final MapCodec<TagDifferencePresentCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Identifier.CODEC.optionalFieldOf("registry", Registries.ITEM.identifier()).forGetter(condition -> condition.base.registry().identifier()),
    Identifier.CODEC.fieldOf("base").forGetter(condition -> condition.base.location()),
    TAG_LIST.fieldOf("subtracted").forGetter(condition -> condition.subtracted.stream().map(TagKey::location).toList())
  ).apply(instance, TagDifferencePresentCondition::new));

  private TagDifferencePresentCondition(Identifier registryName, Identifier base, List<Identifier> subtracted) {
    this(TagKey.create(ResourceKey.createRegistryKey(registryName), base), toTags(registryName, subtracted));
  }

  public TagDifferencePresentCondition {
    if (subtracted.isEmpty()) {
      throw new IllegalArgumentException("Cannot create a condition with no subtracted tags");
    }
  }

  private static <T> List<TagKey<T>> toTags(Identifier registryName, List<Identifier> names) {
    ResourceKey<Registry<T>> registry = ResourceKey.createRegistryKey(registryName);
    return names.stream().map(name -> TagKey.create(registry, name)).toList();
  }

  @SafeVarargs
  public static <T> TagDifferencePresentCondition<T> ofKeys(TagKey<T> base, TagKey<T>... subtracted) {
    return new TagDifferencePresentCondition<>(base, Arrays.asList(subtracted));
  }

  public static <T> TagDifferencePresentCondition<T> ofNames(ResourceKey<? extends Registry<T>> registry, Identifier base, Identifier... subtracted) {
    TagKey<T> baseKey = TagKey.create(registry, base);
    return new TagDifferencePresentCondition<>(baseKey, Arrays.stream(subtracted).map(name -> TagKey.create(registry, name)).toList());
  }

  @Override
  public boolean test(IContext context) {
    Collection<Holder<T>> base = context.getTag(this.base);
    if (base.isEmpty()) {
      return false;
    }

    itemLoop:
    for (Holder<T> entry : base) {
      for (TagKey<T> tag : subtracted) {
        if (context.getTag(tag).contains(entry)) {
          continue itemLoop;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }
}