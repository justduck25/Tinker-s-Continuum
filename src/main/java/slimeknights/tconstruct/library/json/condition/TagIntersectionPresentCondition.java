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

/** Condition that checks whether all listed tags share at least one loaded entry. */
public record TagIntersectionPresentCondition<T>(List<TagKey<T>> names) implements ICondition {
  public static final Identifier ID = TConstruct.getResource("tag_intersection_present");
  private static final Codec<List<Identifier>> TAG_LIST = Codec.withAlternative(Identifier.CODEC.listOf(), Identifier.CODEC, List::of);
  public static final MapCodec<TagIntersectionPresentCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Identifier.CODEC.optionalFieldOf("registry", Registries.ITEM.identifier()).forGetter(condition -> condition.names.getFirst().registry().identifier()),
    TAG_LIST.fieldOf("tags").forGetter(condition -> condition.names.stream().map(TagKey::location).toList())
  ).apply(instance, TagIntersectionPresentCondition::new));

  private TagIntersectionPresentCondition(Identifier registryName, List<Identifier> names) {
    this(toTags(registryName, names));
  }

  public TagIntersectionPresentCondition {
    if (names.isEmpty()) {
      throw new IllegalArgumentException("Cannot create a condition with no names");
    }
  }

  private static <T> List<TagKey<T>> toTags(Identifier registryName, List<Identifier> names) {
    ResourceKey<Registry<T>> registry = ResourceKey.createRegistryKey(registryName);
    return names.stream().map(name -> TagKey.create(registry, name)).toList();
  }

  @SafeVarargs
  public static <T> TagIntersectionPresentCondition<T> ofKeys(TagKey<T>... names) {
    return new TagIntersectionPresentCondition<>(Arrays.asList(names));
  }

  public static <T> TagIntersectionPresentCondition<T> ofNames(ResourceKey<? extends Registry<T>> registry, Identifier... names) {
    return new TagIntersectionPresentCondition<>(Arrays.stream(names).map(name -> TagKey.create(registry, name)).toList());
  }

  @Override
  public boolean test(IContext context) {
    List<Collection<Holder<T>>> tags = names.stream().map(context::getTag).toList();
    if (tags.size() == 1) {
      return !tags.get(0).isEmpty();
    }

    int count = tags.size();
    for (int i = 1; i < count; i++) {
      if (tags.get(i).isEmpty()) {
        return false;
      }
    }

    itemLoop:
    for (Holder<T> entry : tags.get(0)) {
      for (int i = 1; i < count; i++) {
        if (!tags.get(i).contains(entry)) {
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