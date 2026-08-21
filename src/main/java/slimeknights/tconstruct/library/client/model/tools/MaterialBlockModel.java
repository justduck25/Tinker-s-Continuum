package slimeknights.tconstruct.library.client.model.tools;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.part.block.MaterialBlockEntity;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dynamic full-cube model for blocks whose texture is supplied by a material block entity.
 */
public final class MaterialBlockModel implements DynamicBlockStateModel {
  public static final Identifier ID = TConstruct.getResource("material_block");
  private static final Material BASE_MATERIAL = new Material(TConstruct.getResource("block/storage/fallback"));

  private final BlockStateModelPart fallbackPart;
  private final Material.Baked particleMaterial;

  private MaterialBlockModel(BlockStateModelPart fallbackPart, Material.Baked particleMaterial) {
    this.fallbackPart = fallbackPart;
    this.particleMaterial = particleMaterial;
  }

  @Override
  public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
    parts.add(fallbackPart);
  }

  @Override
  public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                           List<BlockStateModelPart> parts) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (!(blockEntity instanceof MaterialBlockEntity materialBlockEntity)) {
      parts.add(fallbackPart);
      return;
    }
    MaterialVariantId material = materialBlockEntity.getMaterial();
    if (material == null) {
      parts.add(fallbackPart);
      return;
    }
    Optional<MaterialRenderInfo> renderInfo = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(material);
    if (renderInfo.isEmpty()) {
      parts.add(fallbackPart);
      return;
    }
    TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
    MaterialRenderInfo.TintedSprite tinted = renderInfo.get().getSprite(BASE_MATERIAL, mat -> atlas.getSprite(mat.sprite()));
    parts.add(new CubePart(new Material.Baked(tinted.sprite(), false), tinted.color(), tinted.emissivity()));
  }

  @Override
  public Material.Baked particleMaterial() {
    return particleMaterial;
  }

  @Override
  public int materialFlags() {
    return 0;
  }

  private static final class CubePart implements BlockStateModelPart {
    private final Map<Direction, List<BakedQuad>> quads = new EnumMap<>(Direction.class);
    private final Material.Baked particleMaterial;

    private CubePart(Material.Baked sprite, int color, int emission) {
      this.particleMaterial = sprite;
      for (Direction direction : Direction.values()) {
        quads.put(direction, List.of(createQuad(direction, sprite, color, emission)));
      }
    }

    @Override
    public List<BakedQuad> getQuads(Direction direction) {
      return quads.getOrDefault(direction, List.of());
    }

    @Override
    public boolean useAmbientOcclusion() {
      return true;
    }

    @Override
    public Material.Baked particleMaterial() {
      return particleMaterial;
    }

    @Override
    public int materialFlags() {
      return 0;
    }

    private static BakedQuad createQuad(Direction direction, Material.Baked sprite, int color, int emission) {
      QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer();
      builder.setDirection(direction);
      builder.setSprite(sprite);
      builder.setShade(true);
      builder.setAmbientOcclusion(true);
      builder.setTintIndex(-1);
      builder.setLightEmission(emission);
      int argb = color == -1 ? 0xFFFFFFFF : color;
      float[][] vertices = vertices(direction);
      for (int i = 0; i < 4; i++) {
        float[] vertex = vertices[i];
        float u = i == 0 || i == 3 ? 0 : 1;
        float v = i == 0 || i == 1 ? 0 : 1;
        builder.addVertex(vertex[0], vertex[1], vertex[2])
          .setColor(argb)
          .setUv(sprite.sprite().getU(u), sprite.sprite().getV(v))
          .setNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
      }
      return builder.bakeQuad();
    }

    private static float[][] vertices(Direction direction) {
      return switch (direction) {
        case DOWN -> new float[][] {{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}};
        case UP -> new float[][] {{0, 1, 1}, {1, 1, 1}, {1, 1, 0}, {0, 1, 0}};
        case NORTH -> new float[][] {{1, 0, 0}, {0, 0, 0}, {0, 1, 0}, {1, 1, 0}};
        case SOUTH -> new float[][] {{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}};
        case WEST -> new float[][] {{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}};
        case EAST -> new float[][] {{1, 0, 1}, {1, 0, 0}, {1, 1, 0}, {1, 1, 1}};
      };
    }
  }

  public static final class Unbaked implements CustomUnbakedBlockStateModel {
    public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

    @Override
    public BlockStateModel bake(ModelBaker baker) {
      Material.Baked fallback = baker.materials().get(BASE_MATERIAL, (ModelDebugName) () -> "tconstruct:material_block");
      return new MaterialBlockModel(new CubePart(fallback, -1, 0), fallback);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {}

    @Override
    public MapCodec<Unbaked> codec() {
      return MAP_CODEC;
    }
  }
}