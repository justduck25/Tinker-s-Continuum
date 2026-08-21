package slimeknights.tconstruct.smeltery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/** Fluid container holding 1 ingot of fluid. */
public class CopperCanItem extends Item {
  private static final String TAG_FLUID = "fluid";
  private static final String TAG_FLUID_TAG = "fluid_tag";

  public CopperCanItem(Properties properties) {
    super(slimeknights.mantle.registration.deferred.ItemDeferredRegister.setIdFromCurrentKey(properties));
  }

  public boolean hasCraftingRemainingItem(ItemStack stack) {
    return getFluid(stack) != Fluids.EMPTY;
  }

  public ItemStack getCraftingRemainingItem(ItemStack stack) {
    return hasCraftingRemainingItem(stack) ? new ItemStack(this) : ItemStack.EMPTY;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    Fluid fluid = getFluid(stack);
    if (fluid != Fluids.EMPTY) {
      CompoundTag fluidTag = getFluidTag(stack);
      MutableComponent text;
      if (fluidTag != null) {
        FluidStack displayFluid = new FluidStack(fluid, FluidValues.INGOT);
        displayFluid.set(DataComponents.CUSTOM_DATA, CustomData.of(fluidTag));
        text = Component.translatable(displayFluid.getFluid().getFluidType().getDescriptionId(displayFluid));
      } else {
        text = Component.translatable(fluid.getFluidType().getDescriptionId());
      }
      tooltip.accept(Component.translatable(this.getDescriptionId() + ".contents", text).withStyle(ChatFormatting.GRAY));
      if (flag.isAdvanced()) {
        tooltip.accept(Component.translatable(TankItem.FLUID_ID, Loadables.FLUID.getKey(fluid).toString()).withStyle(ChatFormatting.DARK_GRAY));
      }
    } else {
      tooltip.accept(Component.translatable(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
  }

  private static CompoundTag getRoot(ItemStack stack) {
    return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
  }

  private static void setRoot(ItemStack stack, CompoundTag tag) {
    if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
    else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  public static void removeFluid(ItemStack stack) {
    CompoundTag nbt = getRoot(stack);
    nbt.remove(TAG_FLUID);
    nbt.remove(TAG_FLUID_TAG);
    setRoot(stack, nbt);
  }

  private static void setFluidInternal(ItemStack stack, Identifier fluid, @Nullable CompoundTag fluidTag) {
    CompoundTag nbt = getRoot(stack);
    nbt.putString(TAG_FLUID, fluid.toString());
    if (fluidTag != null) nbt.put(TAG_FLUID_TAG, fluidTag.copy());
    else nbt.remove(TAG_FLUID_TAG);
    setRoot(stack, nbt);
  }

  @SuppressWarnings("deprecation")
  public static ItemStack setFluid(ItemStack stack, Identifier fluid, @Nullable CompoundTag fluidTag) {
    if (fluid.equals(BuiltInRegistries.FLUID.getDefaultKey())) removeFluid(stack);
    else setFluidInternal(stack, fluid, fluidTag);
    return stack;
  }

  public static ItemStack setFluid(ItemStack stack, Fluid fluid, @Nullable CompoundTag fluidTag) {
    if (fluid == Fluids.EMPTY) removeFluid(stack);
    else setFluidInternal(stack, BuiltInRegistries.FLUID.getKey(fluid), fluidTag);
    return stack;
  }

  public static ItemStack setFluid(ItemStack stack, FluidStack fluid) {
    CustomData data = fluid.get(DataComponents.CUSTOM_DATA);
    return setFluid(stack, fluid.getFluid(), data == null ? null : data.copyTag());
  }

  public static Fluid getFluid(ItemStack stack) {
    CompoundTag nbt = getRoot(stack);
    Identifier location = Identifier.tryParse(nbt.getString(TAG_FLUID).orElse(""));
    if (location != null) {
      Fluid fluid = BuiltInRegistries.FLUID.getValue(location);
      if (fluid != null) return fluid;
    }
    return Fluids.EMPTY;
  }

  @SuppressWarnings("deprecation")
  public static void addFilledVariants(Consumer<ItemStack> output) {
    BuiltInRegistries.FLUID.listElements().filter(holder -> {
      Fluid fluid = holder.value();
      return fluid.isSource(fluid.defaultFluidState()) && !holder.is(TinkerTags.Fluids.HIDE_IN_CREATIVE_TANKS);
    }).forEachOrdered(holder -> output.accept(CopperCanItem.setFluid(new ItemStack(TinkerSmeltery.copperCan), holder.key().identifier(), null)));
  }

  @Nullable
  public static CompoundTag getFluidTag(ItemStack stack) {
    return getRoot(stack).getCompound(TAG_FLUID_TAG).orElse(null);
  }

  public static String getSubtype(ItemStack stack) {
    return getRoot(stack).getString(TAG_FLUID).orElse("");
  }
}
