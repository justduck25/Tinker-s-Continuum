package slimeknights.tconstruct.library.materials.definition;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.utils.IdParser;
import slimeknights.tconstruct.library.utils.ResourceId;

import javax.annotation.Nullable;

/**
 * This is just a copy of Identifier for type safety in material JSON.
 */
public final class MaterialId extends ResourceId implements MaterialVariantId {
  public static final IdParser<MaterialId> PARSER = new IdParser<>(MaterialId::new, "Material");

  public MaterialId(String resourceName) {
    super(Identifier.parse(resourceName));
  }

  public MaterialId(String namespaceIn, String pathIn) {
    super(Identifier.fromNamespaceAndPath(namespaceIn, pathIn));
  }

  public MaterialId(Identifier location) {
    super(location);
  }

  /** Checks if this ID matches the given material */
  public boolean matches(IMaterial material) {
    return this.equals(material.getIdentifier());
  }

  /** Checks if this ID matches the given stack */
  public boolean matches(ItemStack stack) {
    return !stack.isEmpty() && this.equals(IMaterialItem.getMaterialFromStack(stack));
  }

  @Override
  public MaterialId getMaterialId() {
    return this;
  }

  @Override
  public String getVariant() {
    return "";
  }

  @Override
  public boolean hasVariant() {
    return false;
  }

  @Override
  public Identifier getLocation(char separator) {
    return getId();
  }

  @Override
  public String getSuffix() {
    return getNamespace() + '_' + getPath();
  }

  @Override
  public boolean matchesVariant(MaterialVariantId other) {
    return this.equals(other.getMaterialId());
  }


  /* Helpers */

  /** {@return Material ID, or null if invalid} */
  @Nullable
  public static MaterialId tryParse(String string) {
    return tryParse(string, (namespace, path) -> new MaterialId(namespace, path));
  }

  /** {@return Material ID, or null if invalid} */
  @Nullable
  public static MaterialId tryBuild(String namespace, String path) {
    return tryBuild(namespace, path, MaterialId::new);
  }
}
