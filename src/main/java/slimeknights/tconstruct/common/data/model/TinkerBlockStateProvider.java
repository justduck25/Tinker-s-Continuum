package slimeknights.tconstruct.common.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.registration.GeodeItemObject;
import slimeknights.tconstruct.common.registration.GeodeItemObject.BudSize;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.shared.block.ClearStainedGlassBlock.GlassColor;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.world.TinkerWorld;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Generates client blockstates and models that replaced the old Forge model generator pass. */
public class TinkerBlockStateProvider implements DataProvider {
  private final PackOutput.PathProvider blockstates;
  private final PackOutput.PathProvider blockModels;
  private final PackOutput.PathProvider itemModels;
  private final PackOutput.PathProvider itemDefinitions;
  public TinkerBlockStateProvider(PackOutput output) {
    this.blockstates = output.createPathProvider(Target.RESOURCE_PACK, "blockstates");
    this.blockModels = output.createPathProvider(Target.RESOURCE_PACK, "models/block");
    this.itemModels = output.createPathProvider(Target.RESOURCE_PACK, "models/item");
    this.itemDefinitions = output.createPathProvider(Target.RESOURCE_PACK, "items");
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    List<CompletableFuture<?>> tasks = new ArrayList<>();
    fenceBuilding(tasks, cache, TinkerMaterials.blazewood, "wood/blazewood", "block/wood/blazewood");
    fenceBuilding(tasks, cache, TinkerMaterials.nahuatl, "wood/nahuatl", "block/wood/nahuatl");
    wood(tasks, cache, TinkerWorld.greenheart, false);
    wood(tasks, cache, TinkerWorld.skyroot, true);
    wood(tasks, cache, TinkerWorld.bloodshroom, true);
    wood(tasks, cache, TinkerWorld.enderbark, false);
    roots(tasks, cache, "enderbark_roots", "wood/enderbark/roots", "block/wood/enderbark/roots", "block/wood/enderbark/roots_top");
    for (SlimeType type : SlimeType.values()) {
      String name = type.getSerializedName();
      roots(tasks, cache, name + "_enderbark_roots", "wood/enderbark/roots/" + name, "block/wood/enderbark/roots/" + name, "block/wood/enderbark/roots/" + name + "_top");
    }
    geode(tasks, cache, TinkerWorld.earthGeode, "earth");
    geode(tasks, cache, TinkerWorld.skyGeode, "sky");
    geode(tasks, cache, TinkerWorld.ichorGeode, "ichor");
    geode(tasks, cache, TinkerWorld.enderGeode, "ender");
    crystalBud(tasks, cache, key(TinkerWorld.steelCluster.get()).getPath(), "geode/steel_cluster", "block/geode/steel_cluster");
    crystalBud(tasks, cache, key(TinkerWorld.cobaltCluster.get()).getPath(), "geode/cobalt_cluster", "block/geode/cobalt_cluster");
    crystalBud(tasks, cache, key(TinkerWorld.knightmetalCluster.get()).getPath(), "geode/knightmetal_cluster", "block/geode/knightmetal_cluster");
    TinkerWorld.heads.forEach((type, block) -> skull(tasks, cache, key(block).getPath()));
    TinkerWorld.wallHeads.forEach((type, block) -> skull(tasks, cache, key(block).getPath()));
    simpleCubeBlock(tasks, cache, key(TinkerSmeltery.searedLamp.get()).getPath(), "smeltery/seared/lamp", "block/smeltery/seared/lamp");
    simpleCubeBlock(tasks, cache, key(TinkerSmeltery.scorchedLamp.get()).getPath(), "foundry/scorched/lamp", "block/foundry/scorched/lamp");
    glass(tasks, cache, "clear_glass", "clear_glass_pane", "clear_glass", "block/clear_glass", null, -1, true, "minecraft:cutout");
    for (GlassColor color : GlassColor.values()) {
      String name = color.getSerializedName();
      glass(tasks, cache, name + "_clear_stained_glass", name + "_clear_stained_glass_pane", "clear_glass/" + name,
            "block/clear_stained_glass", null, 0xFF000000 | color.getColor(), false, "minecraft:translucent");
    }
    glass(tasks, cache, "soul_glass", "soul_glass_pane", "soul_glass", "block/soul_glass", null, -1, false, "minecraft:translucent");
    glass(tasks, cache, "seared_glass", "seared_glass_pane", "smeltery/glass", "block/smeltery/seared_glass", null, -1, true, "minecraft:cutout");
    glass(tasks, cache, "seared_soul_glass", "seared_soul_glass_pane", "smeltery/soul_glass", "block/smeltery/soul_glass", "block/smeltery/seared_glass_top", -1, true, "minecraft:translucent");
    glass(tasks, cache, "scorched_glass", "scorched_glass_pane", "foundry/glass", "block/foundry/glass", null, -1, true, "minecraft:cutout");
    glass(tasks, cache, "scorched_soul_glass", "scorched_soul_glass_pane", "foundry/soul_glass", "block/foundry/soul_glass", "block/foundry/glass_top", -1, true, "minecraft:translucent");
    pane(tasks, cache, "obsidian_pane", "obsidian_pane", "minecraft:block/obsidian", "minecraft:block/obsidian", false, -1, false, "minecraft:solid");
    return GenericDataProvider.allOf(tasks);
  }

