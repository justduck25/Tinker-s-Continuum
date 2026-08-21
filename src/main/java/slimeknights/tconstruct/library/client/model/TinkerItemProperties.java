package slimeknights.tconstruct.library.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import org.jspecify.annotations.Nullable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** Item model properties for Tinkers' tools on the 1.21.6+ item model definition system. */
public final class TinkerItemProperties {
  private TinkerItemProperties() {}

  private static final Identifier BROKEN_ID = TConstruct.getResource("broken");
  private static final Identifier AMMO_ID = TConstruct.getResource("ammo");
  private static final Identifier CAST_ID = TConstruct.getResource("cast");
  private static final Identifier CHARGING_ID = TConstruct.getResource("charging");
  private static final Identifier CHARGE_ID = TConstruct.getResource("charge");

  /** Registers conditional properties such as {@code tconstruct:broken}. */
  public static void registerConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
    event.register(BROKEN_ID, BrokenProperty.MAP_CODEC);
  }

  /** Registers numeric properties such as {@code tconstruct:charge}, {@code tconstruct:charging}, and {@code tconstruct:ammo}. */
  public static void registerRangeProperties(RegisterRangeSelectItemModelPropertyEvent event) {
    event.register(AMMO_ID, AmmoProperty.MAP_CODEC);
    event.register(CAST_ID, CastProperty.MAP_CODEC);
    event.register(CHARGING_ID, ChargingProperty.MAP_CODEC);
    event.register(CHARGE_ID, ChargeProperty.MAP_CODEC);
  }

  private record BrokenProperty() implements ConditionalItemModelProperty {
    private static final MapCodec<BrokenProperty> MAP_CODEC = MapCodec.unit(new BrokenProperty());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
      return ToolDamageUtil.isBroken(stack);
    }

    @Override
    public MapCodec<BrokenProperty> type() {
      return MAP_CODEC;
    }
  }

  private record AmmoProperty() implements RangeSelectItemModelProperty {
    private static final MapCodec<AmmoProperty> MAP_CODEC = MapCodec.unit(new AmmoProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      CompoundTag persistent = persistentData(stack);
      if (!persistent.isEmpty()) {
        CompoundTag ammo = persistent.getCompound(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO.toString()).orElse(new CompoundTag());
        if (!ammo.isEmpty()) {
          return ammo.getString("id").orElse("").equals(BuiltInRegistries.ITEM.getKey(Items.FIREWORK_ROCKET).toString()) ? 2 : 1;
        }
      }
      return 0;
    }

    @Override
    public MapCodec<AmmoProperty> type() {
      return MAP_CODEC;
    }
  }

  private record CastProperty() implements RangeSelectItemModelProperty {
    private static final MapCodec<CastProperty> MAP_CODEC = MapCodec.unit(new CastProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      LivingEntity holder = owner == null ? null : owner.asLivingEntity();
      return holder instanceof Player player && player.fishing != null ? 1 : 0;
    }

    @Override
    public MapCodec<CastProperty> type() {
      return MAP_CODEC;
    }
  }

  private record ChargingProperty() implements RangeSelectItemModelProperty {
    private static final MapCodec<ChargingProperty> MAP_CODEC = MapCodec.unit(new ChargingProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      LivingEntity holder = owner == null ? null : owner.asLivingEntity();
      if (holder != null && holder.isUsingItem() && holder.getUseItem() == stack) {
        ItemUseAnimation anim = stack.getUseAnimation();
        if (anim == ItemUseAnimation.BLOCK) {
          return ModifierUtil.checkPersistentPresent(stack, ModifiableLauncherItem.KEY_DRAWBACK_AMMO) ? 2.5f : 2;
        }
        if (anim == ItemUseAnimation.SPEAR) {
          return 1.75f;
        }
        if (anim != ItemUseAnimation.EAT && anim != ItemUseAnimation.DRINK) {
          return ModifierUtil.checkPersistentPresent(stack, ModifiableLauncherItem.KEY_DRAWBACK_AMMO) ? 1.5f : 1;
        }
      }
      return 0;
    }

    @Override
    public MapCodec<ChargingProperty> type() {
      return MAP_CODEC;
    }
  }

  private record ChargeProperty() implements RangeSelectItemModelProperty {
    private static final MapCodec<ChargeProperty> MAP_CODEC = MapCodec.unit(new ChargeProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      LivingEntity holder = owner == null ? null : owner.asLivingEntity();
      if (holder == null || holder.getUseItem() != stack) {
        return 0;
      }
      int drawtime = ModifierUtil.getPersistentInt(stack, GeneralInteractionModifierHook.KEY_DRAWTIME, -1);
      return drawtime == -1 ? 0 : (float)(stack.getUseDuration(holder) - holder.getUseItemRemainingTicks()) / drawtime;
    }

    @Override
    public MapCodec<ChargeProperty> type() {
      return MAP_CODEC;
    }
  }

  private static CompoundTag persistentData(ItemStack stack) {
    CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    if (!nbt.isEmpty()) {
      Tag tag = nbt.get(ToolStack.TAG_PERSISTENT_MOD_DATA);
      if (tag instanceof CompoundTag compound) {
        return compound;
      }
    }
    return new CompoundTag();
  }
}
