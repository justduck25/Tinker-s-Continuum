package slimeknights.tconstruct.library.recipe.material;

import lombok.NoArgsConstructor;
import net.minecraft.data.recipes.RecipeOutput;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.ArrayList;
import java.util.List;

/** Special variant of material recipe output wrapping for the deprecated shaped material serializer. */
@Deprecated
@NoArgsConstructor(staticName = "wrap")
public class ShapedMaterialConsumerBuilder {
  private final List<MaterialVariantId> materials = new ArrayList<>();

  public ShapedMaterialConsumerBuilder material(MaterialVariantId material) {
    materials.add(material);
    return this;
  }

  public RecipeOutput build(RecipeOutput consumer) {
    return MaterialRecipeWrapper.output(consumer, TinkerTables.shapedMaterialRecipeSerializer.get(), "", 0, materials);
  }
}