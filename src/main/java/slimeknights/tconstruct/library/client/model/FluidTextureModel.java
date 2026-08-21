package slimeknights.tconstruct.library.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.model.data.ModelProperty;
import slimeknights.mantle.client.model.RetexturedBlockStateModel;
import slimeknights.mantle.client.model.builder.ColorData;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.util.RetexturedHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Model replacing selected texture slots with the fluid stored in model data. */
public final class FluidTextureModel extends AbstractUnbakedModel {
  public static final UnbakedModelLoader<FluidTextureModel> LOADER = FluidTextureModel::deserialize;

  private final List<CuboidModelElement> elements;
  private final Set<String> fluids;
  private final Set<String> retextured;
  private final List<ColorData> colors;

  private FluidTextureModel(StandardModelParameters parameters, List<CuboidModelElement> elements,
                            Set<String> fluids, Set<String> retextured, List<ColorData> colors) {
    super(parameters);
    this.elements = elements;
    this.fluids = fluids;
    this.retextured = retextured;
    this.colors = colors;
  }

  @Override
  public ExtendedUnbakedGeometry geometry() {
    return new Geometry(elements, parameters.textures());
  }

  /** Creates the dynamic block-state model used by Mantle's retextured block wrapper. */
  public DynamicBlockStateModel bakeDynamic(ModelBaker baker, ResolvedModel resolved, ModelState modelState) {
    return new Baked(baker, resolved, modelState, SimpleModelWrapper.bake(baker, resolved, modelState));
  }

  public Set<String> fluids() {
    return fluids;
  }

  public Set<String> retextured() {
    return retextured;
  }

  public static FluidTextureModel deserialize(JsonObject json, JsonDeserializationContext context) {
    StandardModelParameters parameters = StandardModelParameters.parse(json, context);
    List<CuboidModelElement> elements = parseElements(json, context);
    Set<String> fluids = parseNames(json, "fluids");
    Set<String> retextured = parseNames(json, "retextured");
    List<ColorData> colors = ColorData.LIST_LOADABLE.getOrDefault(json, "colors", List.of());
    return new FluidTextureModel(parameters, elements, fluids, retextured, colors);
  }

  private static List<CuboidModelElement> parseElements(JsonObject json, JsonDeserializationContext context) {
    if (!json.has("elements")) {
      return List.of();
    }
    JsonArray elementsJson = GsonHelper.getAsJsonArray(json, "elements");
    List<CuboidModelElement> elements = new ArrayList<>(elementsJson.size());
    for (int i = 0; i < elementsJson.size(); i++) {
      elements.add(context.deserialize(elementsJson.get(i), CuboidModelElement.class));
    }
    return Collections.unmodifiableList(elements);
  }

  private static Set<String> parseNames(JsonObject json, String name) {
    if (!json.has(name)) {
      return Set.of();
    }
    JsonArray array = GsonHelper.getAsJsonArray(json, name);
    if (array.isEmpty()) {
      return Set.of();
    }
    Set<String> result = new java.util.HashSet<>();
    for (JsonElement element : array) {
      result.add(GsonHelper.convertToString(element, name));
    }
    return Set.copyOf(result);
  }

