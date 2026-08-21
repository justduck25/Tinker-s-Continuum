package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ShapelessMaterialsRecipe extends ShapelessRecipe implements MaterialsCraftingTableRecipe {
  private static ItemStackTemplate template(ItemStack result) {
    return result.isEmpty() ? new ItemStackTemplate(Items.STICK) : ItemStackTemplate.fromNonEmptyStack(result);
  }

  private static ItemStackTemplate template(@Nullable ItemStackTemplate result) {
    return result == null ? new ItemStackTemplate(Items.STICK) : result;
  }

  private final Identifier id;
  @Getter
  private final int partCount;
  @Getter
  private final List<MaterialVariantId> extraMaterials;
  private final List<Ingredient> ingredients;
  private final ItemStackTemplate result;

  public ShapelessMaterialsRecipe(Identifier id, String group, net.minecraft.world.item.crafting.CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, int partCount, List<MaterialVariantId> extraMaterials) {
    super(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(category, group), template(result), ingredients);
    this.id = id;
    this.partCount = partCount;
    this.extraMaterials = List.copyOf(extraMaterials);
    this.ingredients = List.copyOf(ingredients);
    this.result = template(result);
  }

  /** Creates the runtime material recipe from a decoded vanilla shapeless recipe. */
  ShapelessMaterialsRecipe(ShapelessRecipe recipe, ItemStackTemplate result, List<Ingredient> ingredients, int partCount, List<MaterialVariantId> extraMaterials) {
    super(new Recipe.CommonInfo(recipe.showNotification()), new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()), template(result), NonNullList.copyOf(ingredients));
    this.id = TConstruct.getResource("decoded_shapeless_materials");
    this.partCount = partCount;
    this.extraMaterials = List.copyOf(extraMaterials);
    this.ingredients = List.copyOf(ingredients);
    this.result = template(result);
  }

  public Identifier getId() {
    return id;
  }

  @Override
  public List<Ingredient> getParts() {
    return ingredients;
  }

  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    ShapedMaterialsRecipe.setMaterial(stack, material, extraMaterials);
  }

  @Override
  public ItemStack assemble(CraftingInput inventory) {
    return ShapedMaterialsRecipe.assemble(result.create(), inventory, ingredients, partCount, false, extraMaterials);
  }

  @Override
  public RecipeSerializer<ShapelessRecipe> getSerializer() {
    return (RecipeSerializer<ShapelessRecipe>)(RecipeSerializer<?>)TinkerTables.shapelessMaterialsRecipeSerializer.get();
  }

  public static RecipeSerializer<ShapelessMaterialsRecipe> serializer() {
    return new RecipeSerializer<>((MapCodec<ShapelessMaterialsRecipe>)(MapCodec<?>)MaterialRecipeMapCodecs.SHAPELESS_MATERIALS, StreamCodec.of(ShapelessMaterialsRecipe::encodeNetwork, ShapelessMaterialsRecipe::decodeNetwork));
  }

  private static void encodeNetwork(RegistryFriendlyByteBuf buffer, ShapelessMaterialsRecipe recipe) {
    ShapelessRecipe.STREAM_CODEC.encode(buffer, recipe);
    buffer.writeVarInt(recipe.partCount);
    buffer.writeVarInt(recipe.ingredients.size());
    for (Ingredient ingredient : recipe.ingredients) {
      Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
    }
    buffer.writeVarInt(recipe.extraMaterials.size());
    for (MaterialVariantId material : recipe.extraMaterials) {
      buffer.writeUtf(material.toString());
    }
  }

  private static ShapelessMaterialsRecipe decodeNetwork(RegistryFriendlyByteBuf buffer) {
    Recipe.CommonInfo commonInfo = Recipe.CommonInfo.STREAM_CODEC.decode(buffer);
    CraftingRecipe.CraftingBookInfo bookInfo = CraftingRecipe.CraftingBookInfo.STREAM_CODEC.decode(buffer);
    ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
    List<Ingredient> baseIngredients = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
    ShapelessRecipe recipe = new ShapelessRecipe(commonInfo, bookInfo, result, baseIngredients);
    int partCount = buffer.readVarInt();
    int ingredientCount = buffer.readVarInt();
    List<Ingredient> ingredients = new ArrayList<>(ingredientCount);
    for (int i = 0; i < ingredientCount; i++) {
      ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
    }
    int extraCount = buffer.readVarInt();
    List<MaterialVariantId> extraMaterials = new ArrayList<>(extraCount);
    for (int i = 0; i < extraCount; i++) {
      extraMaterials.add(MaterialVariantId.parse(buffer.readUtf()));
    }
    return new ShapelessMaterialsRecipe(recipe, result, ingredients, partCount, extraMaterials);
  }
}
