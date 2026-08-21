package slimeknights.tconstruct.tools.modifiers.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.tools.modifiers.traits.skull.ChrysophiliteModifier;

import java.util.List;
import java.util.Set;

/** Loot modifier to boost drops based on teh chrysophilite amount */
public class ChrysophiliteBonusFunction extends LootItemConditionalFunction {
  public static final JsonSerializer SERIALIZER = new JsonSerializer();

  /** Formula to apply */
  private final BonusFormula formula;
  /** If true, the includes the helmet in the level, if false level is just gold pieces */
  private final boolean includeBase;
  protected ChrysophiliteBonusFunction(List<LootItemCondition> conditions, BonusFormula formula, boolean includeBase) {
    super(conditions == null ? List.of() : conditions);
    this.formula = formula;
    this.includeBase = includeBase;
  }

  /** Creates a generic builder */
  public static Builder<?> builder(BonusFormula formula, boolean includeBase) {
    return simpleBuilder(conditions -> new ChrysophiliteBonusFunction(conditions, formula, includeBase));
  }

  /** Creates a builder for the binomial with bonus formula */
  public static Builder<?> binomialWithBonusCount(float probability, int extra, boolean includeBase) {
    return builder(new BonusFormula.BinomialWithBonusCount(extra, probability), includeBase);
  }

  /** Creates a builder for the ore drops formula */
  public static Builder<?> oreDrops(boolean includeBase) {
    return builder(new BonusFormula.OreDrops(), includeBase);
  }

  /** Creates a builder for the uniform bonus count */
  public static Builder<?> uniformBonusCount(int bonusMultiplier, boolean includeBase) {
    return builder(new BonusFormula.UniformBonusCount(bonusMultiplier), includeBase);
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    int level = ChrysophiliteModifier.getTotalGold(context.getOptionalParameter(LootContextParams.THIS_ENTITY));
    if (!includeBase) {
      level--;
    }
    if (level > 0) {
      stack.setCount(formula.calculateNewCount(context.getRandom(), stack.getCount(), level));
    }
    return stack;
  }

  @Override
  public Set<ContextKey<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.THIS_ENTITY);
  }

  public static final MapCodec<ChrysophiliteBonusFunction> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
          LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", java.util.List.of()).forGetter(f -> f.predicates),
          BonusFormula.CODEC.fieldOf("formula").forGetter(f -> f.formula),
          com.mojang.serialization.Codec.BOOL.optionalFieldOf("include_base", true).forGetter(f -> f.includeBase)
      ).apply(inst, ChrysophiliteBonusFunction::new)
  );

  @Override
  public MapCodec<? extends LootItemConditionalFunction> codec() {
    return CODEC;
  }

  /** Serializer class */
  public static class JsonSerializer implements com.google.gson.JsonSerializer<ChrysophiliteBonusFunction>, com.google.gson.JsonDeserializer<ChrysophiliteBonusFunction> {
    @Override
    public JsonObject serialize(ChrysophiliteBonusFunction loot, java.lang.reflect.Type type, JsonSerializationContext context) {
      JsonObject json = new JsonObject();
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
    public ChrysophiliteBonusFunction deserialize(JsonElement element, java.lang.reflect.Type type, JsonDeserializationContext context) {
      JsonObject json = element.getAsJsonObject();
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
      return new ChrysophiliteBonusFunction(null, formula, includeBase);
    }
  }
}
