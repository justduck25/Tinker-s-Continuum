package slimeknights.tconstruct.library.modifiers.hook.mining;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.EnchantmentModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.LootingModifierHook;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public interface HarvestEnchantmentsModifierHook {
  EquipmentSlot[] APPLICABLE_SLOTS = { EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
  /** Original enchantments for stacks temporarily prepared for vanilla block drops during {@link net.neoforged.neoforge.event.level.block.BreakBlockEvent}. */
  Map<ItemStack,ItemEnchantments> ACTIVE_HARVEST_ENCHANTMENTS = java.util.Collections.synchronizedMap(new IdentityHashMap<>());
  /**
   * Snapshot of a stack taken right after its modifier enchantments were written into the enchantment component.
   * Vanilla copies the tool before {@code mineBlock} but only computes loot after it, so the copy handed to the loot
   * table still carries the written enchantments once the original has been restored. Matching on components rather
   * than identity lets that copy be recognised, and the snapshot is deliberately kept past the restore to cover it.
   */
  ThreadLocal<ItemStack> BAKED_STACK = new ThreadLocal<>();

  void updateHarvestEnchantments(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context, EquipmentContext equipment, EquipmentSlot slot, Map<Enchantment,Integer> map);

  @Nullable
  static ItemEnchantments updateHarvestEnchantments(IToolStackView tool, ItemStack stack, ToolHarvestContext context) {
    // already prepared earlier in this break, writing again would stack the modifier levels on top of themselves
    if (hasBakedEnchantments(stack)) {
      return null;
    }
    Player player = context.getPlayer();
    if (player == null || !player.isCreative()) {
      EquipmentContext equipmentContext = EquipmentContext.withTool(context.getLiving(), tool, EquipmentSlot.MAINHAND);
      ItemEnchantments originalEnchants = stack.getEnchantments();
      Map<Enchantment,Integer> enchantments = new java.util.HashMap<>();
      for (var entry : originalEnchants.entrySet()) {
        enchantments.put(entry.getKey().value(), entry.getIntValue());
      }
      Map<Enchantment,Integer> originalMap = new java.util.HashMap<>(enchantments);
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.ENCHANTMENTS).updateEnchantments(tool, entry, enchantments);
      }
      for (EquipmentSlot slot : APPLICABLE_SLOTS) {
        IToolStackView armor = equipmentContext.getValidTool(slot);
        if (armor != null) {
          for (ModifierEntry entry : armor.getModifierList()) {
            HarvestEnchantmentsModifierHook hook = entry.getModifier().getHooks().getOrNull(ModifierHooks.HARVEST_ENCHANTMENTS);
            if (hook != null) {
              hook.updateHarvestEnchantments(armor, entry, context, equipmentContext, slot, enchantments);
            }
          }
        }
      }
      enchantments.values().removeIf(EnchantmentModifierHook.VALUE_REMOVER);
      if (!enchantments.equals(originalMap)) {
        ItemEnchantments original = originalEnchants;
        java.util.Map<Enchantment,Integer> fixedEnchantments = enchantments;
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(original);
        mutable.removeIf(holder -> !fixedEnchantments.containsKey(holder.value()));
        for (java.util.Map.Entry<Enchantment, Integer> entry : fixedEnchantments.entrySet()) {
          mutable.set(registryHolder(context, entry.getKey()), entry.getValue());
        }
        EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
        BAKED_STACK.set(stack.copy());
        return originalEnchants;
      }
    }
    BAKED_STACK.remove();
    return null;
  }

  /**
   * {@return true while the given stack already has its modifier enchantments written into NBT for block loot}
   * While set, {@link EnchantmentModifierHook} must not add modifier levels on top of the component, as that would double the bonus.
   */
  static boolean hasBakedEnchantments(ItemStack stack) {
    ItemStack baked = BAKED_STACK.get();
    return baked != null && ItemStack.isSameItemSameComponents(baked, stack);
  }

  /** Finds the registry holder for the enchantment, as vanilla loot checks registry holders rather than direct holders. */
  static Holder<Enchantment> registryHolder(ToolHarvestContext context, Enchantment enchantment) {
    return EnchantmentModifierHook.getHolder(context.getWorld().registryAccess().lookupOrThrow(Registries.ENCHANTMENT), enchantment);
  }

  /** Checks if a stack already has temporary harvest enchantments prepared. */
  static boolean hasActiveHarvestEnchantments(ItemStack stack) {
    return ACTIVE_HARVEST_ENCHANTMENTS.containsKey(stack);
  }

  /** Records enchantments prepared before vanilla copies the tool for block loot. */
  static void storeActiveHarvestEnchantments(ItemStack stack, ItemEnchantments originalTag) {
    ACTIVE_HARVEST_ENCHANTMENTS.put(stack, originalTag);
  }

  /** Gets and removes a stored enchantment backup for restoration after vanilla block breaking finishes. */
  @Nullable
  static ItemEnchantments popActiveHarvestEnchantments(ItemStack stack) {
    return ACTIVE_HARVEST_ENCHANTMENTS.remove(stack);
  }

  /**
   * Restores the enchantment component saved by {@link #updateHarvestEnchantments(IToolStackView, ItemStack, ToolHarvestContext)}.
   * The baked snapshot is intentionally left in place, as vanilla still has to compute loot from a copy taken while the tool was baked.
   */
  static void restoreEnchantments(ItemStack stack, ItemEnchantments originalTag) {
    EnchantmentHelper.setEnchantments(stack, originalTag);
  }

  record AllMerger(Collection<HarvestEnchantmentsModifierHook> modules) implements HarvestEnchantmentsModifierHook {
    @Override
    public void updateHarvestEnchantments(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context, EquipmentContext equipment, EquipmentSlot slot, Map<Enchantment,Integer> map) {
      for (HarvestEnchantmentsModifierHook module : modules) {
        module.updateHarvestEnchantments(tool, modifier, context, equipment, slot, map);
      }
    }
  }
}