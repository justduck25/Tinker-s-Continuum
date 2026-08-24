package slimeknights.tconstruct.library.json.loot;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.experimental.Accessors;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.data.loadable.LoadableCodec;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.RandomMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolMaterialHook;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.ArrayList;
import java.util.List;

/** Loot function to add data to a tool. */
public class AddToolDataFunction extends LootItemConditionalFunction {
  private static final Codec<RandomMaterial> RANDOM_MATERIAL_CODEC = new LoadableCodec<>(RandomMaterial.LOADER);
  private static final Codec<ModifierEntry> MODIFIER_ENTRY_CODEC = new LoadableCodec<>(ModifierEntry.LOADABLE);

  public static final MapCodec<AddToolDataFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(f -> f.predicates),
    Codec.FLOAT.optionalFieldOf("damage_percent", 0f).validate(damage -> damage >= 0 && damage <= 1 ? com.mojang.serialization.DataResult.success(damage) : com.mojang.serialization.DataResult.error(() -> "damage_percent must be between 0 and 1, given " + damage)).forGetter(f -> f.damage),
    RANDOM_MATERIAL_CODEC.listOf().optionalFieldOf("materials", List.of()).forGetter(f -> f.materials),
    MODIFIER_ENTRY_CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(f -> f.modifiers),
    MODIFIER_ENTRY_CODEC.listOf().optionalFieldOf("random_modifiers", List.of()).forGetter(f -> f.randomModifiers),
    Codec.INT.optionalFieldOf("random_modifier_min", 0).validate(count -> count >= 0 ? com.mojang.serialization.DataResult.success(count) : com.mojang.serialization.DataResult.error(() -> "random_modifier_min must be non-negative, given " + count)).forGetter(f -> f.randomModifierMin),
    Codec.INT.optionalFieldOf("random_modifier_max", 0).validate(count -> count >= 0 ? com.mojang.serialization.DataResult.success(count) : com.mojang.serialization.DataResult.error(() -> "random_modifier_max must be non-negative, given " + count)).forGetter(f -> f.randomModifierMax),
    Codec.INT.optionalFieldOf("upgrade_slots", 0).validate(slots -> slots >= 0 ? com.mojang.serialization.DataResult.success(slots) : com.mojang.serialization.DataResult.error(() -> "upgrade_slots must be non-negative, given " + slots)).forGetter(f -> f.upgradeSlots),
    Codec.INT.optionalFieldOf("ability_slots", 0).validate(slots -> slots >= 0 ? com.mojang.serialization.DataResult.success(slots) : com.mojang.serialization.DataResult.error(() -> "ability_slots must be non-negative, given " + slots)).forGetter(f -> f.abilitySlots)
  ).apply(instance, AddToolDataFunction::new));

  /** Percentage of damage on the tool, if 0 the tool is undamaged */
  private final float damage;
  /** Fixed materials on the tool, any nulls in the list will randomize */
  private final List<RandomMaterial> materials;
  /** Modifiers to add as recipe upgrades */
  private final List<ModifierEntry> modifiers;
  /** Pool of modifiers to randomly add as recipe upgrades */
  private final List<ModifierEntry> randomModifiers;
  /** Minimum number of modifiers to select from {@link #randomModifiers} */
  private final int randomModifierMin;
  /** Maximum number of modifiers to select from {@link #randomModifiers} */
  private final int randomModifierMax;
  /** Extra upgrade slots to add before modifiers */
  private final int upgradeSlots;
  /** Extra ability slots to add before modifiers */
  private final int abilitySlots;

  protected AddToolDataFunction(List<LootItemCondition> conditions, float damage, List<RandomMaterial> materials, List<ModifierEntry> modifiers, List<ModifierEntry> randomModifiers, int randomModifierMin, int randomModifierMax, int upgradeSlots, int abilitySlots) {
    super(conditions == null ? List.of() : conditions);
    this.damage = damage;
    this.materials = materials;
    this.modifiers = modifiers;
    this.randomModifiers = randomModifiers;
    this.randomModifierMin = randomModifierMin;
    this.randomModifierMax = randomModifierMax;
    this.upgradeSlots = upgradeSlots;
    this.abilitySlots = abilitySlots;
  }

  /** Creates a new builder */
  public static AddToolDataFunction.Builder builder() {
    return new AddToolDataFunction.Builder();
  }

  @Override
  public MapCodec<? extends LootItemConditionalFunction> codec() {
    return TinkerTools.lootAddToolData.get();
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    if (stack.is(TinkerTags.Items.MODIFIABLE)) {
      ToolStack tool = ToolStack.from(stack);
      ToolDefinition definition = tool.getDefinition();
      if (definition.hasMaterials() && !materials.isEmpty()) {
        tool.setMaterials(RandomMaterial.build(ToolMaterialHook.stats(definition), materials, context.getRandom()));
      } else {
        // not multipart? no sense doing materials, just initialize stats
        tool.rebuildStats();
      }
      boolean needsRebuild = false;
      if (upgradeSlots > 0) {
        tool.getPersistentData().addSlots(SlotType.UPGRADE, upgradeSlots);
        needsRebuild = true;
      }
      if (abilitySlots > 0) {
        tool.getPersistentData().addSlots(SlotType.ABILITY, abilitySlots);
        needsRebuild = true;
      }
      for (ModifierEntry modifier : modifiers) {
        tool.addModifier(modifier.getId(), modifier.getLevel());
        needsRebuild = false;
      }
      for (ModifierEntry modifier : selectRandomModifiers(context.getRandom())) {
        tool.addModifier(modifier.getId(), modifier.getLevel());
        needsRebuild = false;
      }
      if (needsRebuild) {
        tool.rebuildStats();
      }
      // set damage last to a percentage of max damage if requested
      if (damage > 0) {
        tool.setDamage((int)(tool.getStats().get(ToolStats.DURABILITY) * damage));
      }
      tool.updateStack(stack, false);
    }
    return stack;
  }

  /** Selects a random subset of modifier entries without replacement. */
  private List<ModifierEntry> selectRandomModifiers(RandomSource random) {
    if (randomModifiers.isEmpty() || randomModifierMax <= 0) {
      return List.of();
    }
    int max = Math.min(randomModifierMax, randomModifiers.size());
    int min = Math.min(randomModifierMin, max);
    int count = min + random.nextInt(max - min + 1);
    if (count == randomModifiers.size()) {
      return randomModifiers;
    }

    List<ModifierEntry> pool = new ArrayList<>(randomModifiers);
    for (int i = 0; i < count; i++) {
      int selected = i + random.nextInt(pool.size() - i);
      ModifierEntry entry = pool.get(i);
      pool.set(i, pool.get(selected));
      pool.set(selected, entry);
    }
    return pool.subList(0, count);
  }

  /** Builder to create a new add tool data function */
  @Accessors(chain = true)
  public static class Builder extends LootItemConditionalFunction.Builder<AddToolDataFunction.Builder> {
    private final ImmutableList.Builder<RandomMaterial> materials = ImmutableList.builder();
    private final ImmutableList.Builder<ModifierEntry> modifiers = ImmutableList.builder();
    private final ImmutableList.Builder<ModifierEntry> randomModifiers = ImmutableList.builder();
    private float damage = 0;
    private int randomModifierMin = 0;
    private int randomModifierMax = 0;
    private int upgradeSlots = 0;
    private int abilitySlots = 0;

    protected Builder() {}

    @Override
    protected Builder getThis() {
      return this;
    }

    /** Sets the damage for the tool */
    public void setDamage(float damage) {
      if (damage < 0 || damage > 1) {
        throw new IllegalArgumentException("Damage must be between 0 and 1, given " + damage);
      }
      this.damage = damage;
    }

    /** Adds a material to the builder */
    public Builder addMaterial(RandomMaterial mat) {
      materials.add(mat);
      return this;
    }

    /** Adds a material to the builder */
    public Builder addMaterial(MaterialVariantId mat) {
      return addMaterial(RandomMaterial.fixed(mat));
    }

    /** Adds a material to the builder */
    public Builder addMaterial(MaterialId mat) {
      return addMaterial(RandomMaterial.fixed(mat));
    }

    /** Adds a modifier to the tool. */
    public Builder addModifier(ModifierId modifier, int level) {
      modifiers.add(new ModifierEntry(modifier, level));
      return this;
    }

    /** Adds a modifier to the random modifier pool. */
    public Builder addRandomModifier(ModifierId modifier, int level) {
      randomModifiers.add(new ModifierEntry(modifier, level));
      return this;
    }

    /** Sets how many modifiers to select from the random modifier pool. */
    public Builder randomModifierCount(int min, int max) {
      if (min < 0 || max < 0 || min > max) {
        throw new IllegalArgumentException("Random modifier count must satisfy 0 <= min <= max, given " + min + " to " + max);
      }
      this.randomModifierMin = min;
      this.randomModifierMax = max;
      return this;
    }

    /** Adds bonus upgrade slots before adding modifiers. */
    public Builder addUpgradeSlots(int slots) {
      if (slots < 0) {
        throw new IllegalArgumentException("Slots must be non-negative, given " + slots);
      }
      this.upgradeSlots += slots;
      return this;
    }

    /** Adds bonus ability slots before adding modifiers. */
    public Builder addAbilitySlots(int slots) {
      if (slots < 0) {
        throw new IllegalArgumentException("Slots must be non-negative, given " + slots);
      }
      this.abilitySlots += slots;
      return this;
    }

    @Override
    public LootItemFunction build() {
      return new AddToolDataFunction(getConditions(), damage, materials.build(), modifiers.build(), randomModifiers.build(), randomModifierMin, randomModifierMax, upgradeSlots, abilitySlots);
    }
  }
}
