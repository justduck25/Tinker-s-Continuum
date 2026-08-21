package slimeknights.tconstruct.library.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;

/** Gson adapter for Minecraft identifiers after the vanilla serializer was removed. */
public enum IdentifierGsonAdapter implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {
  INSTANCE;

  @Override
  public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.toString());
  }

  @Override
  public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return Identifier.parse(json.getAsString());
  }
}