  private void glass(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String paneName, String baseName, String paneTexture, String edgeTexture, int tint, boolean solidEdge, String renderType) {
    String edge = edgeTexture == null ? paneTexture + "_top" : edgeTexture;
    String model = "block/" + baseName + "/block";
    tasks.add(save(cache, blockstates.json(id(blockName)), variant("tconstruct:" + model)));
    tasks.add(save(cache, blockModels.json(id(baseName + "/block")), connectedCube(paneTexture, tint, renderType)));
    itemModel(tasks, cache, id(blockName), parent("tconstruct:" + model));
    pane(tasks, cache, paneName, baseName + "/pane", ns(paneTexture), ns(edge), true, tint, solidEdge, renderType);
  }

  private void pane(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String baseName, String paneTexture, String edgeTexture, boolean connected, int tint, boolean solidEdge, String renderType) {
    tasks.add(save(cache, blockstates.json(id(blockName)), paneState(baseName, solidEdge && !paneTexture.equals(edgeTexture))));
    for (String variant : new String[] {"post", "side", "side_alt", "noside", "noside_alt"}) {
      tasks.add(save(cache, blockModels.json(id(baseName + "_" + variant)), paneModel(variant, paneTexture, variant.startsWith("noside") ? null : edgeTexture, connected, tint, renderType)));
    }
    if (solidEdge && !paneTexture.equals(edgeTexture)) {
      tasks.add(save(cache, blockModels.json(id(baseName + "_noside_edge")), paneModel("noside", paneTexture, edgeTexture, false, tint, renderType)));
    }
    itemModel(tasks, cache, id(blockName), paneItem(paneTexture, tint, renderType));
  }

  private static Identifier id(String path) {
    return TConstruct.getResource(path);
  }

  @SuppressWarnings("deprecation")
  private static Identifier key(Block block) {
    return BuiltInRegistries.BLOCK.getKey(block);
  }

  private static String ns(String texture) {
    return texture.indexOf(':') >= 0 ? texture : "tconstruct:" + texture;
  }

