package slimeknights.tconstruct.plugin.jsonthings;

import dev.gigaherz.jsonthings.things.IFlexBlock;
import dev.gigaherz.jsonthings.things.serializers.FlexBlockType;
import dev.gigaherz.jsonthings.things.serializers.IBlockSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.common.util.Lazy;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.plugin.jsonthings.block.FlexBurningLiquidBlock;
import slimeknights.tconstruct.plugin.jsonthings.block.FlexMobEffectLiquidBlock;

import java.util.Objects;
import java.util.function.Supplier;

/** Collection of custom block types added by Tinkers. */
public class FlexBlockTypes {
  /** Creates the supplier for a fluid in a fluid block. */
  private static FlowingFluid fluid(Identifier name) {
    if (Loadables.FLUID.fromKey(name, "fluid") instanceof FlowingFluid flowing) {
      return flowing;
    }
    throw new RuntimeException("LiquidBlock requires a flowing fluid");
  }

  /** Initializes the block types. */
  public static void init() {
    register("burning_liquid", data -> {
      Identifier fluidField = Loadables.RESOURCE_LOCATION.getOrDefault(data, "fluid", null);
      int burnTime = GsonHelper.getAsInt(data, "burn_time");
      float damage = GsonHelper.getAsFloat(data, "damage");
      return (props, builder) -> new FlexBurningLiquidBlock(props, builder,
        fluid(Objects.requireNonNullElse(fluidField, builder.getRegistryName())), burnTime, damage);
    });
    register("mob_effect_liquid", data -> {
      Identifier fluidField = Loadables.RESOURCE_LOCATION.getOrDefault(data, "fluid", null);
      Identifier effectName = Loadables.RESOURCE_LOCATION.getIfPresent(data, "effect");
      int effectLevel = GsonHelper.getAsInt(data, "burn_time");
      return (props, builder) -> {
        Lazy<MobEffect> effect = Lazy.of(() -> Loadables.MOB_EFFECT.fromKey(effectName, "effect"));
        return new FlexMobEffectLiquidBlock(props, builder,
          fluid(Objects.requireNonNullElse(fluidField, builder.getRegistryName())),
          () -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get()), 5 * 20, effectLevel - 1));
      };
    });
  }

  /** Local helper to register our stuff. */
  private static <T extends Block & IFlexBlock> void register(String name, IBlockSerializer<T> factory) {
    FlexBlockType.register(TConstruct.resourceString(name), factory,
      FlexBlockType.DefaultTypeProperties.builder()
        .defaultSeeThrough(true)
        .defaultIgnitedByLava(false)
        .defaultReplaceable(true));
  }
}
