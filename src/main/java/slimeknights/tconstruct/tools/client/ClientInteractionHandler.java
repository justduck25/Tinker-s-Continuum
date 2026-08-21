package slimeknights.tconstruct.tools.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.tools.logic.InteractionHandler;
import slimeknights.tconstruct.tools.network.InteractWithAirPacket;

/**
 * Client side interaction hooks
 */
public class ClientInteractionHandler {
  /** If true, next offhand interaction should be canceled, used since we cannot tell Forge to break the hand loop from the main hand */
  private static boolean cancelNextOffhand = false;

  /** Implements client side chestplate empty-air use. */
  @SubscribeEvent(priority = EventPriority.LOW)
  static void chestplateToolUse(PlayerInteractEvent.RightClickEmpty event) {
    Player player = event.getEntity();
    ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
    if (!player.isSpectator() && chestplate.is(TinkerTags.Items.INTERACTABLE_ARMOR)) {
      InteractionHand hand = event.getHand();
      TinkerNetwork.getInstance().sendToServer(InteractWithAirPacket.fromChestplate(hand));
      InteractionResult result = InteractionHandler.onChestplateUse(player, chestplate, hand);
      if (result.consumesAction()) {
        swingAndUse(player, hand, result);
        if (hand == InteractionHand.MAIN_HAND) {
          cancelNextOffhand = true;
        }
      }
    }
  }

  /** Prevents an empty right click from running the offhand */
  @SubscribeEvent(priority = EventPriority.HIGH)
  static void preventDoubleInteract(InputEvent.InteractionKeyMappingTriggered event) {
    if (cancelNextOffhand) {
      cancelNextOffhand = false;
      if (event.getHand() == InteractionHand.OFF_HAND) {
        event.setCanceled(true);
        event.setSwingHand(false);
      }
    }
  }

  /** Implements client side left click empty-air interaction. */
  @SubscribeEvent
  static void leftClickAir(PlayerInteractEvent.LeftClickEmpty event) {
    Player player = event.getEntity();
    ItemStack tool = event.getItemStack();
    if (!player.isSpectator() && tool.is(TinkerTags.Items.INTERACTABLE_LEFT)) {
      InteractionHand hand = event.getHand();
      TinkerNetwork.getInstance().sendToServer(InteractWithAirPacket.LEFT_CLICK);
      InteractionResult result = InteractionHandler.onLeftClickInteraction(player, tool, hand);
      if (result.consumesAction()) {
        swingAndUse(player, hand, result);
      }
    }
  }

  private static void swingAndUse(Player player, InteractionHand hand, InteractionResult result) {
    if (result instanceof InteractionResult.Success success && success.swingSource() != InteractionResult.SwingSource.NONE) {
      player.swing(hand);
    }
    Minecraft.getInstance().gameRenderer.itemInHandRenderer.itemUsed(hand);
  }
}
