package slimeknights.tconstruct.library.modifiers.modules.behavior;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.capability.BlockItemProviderModifierHook;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

public class BlockItemProviderModule implements ModifierModule, BlockItemProviderModifierHook, ModifierCondition.ConditionalModule<IToolStackView> {
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = List.of(ModifierHooks.BLOCK_ITEM_PROVIDER);

    private record BlockItemData(Item item, int count) {}

    private static final Loadable<BlockItemData> ITEM_LOADABLE = new Loadable<>() {
        @Override
        public BlockItemData convert(JsonElement element, String key, TypedMap context) {
            JsonObject json = net.minecraft.util.GsonHelper.convertToJsonObject(element, key);
            Identifier id = Identifier.parse(net.minecraft.util.GsonHelper.getAsString(json, "id"));
            int count = net.minecraft.util.GsonHelper.getAsInt(json, "count", 1);
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item == null) throw new JsonSyntaxException("Unknown item: " + id);
            if (!(item instanceof BlockItem)) throw new JsonSyntaxException("Expected BlockItem, got: " + id);
            return new BlockItemData(item, count);
        }
        @Override
        public JsonElement serialize(BlockItemData data) {
            JsonObject json = new JsonObject();
            json.addProperty("id", BuiltInRegistries.ITEM.getKey(data.item()).toString());
            json.addProperty("count", data.count());
            return json;
        }
        @Override
        public BlockItemData decode(FriendlyByteBuf buf, TypedMap context) {
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf) buf);
            return new BlockItemData(stack.getItem(), stack.getCount());
        }
        @Override
        public void encode(FriendlyByteBuf buf, BlockItemData data) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf) buf, new ItemStack(data.item(), data.count()));
        }
    };

    public static final RecordLoadable<BlockItemProviderModule> LOADER = RecordLoadable.create(
      ITEM_LOADABLE.requiredField("item", BlockItemProviderModule::item),
      IntLoadable.FROM_ZERO.defaultField("tool_damage", 1, BlockItemProviderModule::damage),
      ModifierCondition.TOOL_FIELD,
      BlockItemProviderModule::new);

    private final Item item;
    private final int count;
    private final int damage;
    private final ModifierCondition<IToolStackView> condition;

    public BlockItemProviderModule(BlockItemData item, int damage, ModifierCondition<IToolStackView> condition) {
        this(item.item(), item.count(), damage, condition);
    }

    public BlockItemProviderModule(Item item, int count, int damage, ModifierCondition<IToolStackView> condition) {
        this.item = item;
        this.count = count;
        this.damage = damage;
        this.condition = condition;
    }

    /** Datagen factory for block item provider modules. */
    public static BlockItemProviderModule create(ItemLike item, int damage, ModifierCondition<IToolStackView> condition) {
        return new BlockItemProviderModule(item.asItem(), 1, damage, condition);
    }

    public ItemStack stack() { return new ItemStack(item, count); }
    public BlockItemData item() { return new BlockItemData(item, count); }
    public int damage() { return damage; }
    public ModifierCondition<IToolStackView> condition() { return condition; }

    @Override
    public RecordLoadable<BlockItemProviderModule> getLoader() { return LOADER; }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() { return DEFAULT_HOOKS; }

    @Override
    public ItemStack getBlockItemStack(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity entity) {
        return !tool.isBroken() && condition.matches(tool, modifier) ? new ItemStack(item, count) : ItemStack.EMPTY;
    }

    @Override
    public boolean consumeBlockItem(IToolStackView tool, ModifierEntry modifier, ItemStack backingStack, @Nullable LivingEntity entity) {
        if (!ItemStack.isSameItemSameComponents(new ItemStack(item, count), backingStack)) return false;
        if (damage > 0) {
            ToolDamageUtil.damageAnimated(tool, damage, entity, modifier.getId());
        }
        return true;
    }
}
