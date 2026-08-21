package slimeknights.tconstruct.library.recipe.material;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.RecipeOutput;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.ArrayList;
import java.util.List;

/** Special variant of material recipe output wrapping for shaped and shapeless material recipes. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MaterialsConsumerBuilder {
  private final String parts;
  private final int partCount;
  private final List<MaterialVariantId> materials = new ArrayList<>();

  public static MaterialsConsumerBuilder shaped(String parts) {
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("Parts may not be empty");
    }
    return new MaterialsConsumerBuilder(parts, 0);
  }

  public static MaterialsConsumerBuilder shapeless(int parts) {
    if (parts <= 0) {
      throw new IllegalArgumentException("Parts must be greater than 0");
    }
    return new MaterialsConsumerBuilder("", parts);
  }

  public MaterialsConsumerBuilder material(MaterialVariantId material) {
    materials.add(material);
    return this;
  }

  public RecipeOutput build(RecipeOutput consumer) {
    return MaterialRecipeWrapper.output(
      consumer,
      partCount > 0 ? TinkerTables.shapelessMaterialsRecipeSerializer.get() : TinkerTables.shapedMaterialsRecipeSerializer.get(),
      parts,
      partCount,
      materials
    );
  }
}