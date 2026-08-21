package slimeknights.tconstruct.tools.modifiers.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

import java.util.List;
import java.util.Set;

/** Boosts drop rates based on modifier level */
public class ModifierBonusLootFunction extends LootItemConditionalFunction {
  /** Modifier ID to use for multiplier bonus */
  private final ModifierId modifier;
  /** Formula to apply */
  private final BonusFormula formula;
  /** If true, considers level 1 as bonus, if false considers level 1 as no bonus */
  private final boolean includeBase;

  protected ModifierBonusLootFunction(List<LootItemCondition> conditions, ModifierId modifier, BonusFormula formula, boolean includeBase) {
    super(conditions == null ? List.of() : conditions);
    this.modifier = modifier;
    this.formula = formula;
    this.includeBase = includeBase;
  }

  /** Creates a generic builder */
  public static Builder<?> builder(ModifierId modifier, BonusFormula formula, boolean includeBase) {
    return simpleBuilder(conditions -> new ModifierBonusLootFunction(conditions, modifier, formula, includeBase));
  }

  /** Creates a builder for the binomial with bonus formula */
  public static Builder<?> binomialWithBonusCount(ModifierId modifier, float probability, int extra, boolean includeBase) {
    return builder(modifier, new BonusFormula.BinomialWithBonusCount(extra, probability), includeBase);
  }

  /** Creates a builder for the ore drops formula */
  public static Builder<?> oreDrops(ModifierId modifier, boolean includeBase) {
    return builder(modifier, new BonusFormula.OreDrops(), includeBase);
  }

  /** Creates a builder for the uniform bonus count */
  public static Builder<?> uniformBonusCount(ModifierId modifier, int bonusMultiplier, boolean includeBase) {
    return builder(modifier, new BonusFormula.UniformBonusCount(bonusMultiplier), includeBase);
  }

  public static final MapCodec<ModifierBonusLootFunction> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
          LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", java.util.List.of()).forGetter(f -> f.predicates),
          net.minecraft.resources.Identifier.CODEC.xmap(id -> new ModifierId(id.toString()), id -> id.getId()).fieldOf("modifier").forGetter(f -> f.modifier),
          BonusFormula.CODEC.fieldOf("formula").forGetter(f -> f.formula),
          com.mojang.serialization.Codec.BOOL.optionalFieldOf("include_base", true).forGetter(f -> f.includeBase)
      ).apply(inst, ModifierBonusLootFunction::new)
  );

  @Override
  public MapCodec<? extends LootItemConditionalFunction> codec() {
    return CODEC;
  }

  @Override
  public Set<ContextKey<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.TOOL);
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    ItemInstance toolInstance = context.getParameter(LootContextParams.TOOL);
    ItemStack tool = toolInstance instanceof ItemStack toolStack ? toolStack : ItemStack.EMPTY;
    int level = ModifierUtil.getModifierLevel(tool, modifier);
    if (!includeBase) {
      level--;
    }
    if (level > 0) {
      stack.setCount(formula.calculateNewCount(context.getRandom(), stack.getCount(), level));
    }
    return stack;
  }

  /** Serializer class */
  public static class JsonSerializer implements com.google.gson.JsonSerializer<ModifierBonusLootFunction>, com.google.gson.JsonDeserializer<ModifierBonusLootFunction> {
    @Override
    public JsonObject serialize(ModifierBonusLootFunction loot, java.lang.reflect.Type type, JsonSerializationContext context) {
      JsonObject json = new JsonObject();
      json.addProperty("modifier", loot.modifier.toString());
      json.addProperty("formula", loot.formula.getType().toString());
      JsonObject parameters = new JsonObject();
      loot.formula.serializeParams(parameters, context);
      if (parameters.size() > 0) {
        json.add("parameters", parameters);
      }
      json.addProperty("include_base", loot.includeBase);
      return json;
    }

    @Override
    public ModifierBonusLootFunction deserialize(JsonElement element, java.lang.reflect.Type type, JsonDeserializationContext context) {
      JsonObject json = element.getAsJsonObject();
      ModifierId modifier = new ModifierId(JsonHelper.getIdentifier(json, "modifier"));
      Identifier id = JsonHelper.getIdentifier(json, "formula");
      BonusFormula.FormulaDeserializer deserializer = BonusFormula.FORMULAS.get(id);
      if (deserializer == null) {
        throw new com.google.gson.JsonParseException("Invalid formula id: " + id);
      }
      JsonObject parameters;
      if (json.has("parameters")) {
        parameters = GsonHelper.getAsJsonObject(json, "parameters");
      } else {
        parameters = new JsonObject();
      }
      BonusFormula formula = deserializer.deserialize(parameters, context);
      boolean includeBase = GsonHelper.getAsBoolean(json, "include_base", true);
      return new ModifierBonusLootFunction(null, modifier, formula, includeBase);
    }
  }
}
