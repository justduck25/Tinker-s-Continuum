package slimeknights.tconstruct.library.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Quadrant;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.model.data.ModelProperty;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Dynamic tank model retaining the legacy {@code loader: tconstruct:tank} JSON schema. */
public final class TankModel extends AbstractUnbakedModel {
  public static final Identifier ID = TConstruct.getResource("tank");
  public static final UnbakedModelLoader<TankModel> LOADER = TankModel::deserialize;

  private final List<CuboidModelElement> elements;
  private final IncrementalFluidCuboid fluid;
  private final boolean forceModelFluid;

  private TankModel(StandardModelParameters parameters, List<CuboidModelElement> elements,
                    IncrementalFluidCuboid fluid, boolean forceModelFluid) {
    super(parameters);
    this.elements = elements;
    this.fluid = fluid;
    this.forceModelFluid = forceModelFluid;
  }

  @Override
  public ExtendedUnbakedGeometry geometry() {
    return new Geometry(elements, parameters.textures());
  }

  private record Geometry(List<CuboidModelElement> elements, TextureSlots.Data textures) implements ExtendedUnbakedGeometry {
    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state,
                               ModelDebugName debugName, net.minecraft.util.context.ContextMap additionalProperties) {
      QuadCollection.Builder builder = new QuadCollection.Builder();
      Function<String, Material.Baked> materialGetter = name -> {
        String lookup = name.startsWith("#") ? name.substring(1) : name;
        Material material = textureSlots.getMaterial(lookup);
        return material == null ? null : baker.materials().get(material, debugName);
      };
      if (!elements.isEmpty()) {
        UnbakedElementsHelper.bakeElements(baker, builder, elements, materialGetter, state);
      }
      return builder.build();
    }
  }

  public static TankModel deserialize(JsonObject json, JsonDeserializationContext context) {
    StandardModelParameters parameters = StandardModelParameters.parse(json, context);
    List<CuboidModelElement> elements = parseElements(json, context);
    IncrementalFluidCuboid fluid = IncrementalFluidCuboid.fromJson(GsonHelper.getAsJsonObject(json, "fluid"));
    boolean force = GsonHelper.getAsBoolean(json, "render_fluid_in_model", false);
    return new TankModel(parameters, elements, fluid, force);
  }

  private static List<CuboidModelElement> parseElements(JsonObject json, JsonDeserializationContext context) {
    if (!json.has("elements")) {
      return List.of();
    }
    JsonArray array = GsonHelper.getAsJsonArray(json, "elements");
    List<CuboidModelElement> result = new ArrayList<>(array.size());
    for (JsonElementRef element : JsonElementRef.iterate(array)) {
      result.add(context.deserialize(element.element(), CuboidModelElement.class));
    }
    return Collections.unmodifiableList(result);
  }

  /** Small iterable wrapper avoids depending on removed Gson helper overloads. */
  private record JsonElementRef(com.google.gson.JsonElement element) {
    private static List<JsonElementRef> iterate(JsonArray array) {
      List<JsonElementRef> result = new ArrayList<>(array.size());
      for (int i = 0; i < array.size(); i++) {
        result.add(new JsonElementRef(array.get(i)));
      }
      return result;
    }
  }

  public record Unbaked(Identifier model, int x, int y, boolean uvlock) implements CustomUnbakedBlockStateModel {
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
      Codec.INT.optionalFieldOf("x", 0).forGetter(Unbaked::x),
      Codec.INT.optionalFieldOf("y", 0).forGetter(Unbaked::y),
      Codec.BOOL.optionalFieldOf("uvlock", false).forGetter(Unbaked::uvlock)
    ).apply(instance, Unbaked::new));

    @Override
    public BlockStateModel bake(ModelBaker baker) {
      ResolvedModel resolved = baker.getModel(model);
      TankModel tank = findTankModel(resolved);
      BlockModelRotation rotation = BlockModelRotation.get(Quadrant.fromXYAngles(quadrant(x), quadrant(y)));
      ModelState state = uvlock ? rotation.withUvLock() : rotation;
      BlockStateModelPart base = SimpleModelWrapper.bake(baker, resolved, state);
      if (tank == null) {
        return new StaticModel(base);
      }
      return new Baked(baker, resolved, state, base, tank);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      resolver.markDependency(model);
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
      return MAP_CODEC;
    }

    private static TankModel findTankModel(ResolvedModel resolved) {
      for (ResolvedModel current = resolved; current != null; current = current.parent()) {
        if (current.wrapped() instanceof TankModel model) {
          return model;
        }
      }
      return null;
    }

    private static Quadrant quadrant(int angle) {
      return switch (Math.floorMod(angle, 360)) {
        case 90 -> Quadrant.R90;
        case 180 -> Quadrant.R180;
        case 270 -> Quadrant.R270;
        default -> Quadrant.R0;
      };
    }
  }

  private static final class StaticModel implements BlockStateModel {
    private final BlockStateModelPart part;

    private StaticModel(BlockStateModelPart part) {
      this.part = part;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
      parts.add(part);
    }

    @Override
    public Material.Baked particleMaterial() {
      return part.particleMaterial();
    }

    @Override
    public int materialFlags() {
      return part.materialFlags();
    }
  }

  private static final class Baked implements DynamicBlockStateModel {
    private final ModelBaker baker;
    private final ResolvedModel model;
    private final ModelState state;
    private final BlockStateModelPart basePart;
    private final TankModel original;
    private final Map<FluidKey, BlockStateModelPart> cache = new ConcurrentHashMap<>();

    private Baked(ModelBaker baker, ResolvedModel model, ModelState state,
                  BlockStateModelPart basePart, TankModel original) {
      this.baker = baker;
      this.model = model;
      this.state = state;
      this.basePart = basePart;
      this.original = original;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
      parts.add(basePart);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                             RandomSource random, List<BlockStateModelPart> parts) {
      if (!original.forceModelFluid && !Config.CLIENT.tankFluidModel.get()) {
        parts.add(basePart);
        return;
      }
      FluidStack stack = level.getModelData(pos).get(ModelProperties.FLUID_STACK);
      if (stack == null || stack.isEmpty()) {
        parts.add(basePart);
        return;
      }
      Integer capacityValue = level.getModelData(pos).get(ModelProperties.TANK_CAPACITY);
      int capacity = capacityValue == null || capacityValue <= 0 ? stack.getAmount() : capacityValue;
      int increments = original.fluid.getIncrements();
      int scaled = Mth.clamp(stack.getAmount() * increments / Math.max(1, capacity), 1, increments);
      parts.add(new CombinedPart(basePart, cache.computeIfAbsent(new FluidKey(stack.copy(), scaled), key -> bakeFluid(key.fluid(), key.increments()))));
    }

    @Override
    public Material.Baked particleMaterial() {
      return basePart.particleMaterial();
    }

    @Override
    public int materialFlags() {
      return basePart.materialFlags();
    }

    private BlockStateModelPart bakeFluid(FluidStack stack, int increments) {
      FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(stack.getFluid().defaultFluidState());
      Material.Baked still = fluidModel.stillMaterial();
      Material.Baked flowing = fluidModel.flowingMaterial();
      CuboidModelElement element = original.fluid.getPart(increments, stack.getFluid().getFluidType().isLighterThanAir());
      Function<String, Material.Baked> materialGetter = name -> {
        String lookup = name.startsWith("#") ? name.substring(1) : name;
        if (lookup.equals("fluid")) {
          return still;
        }
        if (lookup.equals("flowing_fluid")) {
          return flowing;
        }
        Material material = model.getTopTextureSlots().getMaterial(lookup);
        return material == null ? basePart.particleMaterial() : baker.materials().get(material, model);
      };
      QuadCollection.Builder builder = new QuadCollection.Builder();
      UnbakedElementsHelper.bakeElements(baker, builder, List.of(element), materialGetter, state);
      return new SimpleModelWrapper(builder.build(), basePart.useAmbientOcclusion(), still);
    }

    private record FluidKey(FluidStack fluid, int increments) {}
  }

  private static final class CombinedPart implements BlockStateModelPart {
    private final BlockStateModelPart base;
    private final BlockStateModelPart fluid;

    private CombinedPart(BlockStateModelPart base, BlockStateModelPart fluid) {
      this.base = base;
      this.fluid = fluid;
    }

    @Override
    public List<BakedQuad> getQuads(Direction direction) {
      List<BakedQuad> baseQuads = base.getQuads(direction);
      List<BakedQuad> fluidQuads = fluid.getQuads(direction);
      if (baseQuads.isEmpty()) {
        return fluidQuads;
      }
      if (fluidQuads.isEmpty()) {
        return baseQuads;
      }
      List<BakedQuad> result = new ArrayList<>(baseQuads.size() + fluidQuads.size());
      result.addAll(baseQuads);
      result.addAll(fluidQuads);
      return result;
    }

    @Override
    public boolean useAmbientOcclusion() {
      return base.useAmbientOcclusion();
    }

    @Override
    public Material.Baked particleMaterial() {
      return base.particleMaterial();
    }

    @Override
    public int materialFlags() {
      return base.materialFlags() | fluid.materialFlags();
    }
  }
}
