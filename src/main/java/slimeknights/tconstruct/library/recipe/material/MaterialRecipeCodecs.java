package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.util.List;

/** Mojang codecs used by the 26.1 recipe serializers. */
final class MaterialRecipeCodecs {
  static final Codec<MaterialVariantId> MATERIAL = Codec.STRING.comapFlatMap(
    text -> {
      MaterialVariantId material = MaterialVariantId.tryParse(text);
      return material == null
        ? DataResult.error(() -> "Invalid material variant ID: " + text)
        : DataResult.success(material);
    },
    MaterialVariantId::toString
  );
  static final Codec<List<MaterialVariantId>> EXTRA_MATERIALS = MATERIAL.listOf();

  private MaterialRecipeCodecs() {}
}
