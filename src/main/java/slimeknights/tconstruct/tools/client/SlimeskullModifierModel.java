package slimeknights.tconstruct.tools.client;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.nbt.Tag;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo.TintedSprite;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.client.modifiers.model.ModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.SimpleModifierModel;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.TinkerModifiers;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Modifier model that tints the skull material. Will tint it using the dye color, or the material color if not dyed.
 */
public record SlimeskullModifierModel(Material small, int skullIndex, int slimeIndex) implements SimpleModifierModel {
  public static final RecordLoadable<SlimeskullModifierModel> LOADER = RecordLoadable.create(
    ModifierModel.MATERIAL_LOADABLE.requiredField("texture", SimpleModifierModel::small),
    IntLoadable.FROM_ZERO.requiredField("skull_index", SlimeskullModifierModel::skullIndex),
    IntLoadable.FROM_ZERO.requiredField("slime_index", SlimeskullModifierModel::slimeIndex),
    SlimeskullModifierModel::new);

  @Override
  public RecordLoadable<SlimeskullModifierModel> getLoader() {
    return LOADER;
  }

  @Nullable
  @Override
  public Material large() {
    return null;
  }

  private MaterialVariantId getSkullMaterial(IToolStackView tool) {
    return tool.getMaterial(skullIndex).getVariant();
  }

  private int getColor(IToolStackView tool) {
    IModDataView data = tool.getPersistentData();
    ModifierId dyed = TinkerModifiers.dyed.getId();
    if (data.contains(dyed.getId(), Tag.TAG_INT)) {
      return 0xFF000000 | data.getInt(dyed.getId());
    }
    return SlimeskullArmorModel.MATERIAL_COLOR_CACHE.apply(tool.getMaterial(slimeIndex).getVariant());
  }

  @Nullable
  @Override
  public Object getCacheKey(IToolStackView tool, ModifierEntry modifier) {
    IModDataView data = tool.getPersistentData();
    ModifierId dyed = TinkerModifiers.dyed.getId();
    if (data.contains(dyed.getId(), Tag.TAG_INT)) {
      return data.getInt(dyed.getId());
    }
    return getSkullMaterial(tool).toString();
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material, TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    if (small == null) {
      return;
    }
    TintedSprite sprite = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(getSkullMaterial(tool))
      .map(info -> info.getSprite(small, spriteGetter))
      .orElseGet(() -> new TintedSprite(spriteGetter.apply(small), -1, 0));
    quadConsumer.accept(MantleItemLayerModel.getQuadsForSprite(getColor(tool), -1, sprite.sprite(), transforms, sprite.emissivity(), pixels));
  }
}