  private record Geometry(List<CuboidModelElement> elements, TextureSlots.Data textures) implements ExtendedUnbakedGeometry {
    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state,
                               ModelDebugName debugName, net.minecraft.util.context.ContextMap additionalProperties) {
      MaterialBaker materialBaker = baker.materials();
      Function<String, Material.Baked> materialGetter = name -> {
        String lookup = trimTextureName(name);
        Material material = textureSlots.getMaterial(lookup);
        return material == null ? null : materialBaker.get(material, debugName);
      };
      QuadCollection.Builder builder = new QuadCollection.Builder();
      if (!elements.isEmpty()) {
        UnbakedElementsHelper.bakeElements(baker, builder, elements, materialGetter, state);
      }
      return builder.build();
    }
  }

  private static String trimTextureName(String name) {
    return name.startsWith("#") ? name.substring(1) : name;
  }

  private static final class Baked implements DynamicBlockStateModel {
    private final ModelBaker baker;
    private final ResolvedModel resolved;
    private final ModelState modelState;
    private final BlockStateModelPart basePart;
    private final Map<VariantKey, BlockStateModelPart> cache = new ConcurrentHashMap<>();
    private final List<CuboidModelElement> elements;
    private final Set<String> fluidSlots;
    private final Set<String> retexturedSlots;
    @SuppressWarnings("unused")
    private final List<ColorData> colors;

    private Baked(ModelBaker baker, ResolvedModel resolved, ModelState modelState, BlockStateModelPart basePart) {
      this.baker = baker;
      this.resolved = resolved;
      this.modelState = modelState;
      this.basePart = basePart;
      FluidTextureModel source = findSource(resolved);
      this.elements = source == null ? List.of() : source.elements;
      this.fluidSlots = source == null ? Set.of() : source.fluids;
      this.retexturedSlots = source == null ? Set.of() : source.retextured;
      this.colors = source == null ? List.of() : source.colors;
    }

    private static FluidTextureModel findSource(ResolvedModel resolved) {
      for (ResolvedModel current = resolved; current != null; current = current.parent()) {
        if (current.wrapped() instanceof FluidTextureModel source) {
          return source;
        }
      }
      return null;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
      parts.add(basePart);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                             RandomSource random, List<BlockStateModelPart> parts) {
      FluidStack fluid = level.getModelData(pos).get(ModelProperties.FLUID_STACK);
      if (fluid == null) {
        fluid = FluidStack.EMPTY;
      }
      Block texture = level.getModelData(pos).get(RetexturedHelper.BLOCK_PROPERTY);
      if ((texture == null || texture == Blocks.AIR) && level.getBlockEntity(pos) instanceof IRetexturedBlockEntity retexturedEntity) {
        texture = retexturedEntity.getTexture();
      }
      if (fluid.isEmpty() && (texture == null || texture == Blocks.AIR)) {
        parts.add(basePart);
        return;
      }
      VariantKey key = new VariantKey(fluid.copy(), texture == Blocks.AIR ? null : texture);
      parts.add(cache.computeIfAbsent(key, this::bakeVariant));
    }

    private BlockStateModelPart bakeVariant(VariantKey key) {
      TextureSlots slots = resolved.getTopTextureSlots();
      MaterialBakedSet materials = new MaterialBakedSet(slots, baker, resolved, key);
      Function<String, Material.Baked> materialGetter = name -> {
        String lookup = trimTextureName(name);
        Material.Baked original = materials.original(lookup);
        if (!key.fluid().isEmpty() && fluidSlots.contains(lookup)) {
          return materials.fluidStill();
        }
        if (retexturedSlots.contains(lookup)) {
          Material.Baked retextured = materials.retextured(lookup);
          if (retextured != null) {
            return retextured;
          }
        }
        return original;
      };
      QuadCollection.Builder builder = new QuadCollection.Builder();
      if (!elements.isEmpty()) {
        UnbakedElementsHelper.bakeElements(baker, builder, elements, materialGetter, modelState);
      }
      Material.Baked particle = materials.fluidStill() != null && !key.fluid().isEmpty()
                                ? materials.fluidStill() : resolved.resolveParticleMaterial(slots, baker);
      return new SimpleModelWrapper(builder.build(), resolved.getTopAmbientOcclusion(), particle);
    }

    @Override
    public Material.Baked particleMaterial() {
      return basePart.particleMaterial();
    }

    @Override
    public int materialFlags() {
      return basePart.materialFlags();
    }

    private record VariantKey(FluidStack fluid, Block texture) {}
  }

  private static final class MaterialBakedSet {
    private final TextureSlots slots;
    private final ModelBaker modelBaker;
    private final MaterialBaker baker;
    private final ResolvedModel resolved;
    private final Baked.VariantKey key;
    private final FluidModel fluidModel;
    private Material.Baked fluidStill;

    private MaterialBakedSet(TextureSlots slots, ModelBaker baker, ResolvedModel resolved, Baked.VariantKey key) {
      this.slots = slots;
      this.modelBaker = baker;
      this.baker = baker.materials();
      this.resolved = resolved;
      this.key = key;
      this.fluidModel = key.fluid().isEmpty() ? null : Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(key.fluid().getFluid().defaultFluidState());
    }

    private Material.Baked original(String slot) {
      Material material = slots.getMaterial(slot);
      return material == null ? resolved.resolveParticleMaterial(slots, modelBaker) : baker.get(material, resolved);
    }

    private Material.Baked fluidStill() {
      if (fluidStill == null && fluidModel != null) {
        fluidStill = fluidModel.stillMaterial();
      }
      return fluidStill;
    }

    private Material.Baked retextured(String slot) {
      if (key.texture() == null || key.texture() == Blocks.AIR) {
        return null;
      }
      return RetexturedBlockStateModel.bakedTextureMaterial(key.texture(), RetexturedBlockStateModel.isEndTextureSlot(slot));
    }
  }
}