  private static JsonObject variant(String model) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    JsonObject normal = new JsonObject();
    normal.addProperty("model", model);
    variants.add("", normal);
    state.add("variants", variants);
    return state;
  }

  private static JsonObject parent(String parent) {
    JsonObject json = new JsonObject();
    json.addProperty("parent", parent);
    return json;
  }

  private static JsonObject connectedCube(String texture, int tint, String renderType) {
    JsonObject json = parent("minecraft:block/cube_all");
    json.addProperty("loader", "mantle:connected");
    json.addProperty("render_type", renderType);
    json.add("elements", cubeElements(tint));
    JsonObject textures = new JsonObject();
    textures.addProperty("all", ns(texture));
    json.add("textures", textures);
    JsonObject connection = new JsonObject();
    JsonObject connectedTextures = new JsonObject();
    connectedTextures.addProperty("all", "cornerless_full");
    connection.add("textures", connectedTextures);
    json.add("connection", connection);
    addTint(json, tint, "colors");
    return json;
  }

  private static JsonObject paneModel(String variant, String paneTexture, String edgeTexture, boolean connected, int tint, String renderType) {
    JsonObject json = parent("tconstruct:block/template/pane/" + variant);
    json.addProperty("render_type", renderType);
    json.add("elements", paneElements(variant, tint));
    JsonObject textures = new JsonObject();
    textures.addProperty("pane", paneTexture);
    if (edgeTexture != null) {
      textures.addProperty("edge", edgeTexture);
    }
    json.add("textures", textures);
    if (connected) {
      json.addProperty("loader", "mantle:connected");
      JsonObject connection = new JsonObject();
      connection.addProperty("predicate", "pane");
      JsonObject connectedTextures = new JsonObject();
      connectedTextures.addProperty("pane", "cornerless_full");
      connection.add("textures", connectedTextures);
      json.add("connection", connection);
      addTint(json, tint, "colors");
    } else if (tint != -1) {
      json.addProperty("loader", "mantle:colored_block");
      addTint(json, tint, "colors");
    }
    return json;
  }

  private static JsonObject paneItem(String texture, int tint, String renderType) {
    JsonObject json = parent("minecraft:item/generated");
    json.addProperty("render_type", renderType);
    JsonObject textures = new JsonObject();
    textures.addProperty("layer0", texture);
    json.add("textures", textures);
    if (tint != -1) {
      json.addProperty("loader", "mantle:item_layer");
      addTint(json, tint, "layers");
    }
    return json;
  }


  /** Inline cube geometry for custom NeoForge model loaders, as parent elements are not inherited into custom geometry. */
  private static JsonArray cubeElements(int tint) {
    JsonArray elements = new JsonArray();
    JsonObject element = new JsonObject();
    element.add("from", array(0, 0, 0));
    element.add("to", array(16, 16, 16));
    JsonObject faces = new JsonObject();
    face(faces, "down", "#all", "down", tint);
    face(faces, "up", "#all", "up", tint);
    face(faces, "north", "#all", "north", tint);
    face(faces, "south", "#all", "south", tint);
    face(faces, "west", "#all", "west", tint);
    face(faces, "east", "#all", "east", tint);
    element.add("faces", faces);
    elements.add(element);
    return elements;
  }

  /** Inline pane geometry for custom NeoForge model loaders, as parent elements are not inherited into custom geometry. */
  private static JsonArray paneElements(String variant, int tint) {
    JsonArray elements = new JsonArray();
    JsonObject element = new JsonObject();
    JsonObject faces = new JsonObject();
    switch (variant) {
      case "post" -> {
        element.add("from", array(7, 0, 7));
        element.add("to", array(9, 16, 9));
        face(faces, "down", "#edge", "down", 7, 7, 9, 9, tint);
        face(faces, "up", "#edge", "up", 7, 7, 9, 9, tint);
      }
      case "side" -> {
        element.add("from", array(7, 0, 0));
        element.add("to", array(9, 16, 7));
        face(faces, "down", "#edge", "down", 7, 0, 9, 7, tint);
        face(faces, "up", "#edge", "up", 7, 0, 9, 7, tint);
        face(faces, "north", "#edge", "north", 7, 0, 9, 16, tint);
        face(faces, "west", "#pane", null, 16, 0, 9, 16, tint);
        face(faces, "east", "#pane", null, 9, 0, 16, 16, tint);
      }
      case "side_alt" -> {
        element.add("from", array(7, 0, 9));
        element.add("to", array(9, 16, 16));
        face(faces, "down", "#edge", "down", 7, 0, 9, 7, tint);
        face(faces, "up", "#edge", "up", 7, 0, 9, 7, tint);
        face(faces, "south", "#edge", "south", 7, 0, 9, 16, tint);
        face(faces, "west", "#pane", null, 7, 0, 0, 16, tint);
        face(faces, "east", "#pane", null, 0, 0, 7, 16, tint);
      }
      case "noside_alt" -> {
        element.add("from", array(7, 0, 7));
        element.add("to", array(9, 16, 9));
        face(faces, "east", "#pane", null, 7, 0, 9, 16, tint);
      }
      default -> {
        element.add("from", array(7, 0, 7));
        element.add("to", array(9, 16, 9));
        face(faces, "north", "#pane", null, 9, 0, 7, 16, tint);
      }
    }
    element.add("faces", faces);
    elements.add(element);
    return elements;
  }

  private static JsonArray array(int... values) {
    JsonArray array = new JsonArray();
    for (int value : values) {
      array.add(value);
    }
    return array;
  }

  private static void face(JsonObject faces, String side, String texture, String cullface, int tint) {
    JsonObject face = new JsonObject();
    face.addProperty("texture", texture);
    if (cullface != null) {
      face.addProperty("cullface", cullface);
    }
    addFaceTint(face, tint);
    faces.add(side, face);
  }

  private static void face(JsonObject faces, String side, String texture, String cullface, int u1, int v1, int u2, int v2, int tint) {
    JsonObject face = new JsonObject();
    face.add("uv", array(u1, v1, u2, v2));
    face.addProperty("texture", texture);
    if (cullface != null) {
      face.addProperty("cullface", cullface);
    }
    addFaceTint(face, tint);
    faces.add(side, face);
  }

  private static void addFaceTint(JsonObject face, int tint) {
    if (tint != -1) {
      JsonObject data = new JsonObject();
      data.addProperty("color", String.format("%08X", tint));
      face.add("neoforge_data", data);
    }
  }
  private static void addTint(JsonObject json, int tint, String key) {
    if (tint == -1) {
      return;
    }
    JsonArray array = new JsonArray();
    JsonObject color = new JsonObject();
    color.addProperty("color", String.format("%08X", tint));
    array.add(color);
    json.add(key, array);
  }

  private static JsonObject paneState(String baseName, boolean hasEdge) {
    JsonObject state = new JsonObject();
    JsonArray multipart = new JsonArray();
    part(multipart, baseName + "_post", null, 0);
    part(multipart, baseName + "_side", when("north", false), 0);
    part(multipart, baseName + "_noside", when("north", true), 0);
    if (hasEdge) edgePart(multipart, baseName, 0, "east", "north", "west");
    part(multipart, baseName + "_side_alt", when("south", false), 0);
    part(multipart, baseName + "_noside_alt", when("south", true), 90);
    if (hasEdge) edgePart(multipart, baseName, 180, "east", "south", "west");
    part(multipart, baseName + "_side_alt", when("west", false), 90);
    part(multipart, baseName + "_noside", when("west", true), 270);
    if (hasEdge) edgePart(multipart, baseName, 270, "north", "south", "west");
    part(multipart, baseName + "_side", when("east", false), 90);
    part(multipart, baseName + "_noside_alt", when("east", true), 0);
    if (hasEdge) edgePart(multipart, baseName, 90, "east", "north", "south");
    state.add("multipart", multipart);
    return state;
  }

  private static JsonObject when(String key, boolean invert) {
    JsonObject when = new JsonObject();
    when.addProperty(key, invert ? "false" : "true");
    return when;
  }

  private static void edgePart(JsonArray multipart, String baseName, int y, String... keys) {
    JsonObject when = new JsonObject();
    for (String key : keys) {
      when.addProperty(key, "false");
    }
    part(multipart, baseName + "_noside_edge", when, y);
  }

  private static void part(JsonArray multipart, String model, JsonObject when, int y) {
    JsonObject part = new JsonObject();
    JsonObject apply = new JsonObject();
    apply.addProperty("model", "tconstruct:block/" + model);
    if (y != 0) {
      apply.addProperty("y", y);
    }
    part.add("apply", apply);
    if (when != null) {
      part.add("when", when);
    }
    multipart.add(part);
  }


  private void wood(List<CompletableFuture<?>> tasks, CachedOutput cache, WoodBlockObject wood, boolean orientableTrapdoor) {
    String plankName = wood.getId().getPath();
    String name = plankName.substring(0, plankName.length() - "_planks".length());
    String folder = "wood/" + name;
    String planks = "block/wood/" + name + "/planks";
    fenceBuilding(tasks, cache, wood, folder, planks);
    fenceGate(tasks, cache, key(wood.getFenceGate()).getPath(), folder + "/fence/gate", planks);
    axisBlock(tasks, cache, key(wood.getLog()).getPath(), folder + "/log/log", "block/wood/" + name + "/log", true);
    axisBlock(tasks, cache, key(wood.getStrippedLog()).getPath(), folder + "/log/stripped", "block/wood/" + name + "/stripped_log", true);
    axisBlock(tasks, cache, key(wood.getWood()).getPath(), folder + "/log/wood", "block/wood/" + name + "/log", false);
    axisBlock(tasks, cache, key(wood.getStrippedWood()).getPath(), folder + "/log/wood_stripped", "block/wood/" + name + "/stripped_log", false);
    pressurePlate(tasks, cache, key(wood.getPressurePlate()).getPath(), folder + "/pressure_plate", planks);
    button(tasks, cache, key(wood.getButton()).getPath(), folder + "/button", planks);
    trapdoor(tasks, cache, key(wood.getTrapdoor()).getPath(), folder + "/trapdoor", "block/wood/" + name + "/trapdoor", orientableTrapdoor);
    door(tasks, cache, key(wood.getDoor()).getPath(), folder + "/door", "block/wood/" + name + "/door_bottom", "block/wood/" + name + "/door_top");
    sign(tasks, cache, key(wood.getSign()).getPath(), key(wood.getWallSign()).getPath(), folder + "/sign", planks);
    hangingSign(tasks, cache, key(wood.getHangingSign()).getPath(), key(wood.getWallHangingSign()).getPath(), folder + "/hanging_sign", "block/wood/" + name + "/stripped_log");
  }

  private void fenceBuilding(List<CompletableFuture<?>> tasks, CachedOutput cache, FenceBuildingBlockObject block, String folder, String texture) {
    String name = block.getId().getPath();
    String model = folder + "/planks";
    tasks.add(save(cache, blockstates.json(id(name)), variant(modelId(model))));
    tasks.add(save(cache, blockModels.json(id(model)), cubeAll(ns(texture))));
    itemModel(tasks, cache, id(name), parent(modelId(model)));
    slab(tasks, cache, key(block.getSlab()).getPath(), folder + "/slab", model, texture);
    stairs(tasks, cache, key(block.getStairs()).getPath(), folder + "/stairs", texture);
    fence(tasks, cache, key(block.getFence()).getPath(), folder + "/fence", texture);
  }

  private void slab(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String base, String doubleModel, String texture) {
    tasks.add(save(cache, blockModels.json(id(base)), texturedParent("minecraft:block/slab", texture, "bottom", "top", "side")));
    tasks.add(save(cache, blockModels.json(id(base + "_top")), texturedParent("minecraft:block/slab_top", texture, "bottom", "top", "side")));
    tasks.add(save(cache, blockstates.json(id(blockName)), slabState(base, doubleModel)));
    itemModel(tasks, cache, id(blockName), parent(modelId(base)));
  }

  private void stairs(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String base, String texture) {
    tasks.add(save(cache, blockModels.json(id(base)), texturedParent("minecraft:block/stairs", texture, "bottom", "top", "side")));
    tasks.add(save(cache, blockModels.json(id(base + "_inner")), texturedParent("minecraft:block/inner_stairs", texture, "bottom", "top", "side")));
    tasks.add(save(cache, blockModels.json(id(base + "_outer")), texturedParent("minecraft:block/outer_stairs", texture, "bottom", "top", "side")));
    tasks.add(save(cache, blockstates.json(id(blockName)), stairsState(base)));
    itemModel(tasks, cache, id(blockName), parent(modelId(base)));
  }

  private void fence(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String base, String texture) {
    tasks.add(save(cache, blockModels.json(id(base + "/post")), textureModel("minecraft:block/fence_post", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "/side")), textureModel("minecraft:block/fence_side", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "/inventory")), textureModel("minecraft:block/fence_inventory", "texture", texture)));
    tasks.add(save(cache, blockstates.json(id(blockName)), fenceState(base)));
    itemModel(tasks, cache, id(blockName), parent(modelId(base + "/inventory")));
  }

  private void fenceGate(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String base, String texture) {
    tasks.add(save(cache, blockModels.json(id(base)), textureModel("minecraft:block/template_fence_gate", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "_open")), textureModel("minecraft:block/template_fence_gate_open", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "_wall")), textureModel("minecraft:block/template_fence_gate_wall", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "_wall_open")), textureModel("minecraft:block/template_fence_gate_wall_open", "texture", texture)));
    tasks.add(save(cache, blockstates.json(id(blockName)), fenceGateState(base)));
    itemModel(tasks, cache, id(blockName), parent(modelId(base)));
  }

  private void axisBlock(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String base, String side, boolean horizontal) {
    String end = horizontal ? side + "_top" : side;
    tasks.add(save(cache, blockModels.json(id(base)), cubeColumn(ns(side), ns(end))));
    tasks.add(save(cache, blockModels.json(id(base + "_horizontal")), cubeColumn(ns(side), ns(end))));
    tasks.add(save(cache, blockstates.json(id(blockName)), axisState(base)));
    itemModel(tasks, cache, id(blockName), parent(modelId(base)));
  }

  private void pressurePlate(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String base, String texture) {
    tasks.add(save(cache, blockModels.json(id(base)), textureModel("minecraft:block/pressure_plate_up", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "_down")), textureModel("minecraft:block/pressure_plate_down", "texture", texture)));
    tasks.add(save(cache, blockstates.json(id(blockName)), poweredState(base, base + "_down")));
    itemModel(tasks, cache, id(blockName), parent(modelId(base)));
  }

  private void button(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String base, String texture) {
    tasks.add(save(cache, blockModels.json(id(base)), textureModel("minecraft:block/button", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "_pressed")), textureModel("minecraft:block/button_pressed", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "_inventory")), textureModel("minecraft:block/button_inventory", "texture", texture)));
    tasks.add(save(cache, blockstates.json(id(blockName)), buttonState(base)));
    itemModel(tasks, cache, id(blockName), parent(modelId(base + "_inventory")));
  }

  private void trapdoor(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String base, String texture, boolean orientable) {
    String prefix = orientable ? "orientable_" : "";
    tasks.add(save(cache, blockModels.json(id(base + "_bottom")), textureModel("minecraft:block/template_" + prefix + "trapdoor_bottom", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "_top")), textureModel("minecraft:block/template_" + prefix + "trapdoor_top", "texture", texture)));
    tasks.add(save(cache, blockModels.json(id(base + "_open")), textureModel("minecraft:block/template_" + prefix + "trapdoor_open", "texture", texture)));
    tasks.add(save(cache, blockstates.json(id(blockName)), trapdoorState(base)));
    itemModel(tasks, cache, id(blockName), parent(modelId(base + "_bottom")));
  }


  private void door(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String base, String bottomTexture, String topTexture) {
    for (String half : new String[] {"bottom", "top"}) {
      for (String hinge : new String[] {"left", "right"}) {
        tasks.add(save(cache, blockModels.json(id(base + "_" + half + "_" + hinge)), doorModel("minecraft:block/door_" + half + "_" + hinge, bottomTexture, topTexture)));
        tasks.add(save(cache, blockModels.json(id(base + "_" + half + "_" + hinge + "_open")), doorModel("minecraft:block/door_" + half + "_" + hinge + "_open", bottomTexture, topTexture)));
      }
    }
    tasks.add(save(cache, blockstates.json(id(blockName)), doorState(base)));
    JsonObject item = parent("minecraft:item/generated");
    JsonObject textures = new JsonObject();
    textures.addProperty("layer0", ns("item/wood/" + blockName));
    item.add("textures", textures);
    itemModel(tasks, cache, id(blockName), item);
  }

  private void sign(List<CompletableFuture<?>> tasks, CachedOutput cache, String signName, String wallSignName, String base, String particleTexture) {
    tasks.add(save(cache, blockModels.json(id(base)), particleModel(particleTexture)));
    tasks.add(save(cache, blockstates.json(id(signName)), variant(modelId(base))));
    tasks.add(save(cache, blockstates.json(id(wallSignName)), variant(modelId(base))));
    JsonObject item = parent("minecraft:item/generated");
    JsonObject textures = new JsonObject();
    textures.addProperty("layer0", ns("item/wood/" + signName));
    item.add("textures", textures);
    itemModel(tasks, cache, id(signName), item);
  }

  private void hangingSign(List<CompletableFuture<?>> tasks, CachedOutput cache, String signName, String wallSignName, String base, String particleTexture) {
    tasks.add(save(cache, blockModels.json(id(base)), particleModel(particleTexture)));
    tasks.add(save(cache, blockstates.json(id(signName)), variant(modelId(base))));
    tasks.add(save(cache, blockstates.json(id(wallSignName)), variant(modelId(base))));
    JsonObject item = parent("minecraft:item/generated");
    JsonObject textures = new JsonObject();
    textures.addProperty("layer0", ns("item/wood/" + signName));
    item.add("textures", textures);
    itemModel(tasks, cache, id(signName), item);
  }
  private void skull(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName) {
    tasks.add(save(cache, blockstates.json(id(blockName)), variant("minecraft:block/skull")));
  }
  private void roots(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String model, String side, String top) {
    tasks.add(save(cache, blockstates.json(id(blockName)), variant(modelId(model))));
    JsonObject json = parent("minecraft:block/mangrove_roots");
    JsonObject textures = new JsonObject();
    textures.addProperty("side", ns(side));
    textures.addProperty("top", ns(top));
    textures.addProperty("particle", "#side");
    json.add("textures", textures);
    tasks.add(save(cache, blockModels.json(id(model)), json));
    itemModel(tasks, cache, id(blockName), parent(modelId(model)));
  }

  private void simpleCubeBlock(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String model, String texture) {
    tasks.add(save(cache, blockstates.json(id(blockName)), variant(modelId(model))));
    tasks.add(save(cache, blockModels.json(id(model)), cubeAll(ns(texture))));
    itemModel(tasks, cache, id(blockName), parent(modelId(model)));
  }

  private void geode(List<CompletableFuture<?>> tasks, CachedOutput cache, GeodeItemObject geode, String color) {
    simpleCubeBlock(tasks, cache, key(geode.getBlock()).getPath(), "geode/" + color + "/block", "block/geode/" + color + "/block");
    simpleCubeBlock(tasks, cache, key(geode.getBudding()).getPath(), "geode/" + color + "/budding", "block/geode/" + color + "/budding");
    crystalBud(tasks, cache, key(geode.getBud(BudSize.CLUSTER)).getPath(), "geode/" + color + "/cluster", "block/geode/" + color + "/cluster");
    crystalBud(tasks, cache, key(geode.getBud(BudSize.SMALL)).getPath(), "geode/" + color + "/small_bud", "block/geode/" + color + "/small_bud");
    crystalBud(tasks, cache, key(geode.getBud(BudSize.MEDIUM)).getPath(), "geode/" + color + "/medium_bud", "block/geode/" + color + "/medium_bud");
    crystalBud(tasks, cache, key(geode.getBud(BudSize.LARGE)).getPath(), "geode/" + color + "/large_bud", "block/geode/" + color + "/large_bud");
  }

  private void crystalBud(List<CompletableFuture<?>> tasks, CachedOutput cache, String blockName, String model, String texture) {
    tasks.add(save(cache, blockstates.json(id(blockName)), facingState(model)));
    JsonObject block = parent("minecraft:block/cross");
    JsonObject blockTextures = new JsonObject();
    blockTextures.addProperty("cross", ns(texture));
    block.add("textures", blockTextures);
    tasks.add(save(cache, blockModels.json(id(model)), block));

    JsonObject item = parent("minecraft:item/generated");
    JsonObject itemTextures = new JsonObject();
    itemTextures.addProperty("layer0", ns(texture));
    item.add("textures", itemTextures);
    itemModel(tasks, cache, id(blockName), item);
  }

  private static JsonObject facingState(String model) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    variants.add("facing=up", model(modelId(model)));
    JsonObject down = model(modelId(model)); down.addProperty("x", 180); variants.add("facing=down", down);
    JsonObject north = model(modelId(model)); north.addProperty("x", 90); variants.add("facing=north", north);
    JsonObject south = model(modelId(model)); south.addProperty("x", 90); south.addProperty("y", 180); variants.add("facing=south", south);
    JsonObject east = model(modelId(model)); east.addProperty("x", 90); east.addProperty("y", 90); variants.add("facing=east", east);
    JsonObject west = model(modelId(model)); west.addProperty("x", 90); west.addProperty("y", 270); variants.add("facing=west", west);
    state.add("variants", variants);
    return state;
  }
  private static String modelId(String path) {
    return "tconstruct:block/" + path;
  }

  private static JsonObject cubeAll(String texture) {
    JsonObject json = parent("minecraft:block/cube_all");
    JsonObject textures = new JsonObject();
    textures.addProperty("all", texture);
    json.add("textures", textures);
    return json;
  }

  private static JsonObject cubeColumn(String side, String end) {
    JsonObject json = parent("minecraft:block/cube_column");
    JsonObject textures = new JsonObject();
    textures.addProperty("side", side);
    textures.addProperty("end", end);
    json.add("textures", textures);
    return json;
  }

  private static JsonObject textureModel(String parent, String key, String texture) {
    JsonObject json = parent(parent);
    JsonObject textures = new JsonObject();
    textures.addProperty(key, ns(texture));
    json.add("textures", textures);
    return json;
  }


  private static JsonObject doorModel(String parent, String bottomTexture, String topTexture) {
    JsonObject json = parent(parent);
    JsonObject textures = new JsonObject();
    textures.addProperty("bottom", ns(bottomTexture));
    textures.addProperty("top", ns(topTexture));
    json.add("textures", textures);
    return json;
  }

  private static JsonObject particleModel(String particleTexture) {
    JsonObject json = new JsonObject();
    JsonObject textures = new JsonObject();
    textures.addProperty("particle", ns(particleTexture));
    json.add("textures", textures);
    return json;
  }
  private static JsonObject texturedParent(String parent, String texture, String... keys) {
    JsonObject json = parent(parent);
    JsonObject textures = new JsonObject();
    for (String key : keys) {
      textures.addProperty(key, ns(texture));
    }
    json.add("textures", textures);
    return json;
  }

  private static JsonObject slabState(String base, String doubleModel) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    for (String waterlogged : new String[]{"false", "true"}) {
      variants.add("type=bottom,waterlogged=" + waterlogged, model(modelId(base)));
      variants.add("type=top,waterlogged=" + waterlogged, model(modelId(base + "_top")));
      variants.add("type=double,waterlogged=" + waterlogged, model(modelId(doubleModel)));
    }
    state.add("variants", variants);
    return state;
  }

  private static JsonObject stairsState(String base) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    String[] facings = {"east", "west", "south", "north"};
    String[] halves = {"bottom", "top"};
    String[] shapes = {"straight", "inner_left", "inner_right", "outer_left", "outer_right"};
    for (String facing : facings) for (String half : halves) for (String shape : shapes) for (String waterlogged : new String[]{"false", "true"}) {
      String suffix = shape.startsWith("inner") ? "_inner" : shape.startsWith("outer") ? "_outer" : "";
      JsonObject model = model(modelId(base + suffix));
      int y = switch (facing) { case "south" -> 90; case "west" -> 180; case "north" -> 270; default -> 0; };
      if (shape.endsWith("right")) y = (y + 90) % 360;
      if (y != 0) model.addProperty("y", y);
      if (half.equals("top")) model.addProperty("x", 180);
      variants.add("facing=" + facing + ",half=" + half + ",shape=" + shape + ",waterlogged=" + waterlogged, model);
    }
    state.add("variants", variants);
    return state;
  }

  private static JsonObject fenceState(String base) {
    JsonObject state = new JsonObject();
    JsonArray multipart = new JsonArray();
    part(multipart, base + "/post", null, 0);
    part(multipart, base + "/side", when("north", false), 0);
    part(multipart, base + "/side", when("east", false), 90);
    part(multipart, base + "/side", when("south", false), 180);
    part(multipart, base + "/side", when("west", false), 270);
    state.add("multipart", multipart);
    return state;
  }

  private static JsonObject fenceGateState(String base) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    for (String facing : new String[]{"north", "east", "south", "west"}) for (String inWall : new String[]{"false", "true"}) for (String open : new String[]{"false", "true"}) for (String powered : new String[]{"false", "true"}) {
      String modelName = base + (inWall.equals("true") ? "_wall" : "") + (open.equals("true") ? "_open" : "");
      JsonObject model = model(modelId(modelName));
      int y = switch (facing) { case "east" -> 270; case "south" -> 180; case "west" -> 90; default -> 0; };
      if (y != 0) model.addProperty("y", y);
      variants.add("facing=" + facing + ",in_wall=" + inWall + ",open=" + open + ",powered=" + powered, model);
    }
    state.add("variants", variants);
    return state;
  }

  private static JsonObject axisState(String base) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    variants.add("axis=y", model(modelId(base)));
    JsonObject x = model(modelId(base + "_horizontal")); x.addProperty("x", 90); x.addProperty("y", 90); variants.add("axis=x", x);
    JsonObject z = model(modelId(base + "_horizontal")); z.addProperty("x", 90); variants.add("axis=z", z);
    state.add("variants", variants);
    return state;
  }

  private static JsonObject poweredState(String off, String on) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    variants.add("powered=false", model(modelId(off)));
    variants.add("powered=true", model(modelId(on)));
    state.add("variants", variants);
    return state;
  }

  private static JsonObject buttonState(String base) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    for (String face : new String[]{"floor", "wall", "ceiling"}) for (String facing : new String[]{"north", "east", "south", "west"}) for (String powered : new String[]{"false", "true"}) {
      JsonObject model = model(modelId(base + (powered.equals("true") ? "_pressed" : "")));
      int y = switch (facing) { case "east" -> 90; case "south" -> 180; case "west" -> 270; default -> 0; };
      int x = face.equals("ceiling") ? 180 : face.equals("wall") ? 90 : 0;
      if (x != 0) model.addProperty("x", x);
      if (y != 0) model.addProperty("y", y);
      variants.add("face=" + face + ",facing=" + facing + ",powered=" + powered, model);
    }
    state.add("variants", variants);
    return state;
  }

  private static JsonObject trapdoorState(String base) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    for (String facing : new String[]{"north", "east", "south", "west"}) for (String half : new String[]{"bottom", "top"}) for (String open : new String[]{"false", "true"}) for (String powered : new String[]{"false", "true"}) for (String waterlogged : new String[]{"false", "true"}) {
      JsonObject model = model(modelId(base + (open.equals("true") ? "_open" : "_" + half)));
      int y = switch (facing) { case "east" -> 90; case "south" -> 180; case "west" -> 270; default -> 0; };
      if (y != 0) model.addProperty("y", y);
      variants.add("facing=" + facing + ",half=" + half + ",open=" + open + ",powered=" + powered + ",waterlogged=" + waterlogged, model);
    }
    state.add("variants", variants);
    return state;
  }


  private static JsonObject doorState(String base) {
    JsonObject state = new JsonObject();
    JsonObject variants = new JsonObject();
    for (String facing : new String[]{"east", "north", "south", "west"}) for (String half : new String[]{"lower", "upper"}) for (String hinge : new String[]{"left", "right"}) for (String open : new String[]{"false", "true"}) for (String powered : new String[]{"false", "true"}) {
      String modelName = base + "_" + (half.equals("lower") ? "bottom" : "top") + "_" + hinge + (open.equals("true") ? "_open" : "");
      JsonObject model = model(modelId(modelName));
      int y = switch (facing) {
        case "north" -> open.equals("true") ? (hinge.equals("left") ? 0 : 180) : 270;
        case "south" -> open.equals("true") ? (hinge.equals("left") ? 180 : 0) : 90;
        case "west" -> open.equals("true") ? (hinge.equals("left") ? 270 : 90) : 180;
        default -> open.equals("true") ? (hinge.equals("left") ? 90 : 270) : 0;
      };
      if (y != 0) {
        model.addProperty("y", y);
      }
      variants.add("facing=" + facing + ",half=" + half + ",hinge=" + hinge + ",open=" + open + ",powered=" + powered, model);
    }
    state.add("variants", variants);
    return state;
  }
  private static JsonObject model(String model) {
    JsonObject json = new JsonObject();
    json.addProperty("model", model);
    return json;
  }

  private void itemModel(List<CompletableFuture<?>> tasks, CachedOutput cache, Identifier item, JsonObject model) {
    tasks.add(save(cache, itemModels.json(item), model));
    tasks.add(save(cache, itemDefinitions.json(item), itemDefinition(item)));
  }

  private static JsonObject itemDefinition(Identifier item) {
    JsonObject definition = new JsonObject();
    JsonObject model = new JsonObject();
    model.addProperty("type", isRetexturedItem(item) ? "mantle:retextured_item" : "minecraft:model");
    model.addProperty("model", item.getNamespace() + ":item/" + item.getPath());
    definition.add("model", model);
    return definition;
  }

  private static boolean isRetexturedItem(Identifier item) {
    if (!TConstruct.MOD_ID.equals(item.getNamespace())) {
      return false;
    }
    return switch (item.getPath()) {
      case "crafting_station", "part_builder", "tinker_station", "modifier_worktable", "tinkers_anvil", "scorched_anvil",
           "smeltery_controller", "seared_drain", "seared_duct", "seared_chute", "seared_melter", "seared_heater",
           "foundry_controller", "scorched_drain", "scorched_duct", "scorched_chute", "scorched_alloyer" -> true;
      default -> false;
    };
  }
  private static CompletableFuture<?> save(CachedOutput cache, Path path, JsonObject json) {
    return DataProvider.saveStable(cache, json, path);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct blockstate/model provider";
  }
}