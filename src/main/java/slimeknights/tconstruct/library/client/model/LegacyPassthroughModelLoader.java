package slimeknights.tconstruct.library.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

/**
 * TODO NeoForge 26.1: replace this bridge with real dynamic TConstruct tool/fluid item models.
 * Keeps legacy custom-loader JSON readable by delegating the vanilla-compatible parts to CuboidModel.
 */
public enum LegacyPassthroughModelLoader implements UnbakedModelLoader<UnbakedModel> {
  INSTANCE;

  @Override
  public UnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
    JsonObject json = jsonObject.deepCopy();
    json.remove("loader");

    // Old Forge fluid container JSON used forge:item/default with named textures. Give it a static generated fallback.
    if ("forge:item/default".equals(json.has("parent") ? json.get("parent").getAsString() : "")) {
      json.addProperty("parent", "minecraft:item/generated");
      if (json.has("textures")) {
        JsonObject textures = json.getAsJsonObject("textures");
        if (textures.has("base") && !textures.has("layer0")) {
          textures.add("layer0", textures.get("base"));
        }
        if (textures.has("texture") && !textures.has("layer0")) {
          textures.add("layer0", textures.get("texture"));
        }
      }
    }
    return context.deserialize(json, CuboidModel.class);
  }
}
