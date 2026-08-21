package slimeknights.tconstruct.fluids.item;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.fluid.transfer.FillFluidContainerTransfer;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.TConstruct;

/** Fills an empty bucket from potion fluid, preserving potion data components on the resulting potion bucket. */
public class FillPotionBucketTransfer extends FillFluidContainerTransfer {
  public static final Identifier ID = TConstruct.getResource("fill_potion_bucket");

  public FillPotionBucketTransfer(Ingredient input, ItemOutput filled, FluidIngredient fluid) {
    super(input, filled, fluid);
  }

  @Override
  protected ItemStack getFilled(FluidStack drained) {
    ItemStack bucket = drained.getFluidType().getBucket(drained);
    return bucket.isEmpty() ? super.getFilled(drained) : bucket;
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = super.serialize(context);
    json.addProperty("type", ID.toString());
    return json;
  }

  /** Unique loader instance */
  public static final JsonDeserializer<FillPotionBucketTransfer> DESERIALIZER = new Deserializer<>(FillPotionBucketTransfer::new);
}