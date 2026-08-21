package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.util.JsonHelper;

import java.util.function.Function;

/** Bridges TCon's legacy JSON ingredient parsers onto NeoForge 26.1 IngredientType codecs. */
public final class LegacyIngredientType {
  private static final Codec<JsonElement> JSON_CODEC = Codec.PASSTHROUGH.xmap(dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(), json -> new Dynamic<>(JsonOps.INSTANCE, json));

  private LegacyIngredientType() {}

  public static Ingredient parseIngredient(JsonElement json) {
    if (json != null && json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
      String value = json.getAsString();
      if (value.startsWith("#")) {
        return ofTag(TagKey.create(Registries.ITEM, Identifier.parse(value.substring(1))));
      }
      return BuiltInRegistries.ITEM.get(Identifier.parse(value)).map(holder -> Ingredient.of(holder.value())).orElseThrow(() -> new JsonParseException("Unknown item: " + value));
    }
    if (json instanceof JsonObject object && object.has("tag")) {
      return ofTag(TagKey.create(Registries.ITEM, Identifier.parse(object.get("tag").getAsString())));
    }
    return Ingredient.CODEC.parse(JsonHelper.REGISTRY_OPS, json).getOrThrow(JsonParseException::new);
  }

  /** Creates a tag ingredient without forcing tag contents to be bound during registry bootstrap. */
  public static Ingredient ofTag(TagKey<Item> tag) {
    return Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag));
  }

  public static <T extends ICustomIngredient> IngredientType<T> of(Function<JsonObject,T> parser, Function<T,JsonElement> serializer) {
    Codec<T> codec = JSON_CODEC.xmap(json -> {
      if (!json.isJsonObject()) {
        throw new JsonParseException("Expected custom ingredient to be a JSON object");
      }
      return parser.apply(json.getAsJsonObject());
    }, serializer);
    return new IngredientType<>(MapCodec.assumeMapUnsafe(codec));
  }
}