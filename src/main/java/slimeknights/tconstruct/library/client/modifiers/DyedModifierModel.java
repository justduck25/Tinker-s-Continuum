package slimeknights.tconstruct.library.client.modifiers;

import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.nbt.Tag;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.tconstruct.library.client.modifiers.model.SimpleModifierModel;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Modifier model that copies dye from persistent modifier data.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class DyedModifierModel implements SimpleModifierModel {
  public static final RecordLoadable<DyedModifierModel> LOADER = SimpleModifierModel.loader(DyedModifierModel::new);
  /** @deprecated legacy system, use {@link #LOADER} */
  @Deprecated
  public static final IUnbakedModifierModel UNBAKED_INSTANCE = (smallGetter, largeGetter) -> {
    Material smallTexture = smallGetter.apply("");
    Material largeTexture = largeGetter.apply("");
    if (smallTexture != null || largeTexture != null) {
      return new DyedModifierModel(smallTexture, largeTexture);
    }
    return null;
  };

  /** Textures to show */
  @Nullable
  private final Material small;
  @Nullable
  private final Material large;

  @Override
  public RecordLoadable<? extends DyedModifierModel> getLoader() {
    return LOADER;
  }

  @Nullable
  @Override
  public Object getCacheKey(IToolStackView tool, ModifierEntry entry) {
    ModifierId modifier = entry.getId();
    IModDataView data = tool.getPersistentData();
    int color = -1;
    if (data.contains(modifier.getId(), Tag.TAG_INT)) {
      color = data.getInt(modifier.getId());
    }
    return new CacheKey(modifier, color);
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    Material texture = isLarge ? large : small;
    if (texture != null) {
      IModDataView data = tool.getPersistentData();
      ModifierId key = modifier.getId();
      if (data.contains(key.getId(), Tag.TAG_INT)) {
        quadConsumer.accept(MantleItemLayerModel.getQuadsForSprite(0xFF000000 | data.getInt(key.getId()), -1, spriteGetter.apply(texture), transforms, 0, pixels));
      }
    }
  }

  /** Data class to cache a colored texture */
  private record CacheKey(ModifierId modifier, int color) {}
}