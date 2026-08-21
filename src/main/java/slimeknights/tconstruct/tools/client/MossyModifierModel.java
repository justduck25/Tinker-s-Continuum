package slimeknights.tconstruct.tools.client;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.tconstruct.library.client.modifiers.model.ModifierModel;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/** Mossy overlay model that swaps textures based on mossy level. */
public record MossyModifierModel(
  Material texture1,
  Material texture2,
  Material texture3,
  @Nullable Material textureLarge1,
  @Nullable Material textureLarge2,
  @Nullable Material textureLarge3
) implements ModifierModel {
  public static final RecordLoadable<MossyModifierModel> LOADER = RecordLoadable.create(
    ModifierModel.MATERIAL_LOADABLE.requiredField("texture_1", MossyModifierModel::texture1),
    ModifierModel.MATERIAL_LOADABLE.requiredField("texture_2", MossyModifierModel::texture2),
    ModifierModel.MATERIAL_LOADABLE.requiredField("texture_3", MossyModifierModel::texture3),
    ModifierModel.MATERIAL_LOADABLE.nullableField("texture_large_1", MossyModifierModel::textureLarge1),
    ModifierModel.MATERIAL_LOADABLE.nullableField("texture_large_2", MossyModifierModel::textureLarge2),
    ModifierModel.MATERIAL_LOADABLE.nullableField("texture_large_3", MossyModifierModel::textureLarge3),
    MossyModifierModel::new);

  @Override
  public RecordLoadable<? extends ModifierModel> getLoader() {
    return LOADER;
  }

  @Override
  public void validate(Function<Material, TextureAtlasSprite> spriteGetter) {
    spriteGetter.apply(texture1);
    spriteGetter.apply(texture2);
    spriteGetter.apply(texture3);
    if (textureLarge1 != null) spriteGetter.apply(textureLarge1);
    if (textureLarge2 != null) spriteGetter.apply(textureLarge2);
    if (textureLarge3 != null) spriteGetter.apply(textureLarge3);
  }

  @Nullable
  @Override
  public Object getCacheKey(IToolStackView tool, ModifierEntry modifier) {
    return modifier.getLevel();
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material, TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    Material texture = textureForLevel(modifier.getLevel(), isLarge);
    quadConsumer.accept(MantleItemLayerModel.getQuadsForSprite(-1, -1, spriteGetter.apply(texture), transforms, 0, pixels));
  }

  private Material textureForLevel(int level, boolean large) {
    return switch (Math.clamp(level, 1, 3)) {
      case 3 -> large && textureLarge3 != null ? textureLarge3 : texture3;
      case 2 -> large && textureLarge2 != null ? textureLarge2 : texture2;
      default -> large && textureLarge1 != null ? textureLarge1 : texture1;
    };
  }
}
