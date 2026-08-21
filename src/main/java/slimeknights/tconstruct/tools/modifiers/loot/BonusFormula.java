package slimeknights.tconstruct.tools.modifiers.loot;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import slimeknights.mantle.util.JsonHelper;

import java.util.Map;

/** Stub replacing removed ApplyBonusCount formula classes */
public interface BonusFormula {
    int calculateNewCount(RandomSource random, int base, int level);
    Identifier getType();
    void serializeParams(JsonObject json, JsonSerializationContext context);

    interface FormulaDeserializer {
        BonusFormula deserialize(JsonObject json, JsonDeserializationContext context);
    }

    Map<Identifier, FormulaDeserializer> FORMULAS = ImmutableMap.of(
        Identifier.fromNamespaceAndPath("minecraft", "binomial_with_bonus_count"), (json, context) -> {
            int extra = GsonHelper.getAsInt(json, "extra");
            float probability = GsonHelper.getAsFloat(json, "probability");
            return new BinomialWithBonusCount(extra, probability);
        },
        Identifier.fromNamespaceAndPath("minecraft", "ore_drops"), (json, context) -> new OreDrops(),
        Identifier.fromNamespaceAndPath("minecraft", "uniform_bonus_count"), (json, context) -> {
            int bonusMultiplier = GsonHelper.getAsInt(json, "bonusMultiplier");
            return new UniformBonusCount(bonusMultiplier);
        }
    );

    com.mojang.serialization.Codec<BonusFormula> CODEC = net.minecraft.resources.Identifier.CODEC.dispatch(
        BonusFormula::getType,
        id -> {
            if (id.toString().equals("minecraft:binomial_with_bonus_count")) return BinomialWithBonusCount.MAP_CODEC;
            if (id.toString().equals("minecraft:ore_drops")) return OreDrops.MAP_CODEC;
            if (id.toString().equals("minecraft:uniform_bonus_count")) return UniformBonusCount.MAP_CODEC;
            return OreDrops.MAP_CODEC;
        }
    );

    class BinomialWithBonusCount implements BonusFormula {
        public static final com.mojang.serialization.MapCodec<BinomialWithBonusCount> MAP_CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
            com.mojang.serialization.Codec.INT.fieldOf("extra").forGetter(c -> c.extra),
            com.mojang.serialization.Codec.FLOAT.fieldOf("probability").forGetter(c -> c.probability)
        ).apply(inst, BinomialWithBonusCount::new));

        private final int extra;
        private final float probability;
        public BinomialWithBonusCount(int extra, float probability) {
            this.extra = extra;
            this.probability = probability;
        }
        @Override
        public int calculateNewCount(RandomSource random, int base, int level) {
            int count = base;
            for (int i = 0; i < level + extra; i++) {
                if (random.nextFloat() < probability) {
                    count++;
                }
            }
            return count;
        }
        @Override
        public Identifier getType() { return Identifier.fromNamespaceAndPath("minecraft", "binomial_with_bonus_count"); }
        @Override
        public void serializeParams(JsonObject json, JsonSerializationContext context) {
            json.addProperty("extra", extra);
            json.addProperty("probability", probability);
        }
    }

    class OreDrops implements BonusFormula {
        public static final com.mojang.serialization.MapCodec<OreDrops> MAP_CODEC = com.mojang.serialization.MapCodec.unit(new OreDrops());

        @Override
        public int calculateNewCount(RandomSource random, int base, int level) {
            return base + random.nextInt(level + 1);
        }
        @Override
        public Identifier getType() { return Identifier.fromNamespaceAndPath("minecraft", "ore_drops"); }
        @Override
        public void serializeParams(JsonObject json, JsonSerializationContext context) {}
    }

    class UniformBonusCount implements BonusFormula {
        public static final com.mojang.serialization.MapCodec<UniformBonusCount> MAP_CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
            com.mojang.serialization.Codec.INT.fieldOf("bonusMultiplier").forGetter(c -> c.bonusMultiplier)
        ).apply(inst, UniformBonusCount::new));

        private final int bonusMultiplier;
        public UniformBonusCount(int bonusMultiplier) { this.bonusMultiplier = bonusMultiplier; }
        @Override
        public int calculateNewCount(RandomSource random, int base, int level) {
            return base + random.nextInt(level * bonusMultiplier + 1);
        }
        @Override
        public Identifier getType() { return Identifier.fromNamespaceAndPath("minecraft", "uniform_bonus_count"); }
        @Override
        public void serializeParams(JsonObject json, JsonSerializationContext context) {
            json.addProperty("bonusMultiplier", bonusMultiplier);
        }
    }
}
