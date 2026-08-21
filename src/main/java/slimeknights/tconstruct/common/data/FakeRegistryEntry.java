package slimeknights.tconstruct.common.data;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import slimeknights.mantle.data.loadable.common.BaseRegistryLoadable;
import slimeknights.tconstruct.common.TinkerEffect;

import java.util.Objects;
import java.util.function.Supplier;

public class FakeRegistryEntry {
  @SuppressWarnings("UnstableApiUsage")
  private static <T> T getOrCreate(Registry<T> registry, Identifier id, Supplier<T> constructor) {
    if (!registry.containsKey(id)) {
      T value = constructor.get();
      try {
        Registry.register(registry, id, value);
      } catch (IllegalStateException e) {
        // registry frozen during datagen, register custom key mapping
        BaseRegistryLoadable.registerKey(value, id);
      }
      return value;
    }
    return Objects.requireNonNull(registry.getValue(id));
  }

  public static Block block(Identifier id) {
    try {
      return getOrCreate(BuiltInRegistries.BLOCK, id, () -> new Block(BlockBehaviour.Properties.of()));
    } catch (Exception e) {
      return Blocks.AIR;
    }
  }

  public static Item item(Identifier id) {
    return getOrCreate(BuiltInRegistries.ITEM, id, () -> new Item(new Item.Properties()));
  }

  public static MobEffect effect(Identifier id) {
    return getOrCreate(BuiltInRegistries.MOB_EFFECT, id, () -> new TinkerEffect(MobEffectCategory.NEUTRAL, false));
  }

  public static <T extends Entity> EntityType<?> entity(Identifier id) {
    return getOrCreate(BuiltInRegistries.ENTITY_TYPE, id, () ->
      EntityType.Builder.of((type, level) -> {
        throw new UnsupportedOperationException("Cannot create instance of fake entity");
      }, MobCategory.MISC).build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
  }
}
