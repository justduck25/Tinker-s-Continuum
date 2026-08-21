package slimeknights.tconstruct.library.modifiers.modules.behavior;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierRemovalHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class InfinityModule implements ModifierModule, BowAmmoModifierHook, ModifierRemovalHook, ProjectileLaunchModifierHook.NoShooter {
  private static final String INFINITY = "tic_infinity";
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<InfinityModule>defaultHooks(ModifierHooks.BOW_AMMO, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.REMOVE);
  public static final RecordLoadable<InfinityModule> LOADER = RecordLoadable.create(
    ItemOutput.Loadable.REQUIRED_ITEM.requiredField("ammo", infinityModule -> infinityModule.ammoOutput),
    StringLoadable.DEFAULT.defaultField("variant_tag", "", InfinityModule::variantTag),
    IntLoadable.FROM_ZERO.requiredField("durability_usage", InfinityModule::durabilityUsage),
    BooleanLoadable.INSTANCE.defaultField("check_standard_arrows", true, InfinityModule::checkStandardArrows),
    InfinityModule::new
  );

  private final ItemOutput ammoOutput;
  private final String variantTag;
  private final int durabilityUsage;
  private final boolean checkStandardArrows;

  public InfinityModule(ItemOutput ammoOutput, String variantTag, int durabilityUsage, boolean checkStandardArrows) {
    this.ammoOutput = ammoOutput;
    this.variantTag = variantTag;
    this.durabilityUsage = durabilityUsage;
    this.checkStandardArrows = checkStandardArrows;
  }

  public static InfinityModule create(ItemLike ammo, String variantTag, int durabilityUsage, boolean checkStandardArrows) {
    return new InfinityModule(ItemOutput.fromItem(ammo), variantTag, durabilityUsage, checkStandardArrows);
  }

  public String variantTag() { return variantTag; }
  public int durabilityUsage() { return durabilityUsage; }
  public boolean checkStandardArrows() { return checkStandardArrows; }

  @Override
  public RecordLoadable<InfinityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public ItemStack findAmmo(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, ItemStack standardAmmo, Predicate<ItemStack> ammoPredicate) {
    if (checkStandardArrows && !standardAmmo.isEmpty()) {
      return ItemStack.EMPTY;
    }
    int count = durabilityUsage <= 0 ? 64 : Math.min(64, (tool.getCurrentDurability() + durabilityUsage - 1) / durabilityUsage);
    ItemStack ammo = ammoOutput.get().copyWithCount(count);
    CompoundTag tag = new CompoundTag();
    tag.putBoolean(INFINITY, true);
    if (!variantTag.isEmpty()) {
      String variant = tool.getPersistentData().getString(modifier.getId().getId());
      if (!variant.isEmpty()) {
        tag.putString(variantTag, variant);
      }
    }
    ammo.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    return ammo;
  }

  @Override
  public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (arrow != null && arrow.pickup != Pickup.CREATIVE_ONLY) {
      CustomData data = ammo.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
      if (data.copyTag().getBoolean(INFINITY).orElse(false)) {
        arrow.pickup = Pickup.CREATIVE_ONLY;
      }
    }
  }

  @Override
  public void shrinkAmmo(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, ItemStack ammo, int needed) {
    if (durabilityUsage > 0) {
      ToolDamageUtil.damageAnimated(tool, durabilityUsage * needed, shooter, shooter.getUsedItemHand(), modifier.getId());
    }
  }

  @Nullable
  @Override
  public Component onRemoved(IToolStackView tool, Modifier modifier) {
    if (!variantTag.isEmpty()) {
      tool.getPersistentData().remove(modifier.getId().getId());
    }
    return null;
  }
}
