package slimeknights.tconstruct.fluids.item;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.transfer.EmptyFluidContainerTransfer;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.TConstruct;

/**
 * Fluid transfer info that empties a fluid from an item, copying the fluid's NBT to the stack
 * @deprecated use {@link slimeknights.mantle.fluid.transfer.EmptyPotionTransfer}
 */
@Deprecated(forRemoval = true)
public class EmptyPotionTransfer extends EmptyFluidContainerTransfer {
  public static final Identifier ID = TConstruct.getResource("empty_potion");
  public EmptyPotionTransfer(Ingredient input, ItemOutput filled, FluidOutput fluid) {
    super(input, filled, fluid);
  }

  @Override
  public boolean matches(ItemStack stack, FluidStack contained) {
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    return super.matches(stack, contained)
      && (TagPreference.getPreference(MantleTags.Fluids.POTION).isPresent() || contents.is(Potions.WATER));
  }

  @Override
  protected FluidStack getFluid(ItemStack stack) {
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    if (contents.is(Potions.WATER)) {
      return new FluidStack(Fluids.WATER, fluid.getAmount());
    }
    return TagPreference.getPreference(MantleTags.Fluids.POTION)
      .map(value -> {
        FluidStack result = new FluidStack(value, fluid.getAmount());
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
          result.set(DataComponents.CUSTOM_DATA, CustomData.of(data.copyTag()));
        }
        result.set(DataComponents.POTION_CONTENTS, contents);
        return result;
      })
      .orElse(FluidStack.EMPTY);
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = super.serialize(context);
    json.addProperty("type", ID.toString());
    return json;
  }

  /** Unique loader instance */
  public static final JsonDeserializer<EmptyPotionTransfer> DESERIALIZER = new Deserializer<>(EmptyPotionTransfer::new);
}
