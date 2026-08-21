package slimeknights.tconstruct.fluids.fluids;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.fluids.TinkerFluids;

import java.util.List;
import java.util.Optional;
import java.util.Objects;

public class PotionFluidType extends FluidType {
  public PotionFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public String getDescriptionId(FluidStack stack) {
    PotionContents contents = getPotionContents(stack);
    return contents.getName("item.minecraft.potion.effect.").getString();
  }

  @Override
  public ItemStack getBucket(FluidStack fluidStack) {
    ItemStack itemStack = new ItemStack(fluidStack.getFluid().getBucket());
    PotionContents contents = getPotionContents(fluidStack);
    itemStack.set(DataComponents.POTION_CONTENTS, contents);
    return itemStack;
  }

  // TODO: 1.21.2 - register via IClientFluidTypeExtensions and RegisterClientExtensionsEvent
  /*@Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(new ClientTextureFluidType(this) {
      @Override
      public int getTintColor(FluidStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        Optional<Integer> customColor = contents.customColor();
        if (customColor.isPresent()) {
          return customColor.get() | 0xFF000000;
        }
        if (contents.potion().isEmpty() && contents.customEffects().isEmpty()) {
          return getTintColor();
        }
        return contents.getColor() | 0xFF000000;
      }
    });
  }*/

  /** Creates a potion fluid stack even during datagen before fluid components are bound. */
  private static FluidStack newPotionStack(int size) {
    Holder.Reference<net.minecraft.world.level.material.Fluid> holder = TinkerFluids.potion.get().builtInRegistryHolder();
    return holder.areComponentsBound() ? new FluidStack(holder, size) : new FluidStack(Holder.direct(TinkerFluids.potion.get(), DataComponentMap.EMPTY), size);
  }

  /** Creates legacy potion NBT for recipe JSON compatibility. */
  private static CompoundTag legacyPotionTag(Identifier potion) {
    CompoundTag tag = new CompoundTag();
    tag.putString("Potion", potion.toString());
    return tag;
  }

  /** Gets legacy potion NBT from a fluid stack. */
  public static CompoundTag getLegacyPotionTag(FluidStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data != null) {
      CompoundTag tag = data.copyTag();
      if (tag.contains("Potion")) {
        return tag;
      }
    }
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    String potion = contents == null ? "" : contents.potion().flatMap(Holder::unwrapKey).map(key -> key.identifier().toString()).orElse("");
    return potion.isEmpty() ? new CompoundTag() : legacyPotionTag(Identifier.parse(potion));
  }

  /** Reads potion contents from modern components, falling back to legacy NBT used by generated recipe JSON. */
  public static PotionContents getPotionContents(FluidStack stack) {
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    if (contents != null) {
      return contents;
    }
    CompoundTag tag = getLegacyPotionTag(stack);
    String potion = tag.getString("Potion").orElse("");
    if (!potion.isEmpty()) {
      return BuiltInRegistries.POTION.get(Identifier.parse(potion)).map(PotionContents::new).orElse(PotionContents.EMPTY);
    }
    return PotionContents.EMPTY;
  }
  /** Creates a fluid stack for the given potion */
  public static FluidStack potionFluid(ResourceKey<Potion> potion, int size) {
    FluidStack stack = newPotionStack(size);
    setPotionComponent(stack, potion);
    return stack;
  }

  public static FluidStack potionFluid(Potion potion, int size) {
    FluidStack stack = newPotionStack(size);
    BuiltInRegistries.POTION.getResourceKey(potion).ifPresent(key -> setPotionComponent(stack, key));
    return stack;
  }

  private static void setPotionComponent(FluidStack stack, ResourceKey<Potion> key) {
    BuiltInRegistries.POTION.get(key).ifPresent(holder -> {
      PotionContents contents = new PotionContents(Optional.of(holder), Optional.empty(), List.of(), Optional.empty());
      stack.set(DataComponents.POTION_CONTENTS, contents);
    });
  }

  private static void setPotionComponent(ItemStack stack, ResourceKey<Potion> key) {
    BuiltInRegistries.POTION.get(key).ifPresent(holder -> {
      PotionContents contents = new PotionContents(Optional.of(holder), Optional.empty(), List.of(), Optional.empty());
      stack.set(DataComponents.POTION_CONTENTS, contents);
    });
  }

  /** Creates a fluid output for the given potion */
  public static FluidOutput potionResult(Holder<Potion> potion, int size) {
    return potion.unwrapKey().map(key -> FluidOutput.fromTag(Objects.requireNonNull(TinkerFluids.potion.getCommonTag()), size, legacyPotionTag(key.identifier()))).orElseGet(() -> FluidOutput.fromFluid(TinkerFluids.potion.get(), size));
  }

  /** Creates a fluid output for the given potion */
  public static FluidOutput potionResult(ResourceKey<Potion> potion, int size) {
    return FluidOutput.fromTag(Objects.requireNonNull(TinkerFluids.potion.getCommonTag()), size, legacyPotionTag(potion.identifier()));
  }

  /** Creates a fluid output for the given potion */
  public static FluidOutput potionResult(Potion potion, int size) {
    return BuiltInRegistries.POTION.getResourceKey(potion).map(key -> potionResult(key, size)).orElseGet(() -> FluidOutput.fromFluid(TinkerFluids.potion.get(), size));
  }

  /** Creates a potion bucket for the given potion */
  public static ItemStack potionBucket(ResourceKey<Potion> potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion.getBucket());
    setPotionComponent(stack, potion);
    return stack;
  }

  /** Creates a potion bucket for the given potion */
  public static ItemStack potionBucket(Potion potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion.getBucket());
    BuiltInRegistries.POTION.getResourceKey(potion).ifPresent(key -> {
      setPotionComponent(stack, key);
    });
    return stack;
  }
}
