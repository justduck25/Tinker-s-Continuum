package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Item ingredient matching items with a block form in the given tag */
@RequiredArgsConstructor
public class BlockTagIngredient implements ICustomIngredient {
  public static final Identifier ID = TConstruct.getResource("block_tag");
  public static final IngredientType<BlockTagIngredient> TYPE = LegacyIngredientType.of(BlockTagIngredient::parse, BlockTagIngredient::toJson);

  private final TagKey<Block> tag;
  @Nullable
  private List<Holder<Item>> matchingItems;

  private static BlockTagIngredient parse(JsonObject json) {
    return new BlockTagIngredient(Loadables.BLOCK_TAG.getIfPresent(json, "tag"));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && getMatchingItems().contains(stack.getItem().builtInRegistryHolder());
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  private List<Holder<Item>> getMatchingItems() {
    if (matchingItems == null) {
      List<Holder<Item>> list = new ArrayList<>();
      for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
        Item item = block.value().asItem();
        if (item != Items.AIR) {
          list.add(item.builtInRegistryHolder());
        }
      }
      matchingItems = List.copyOf(list);
    }
    return matchingItems;
  }

  @Override
  public Stream<Holder<Item>> items() {
    return getMatchingItems().stream();
  }

  @Override
  public SlotDisplay display() {
    return new SlotDisplay.Composite(getMatchingItems().stream()
      .map(Holder::value)
      .map(ItemStack::new)
      .filter(stack -> !stack.isEmpty())
      .map(stack -> (SlotDisplay)new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack)))
      .toList());
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("neoforge:ingredient_type", ID.toString());
    json.add("tag", Loadables.BLOCK_TAG.serialize(tag));
    return json;
  }

  public enum Serializer {
    INSTANCE;
    public static final Identifier ID = BlockTagIngredient.ID;
    public Ingredient parse(JsonObject json) {
      return BlockTagIngredient.parse(json).toVanilla();
    }
  }
}
