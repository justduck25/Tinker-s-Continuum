package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

/** Ingredient matching material items with the given value. Typically, matches ingots or blocks */
@Getter
@RequiredArgsConstructor
public class MaterialValueIngredient implements ICustomIngredient {
  public static final Identifier ID = TConstruct.getResource("material_value");
  public static final IngredientType<MaterialValueIngredient> TYPE = LegacyIngredientType.of(MaterialValueIngredient::parseCustom, MaterialValueIngredient::toJson);
  private static final LoadableField<IJsonPredicate<MaterialVariantId>, MaterialValueIngredient> MATERIAL_FIELD = new MaterialPredicateField<>("material", i -> i.material);

  private final IJsonPredicate<MaterialVariantId> material;
  private final float minValue;
  private final float maxValue;
  private ItemStack[] items;

  public static net.minecraft.world.item.crafting.Ingredient of(IJsonPredicate<MaterialVariantId> materials, float minValue, float maxValue) {
    return new MaterialValueIngredient(materials, minValue, maxValue).toVanilla();
  }

  public static net.minecraft.world.item.crafting.Ingredient of(IJsonPredicate<MaterialVariantId> materials, float value) {
    return of(materials, value, value);
  }

  private static MaterialValueIngredient parseCustom(JsonObject json) {
    float minValue, maxValue;
    JsonElement value = json.get("value");
    if (value.isJsonPrimitive()) {
      minValue = maxValue = value.getAsJsonPrimitive().getAsFloat();
    } else {
      JsonObject object = GsonHelper.convertToJsonObject(value, "value");
      minValue = GsonHelper.getAsFloat(object, "min", 0);
      maxValue = GsonHelper.getAsFloat(object, "max", Float.POSITIVE_INFINITY);
    }
    return new MaterialValueIngredient(MATERIAL_FIELD.get(json), minValue, maxValue);
  }

  public boolean test(MaterialRecipe material) {
    float value = material.getValue() / (float) material.getNeeded();
    return minValue <= value && value <= maxValue && this.material.matches(material.getMaterial().getVariant());
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null) {
      return false;
    }
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe);
  }

  public ItemStack[] getItems() {
    if (items == null) {
      items = MaterialRecipeCache.getAllRecipes().stream()
        .filter(this::test)
        .flatMap(material -> BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(material.getIngredient()::test))
        .toArray(ItemStack[]::new);
    }
    return items;
  }

  @Override
  public Stream<Holder<Item>> items() {
    return Stream.of(getItems()).map(stack -> stack.getItem().builtInRegistryHolder());
  }

  @Override
  public SlotDisplay display() {
    return new SlotDisplay.Composite(Arrays.stream(getItems())
      .filter(stack -> !stack.isEmpty())
      .map(stack -> (SlotDisplay)new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack)))
      .toList());
  }

  protected void invalidate() {
    this.items = null;
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  private boolean contains(MaterialValueIngredient other) {
    return this.minValue <= other.minValue && other.maxValue <= this.maxValue;
  }

  public MaterialValueIngredient merge(MaterialValueIngredient other) {
    if (this == other) return this;
    IJsonPredicate<MaterialVariantId> predicate = this.material;
    if (this.material.equals(other.material)) {
      if (this.contains(other)) {
        return this;
      }
      if (other.contains(this)) {
        return other;
      }
    } else {
      predicate = MaterialPredicate.or(this.material, other.material);
    }
    return new MaterialValueIngredient(predicate, Math.min(this.minValue, other.minValue), Math.max(this.maxValue, other.maxValue));
  }

  @Nullable
  public MaterialVariantId getMaterial(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe) ? recipe.getMaterial().getVariant() : null;
  }

  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("neoforge:ingredient_type", ID.toString());
    MATERIAL_FIELD.serialize(this, json);
    if (minValue == maxValue) {
      json.addProperty("value", minValue);
    } else {
      JsonObject value = new JsonObject();
      if (minValue > 0) {
        value.addProperty("min", minValue);
      }
      if (Float.isFinite(maxValue)) {
        value.addProperty("max", maxValue);
      }
      json.add("value", value);
    }
    return json;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  public enum Serializer {
    INSTANCE;
    public static final Identifier ID = MaterialValueIngredient.ID;
    public net.minecraft.world.item.crafting.Ingredient parse(JsonObject json) {
      return MaterialValueIngredient.parseCustom(json).toVanilla();
    }
  }
}
