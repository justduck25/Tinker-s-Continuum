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

  void updateHarvestEnchantments(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context, EquipmentContext equipment, EquipmentSlot slot, Map<Enchantment,Integer> map);

  @Nullable
  static ItemEnchantments updateHarvestEnchantments(IToolStackView tool, ItemStack stack, ToolHarvestContext context) {
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
        return originalEnchants;
      }
    }
    return null;
  }

  /** Finds the registry holder for the enchantment, as vanilla loot checks registry holders rather than direct holders. */
  static Holder<Enchantment> registryHolder(ToolHarvestContext context, Enchantment enchantment) {
    return context.getWorld().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                  .filter(holder -> holder.value() == enchantment)
                  .findFirst()
                  .map(holder -> (Holder<Enchantment>)holder)
                  .orElseGet(() -> Holder.direct(enchantment));
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