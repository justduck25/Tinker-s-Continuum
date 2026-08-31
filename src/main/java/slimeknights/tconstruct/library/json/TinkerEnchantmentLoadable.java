package slimeknights.tconstruct.library.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import slimeknights.mantle.data.loadable.common.LazyRegistryLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Loadable for the dynamic enchantment registry, including datagen HolderLookup support. */
public class TinkerEnchantmentLoadable implements StringLoadable<Enchantment> {
  public static final TinkerEnchantmentLoadable INSTANCE = new TinkerEnchantmentLoadable();

  private static final Map<Enchantment, ResourceKey<Enchantment>> KEY_CACHE = new ConcurrentHashMap<>();
  private static Supplier<HolderLookup.Provider> LOOKUP_SUPPLIER = () -> null;

  private final LazyRegistryLoadable<Enchantment> delegate = new LazyRegistryLoadable<>(Registries.ENCHANTMENT);

  public static HolderLookup.Provider setLookupProvider(HolderLookup.Provider provider) {
    HolderLookup.Provider previous = LOOKUP_SUPPLIER.get();
    LOOKUP_SUPPLIER = () -> provider;
    return previous;
  }

  public static void clear() {
    LOOKUP_SUPPLIER = () -> null;
    KEY_CACHE.clear();
  }

  public static void registerKey(Enchantment enchantment, ResourceKey<Enchantment> key) {
    KEY_CACHE.put(enchantment, key);
  }


  private static HolderLookup.Provider lookupProvider() {
    HolderLookup.Provider provider = LOOKUP_SUPPLIER.get();
    if (provider != null) {
      return provider;
    }
    var server = ServerLifecycleHooks.getCurrentServer();
    return server == null ? null : server.registryAccess();
  }
  @Override
  public Enchantment convert(JsonElement element, String key, TypedMap context) {
    return parseString(JsonHelper.convertToIdentifier(element, key).toString(), key, context);
  }

  @Override
  public Enchantment parseString(String value, String key, TypedMap context) {
    Identifier id = JsonHelper.parseIdentifier(value, key);
    HolderLookup.Provider provider = lookupProvider();
    if (provider != null) {
      ResourceKey<Enchantment> resourceKey = ResourceKey.create(Registries.ENCHANTMENT, id);
      Enchantment enchantment = provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(resourceKey).value();
      KEY_CACHE.put(enchantment, resourceKey);
      return enchantment;
    }
    return delegate.parseString(value, key, context);
  }

  @Override
  public JsonElement serialize(Enchantment enchantment) {
    return new JsonPrimitive(getString(enchantment));
  }

  @Override
  public Enchantment decode(FriendlyByteBuf buf, TypedMap context) {
    return parseString(buf.readIdentifier().toString(), "enchantment", context);
  }

  @Override
  public void encode(FriendlyByteBuf buf, Enchantment value) {
    buf.writeIdentifier(Identifier.parse(getString(value)));
  }

  @Override
  public String getString(Enchantment enchantment) {
    ResourceKey<Enchantment> resourceKey = KEY_CACHE.get(enchantment);
    if (resourceKey != null) {
      return resourceKey.identifier().toString();
    }
    HolderLookup.Provider provider = lookupProvider();
    if (provider != null) {
      return provider.lookupOrThrow(Registries.ENCHANTMENT).listElements()
        .filter(holder -> holder.value() == enchantment)
        .findFirst()
        .map(holder -> {
          KEY_CACHE.put(enchantment, holder.key());
          return holder.key().identifier().toString();
        })
        .orElseThrow(() -> new IllegalArgumentException("Unknown enchantment " + enchantment));
    }
    if (delegate.registry() != null) {
      Identifier id = delegate.registry().getKey(enchantment);
      if (id != null) {
        return id.toString();
      }
    }
    throw new IllegalArgumentException("Unknown enchantment " + enchantment);
  }
}
