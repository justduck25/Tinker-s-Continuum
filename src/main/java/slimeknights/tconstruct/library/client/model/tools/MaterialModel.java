package slimeknights.tconstruct.library.client.model.tools;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.util.List;
import java.util.function.Function;

public class MaterialModel {
  public static List<BakedQuad> getQuadsForMaterial(Function<Material,TextureAtlasSprite> spriteGetter, Material texture, MaterialVariantId material, int tint, Transformation transforms, ItemLayerPixels pixels) {
    MaterialRenderInfo.TintedSprite sprite = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(material)
      .map(info -> info.getSprite(texture, spriteGetter))
      .orElseGet(() -> new MaterialRenderInfo.TintedSprite(spriteGetter.apply(texture), tint, 0));
    return MantleItemLayerModel.getQuadsForSprite(sprite.color(), -1, sprite.sprite(), transforms, sprite.emissivity(), pixels);
  }
}
