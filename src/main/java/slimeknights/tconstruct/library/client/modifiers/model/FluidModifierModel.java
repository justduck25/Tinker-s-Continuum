package slimeknights.tconstruct.library.client.modifiers.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.capability.fluid.ToolTankHelper;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/** Model for a fluid overlay in a tool. */
public record FluidModifierModel(Material small, @Nullable Material large, ToolTankHelper tankHelper) implements SimpleModifierModel {
  public static final RecordLoadable<FluidModifierModel> LOADER = RecordLoadable.create(
    ModifierModel.MATERIAL_LOADABLE.requiredField("mask", FluidModifierModel::small),
    ModifierModel.MATERIAL_LOADABLE.nullableField("mask_large", FluidModifierModel::large),
    ToolTankHelper.LOADABLE.defaultField("tank_helper", ToolTankHelper.TANK_HELPER, false, FluidModifierModel::tankHelper),
    FluidModifierModel::new);

  /** Instance with default tank helper. */
  public FluidModifierModel(Material small, @Nullable Material large) {
    this(small, large, ToolTankHelper.TANK_HELPER);
  }

  private record CacheKey(Fluid fluid, DataComponentPatch components) {}

  @Nullable
  @Override
  public Object getCacheKey(IToolStackView tool, ModifierEntry modifier) {
    FluidStack fluid = tankHelper().getFluid(tool);
    if (!fluid.isEmpty()) {
      return new CacheKey(fluid.getFluid(), fluid.getComponentsPatch());
    }
    return null;
  }

  @Override
  public RecordLoadable<FluidModifierModel> getLoader() {
    return LOADER;
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material, TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    Material template = isLarge ? large() : small();
    if (template != null) {
      FluidStack fluid = tankHelper().getFluid(tool);
      if (!fluid.isEmpty()) {
        addQuads(fluid, template, spriteGetter, transforms, quadConsumer, pixels);
      }
    }
  }

  /** Adds quads for the given fluid using the supplied mask texture. */
  public static void addQuads(FluidStack fluid, Material template, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, Consumer<Collection<BakedQuad>> quadConsumer) {
    addQuads(fluid, template, spriteGetter, transforms, quadConsumer, null);
  }

  /** Adds quads for the given fluid using the supplied mask texture. */
  public static void addQuads(FluidStack fluid, Material template, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    TextureAtlasSprite mask = spriteGetter.apply(template);
    int color = RenderUtils.getFluidColor(fluid, fluid.getFluid().getFluidType());
    if (color == -1) {
      color = 0xFFFFFFFF;
    } else if ((color & 0xFF000000) == 0) {
      color |= 0xFF000000;
    }
    int luminosity = fluid.getFluid().getFluidType().getLightLevel(fluid);
    quadConsumer.accept(MantleItemLayerModel.getQuadsForSprite(color, -1, mask, transforms, luminosity, pixels));
  }
}
