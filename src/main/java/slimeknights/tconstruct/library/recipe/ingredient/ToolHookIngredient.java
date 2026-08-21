package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Ingredient that only matches tools with a specific hook */
public class ToolHookIngredient implements ICustomIngredient {
  public static final Identifier ID = TConstruct.getResource("tool_hook");
  public static final IngredientType<ToolHookIngredient> TYPE = LegacyIngredientType.of(ToolHookIngredient::parseCustom, ToolHookIngredient::toJson);

  private final TagKey<Item> tag;
  private final ModuleHook<?> hook;

  protected ToolHookIngredient(TagKey<Item> tag, ModuleHook<?> hook) {
    this.tag = tag;
    this.hook = hook;
  }

  private static ToolHookIngredient parseCustom(JsonObject json) {
    return new ToolHookIngredient(
      Loadables.ITEM_TAG.getOrDefault(json, "tag", TinkerTags.Items.MODIFIABLE),
      ToolHooks.LOADER.getIfPresent(json, "hook")
    );
  }

  public static Ingredient of(TagKey<Item> tag, ModuleHook<?> hook) {
    return new ToolHookIngredient(tag, hook).toVanilla();
  }

  public static Ingredient of(ModuleHook<?> hook) {
    return of(TinkerTags.Items.MODIFIABLE, hook);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && stack.is(tag) && stack.getItem() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public Stream<Holder<Item>> items() {
    return StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false);
  }

  @Override
  public SlotDisplay display() {
    return new SlotDisplay.Composite(items()
      .map(Holder::value)
      .filter(item -> item instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook))
      .map(ItemStack::new)
      .filter(stack -> !stack.isEmpty())
      .map(stack -> (SlotDisplay)new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack)))
      .toList());
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("neoforge:ingredient_type", ID.toString());
    json.addProperty("tag", tag.location().toString());
    json.addProperty("hook", hook.getId().toString());
    return json;
  }

  @RequiredArgsConstructor
  public static class ToolHookValue {
    private final TagKey<Item> tag;
    private final ModuleHook<?> hook;

    public Collection<ItemStack> getItems() {
      List<ItemStack> list = new ArrayList<>();
      for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
        if (holder.value() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook)) {
          list.add(new ItemStack(modifiable));
        }
      }
      if (list.isEmpty()) {
        list.add(new ItemStack(Blocks.BARRIER));
      }
      return list;
    }

    public JsonObject serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("id", ID.toString());
      json.addProperty("tag", tag.location().toString());
      json.addProperty("hook", hook.getId().toString());
      return json;
    }
  }

  public enum Serializer {
    INSTANCE;
    public static final Identifier ID = ToolHookIngredient.ID;
    public Ingredient parse(JsonObject json) {
      return ToolHookIngredient.parseCustom(json).toVanilla();
    }
  }
}
