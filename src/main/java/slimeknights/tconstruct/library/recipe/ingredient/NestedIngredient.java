package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/** Ingredient that contains another ingredient nested inside */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class NestedIngredient implements ICustomIngredient {
  protected final Ingredient nested;

  protected static JsonElement nestedToJson(Ingredient ingredient) {
    return Ingredient.CODEC.encodeStart(slimeknights.mantle.util.JsonHelper.REGISTRY_OPS, ingredient).getOrThrow(JsonParseException::new);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return nested.test(stack);
  }

  @Override
  public Stream<Holder<Item>> items() {
    return nested.items();
  }

  @Override
  public SlotDisplay display() {
    return nested.display();
  }

  @Override
  public boolean isSimple() {
    return nested.isSimple();
  }
}
