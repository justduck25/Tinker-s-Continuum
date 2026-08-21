package slimeknights.tconstruct.library.json.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Condition that checks whether a tag has at least one loaded entry. */
public record TagNotEmptyCondition<T>(TagKey<T> tag) implements ICondition {
  public static final Identifier ID = slimeknights.tconstruct.TConstruct.getResource("tag_not_empty");
  public static final MapCodec<TagNotEmptyCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Identifier.CODEC.optionalFieldOf("registry", Registries.ITEM.identifier()).forGetter(condition -> condition.tag.registry().identifier()),
    Identifier.CODEC.fieldOf("tag").forGetter(condition -> condition.tag.location())
  ).apply(instance, TagNotEmptyCondition::new));

  private TagNotEmptyCondition(Identifier registryName, Identifier tagName) {
    this(TagKey.create(ResourceKey.createRegistryKey(registryName), tagName));
  }

  public TagNotEmptyCondition(ResourceKey<? extends Registry<T>> registry, Identifier name) {
    this(TagKey.create(registry, name));
  }

  @Override
  public boolean test(IContext context) {
    return !context.getTag(tag).isEmpty();
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }
}