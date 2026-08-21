package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Helper for use with our wrappers of resource location for some type safety in IDs.
 * @see IdParser
 */
public abstract class ResourceId {
  private final Identifier id;

  protected ResourceId(Identifier id) {
    this.id = id;
  }

  public Identifier getId() {
    return id;
  }

  public String getNamespace() {
    return id.getNamespace();
  }

  public String getPath() {
    return id.getPath();
  }

  /** Gets this ID with a prefix added to the path. */
  public Identifier withPrefix(String prefix) {
    return id.withPrefix(prefix);
  }

  /** Gets this ID with a suffix added to the path. */
  public Identifier withSuffix(String suffix) {
    return id.withSuffix(suffix);
  }

  @Override
  public String toString() {
    return id.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResourceId that = (ResourceId) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  /* Helpers for static constructors */

  @Nullable
  protected static <T extends ResourceId> T tryParse(String string, BiFunction<String,String,T> constructor) {
    String[] parts = decompose(string, ':');
    return tryBuild(parts[0], parts[1], constructor);
  }

  @Nullable
  protected static <T extends ResourceId> T tryBuild(String namespace, String path, BiFunction<String,String,T> constructor) {
    if (Identifier.isValidNamespace(namespace) && Identifier.isValidPath(path)) {
      return constructor.apply(namespace, path);
    }
    return null;
  }

  /** Phantom type to disambiguate constructors */
  protected static final class Dummy {
    private Dummy() {}
  }

  /** Decomposes a string into parts separated by a character */
  private static String[] decompose(String string, char separator) {
    int index = string.indexOf(separator);
    if (index == -1) {
      return new String[]{Identifier.DEFAULT_NAMESPACE, string};
    }
    return new String[]{string.substring(0, index), string.substring(index + 1)};
  }
}
