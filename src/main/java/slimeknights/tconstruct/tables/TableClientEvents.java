package slimeknights.tconstruct.tables;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

import net.neoforged.fml.common.EventBusSubscriber;
import slimeknights.mantle.client.render.InventoryBlockEntityRenderer;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.tables.client.inventory.CraftingStationScreen;
import slimeknights.tconstruct.tables.client.inventory.ModifierWorktableScreen;
import slimeknights.tconstruct.tables.client.inventory.PartBuilderScreen;
import slimeknights.tconstruct.tables.client.inventory.TinkerChestScreen;
import slimeknights.tconstruct.tables.client.inventory.TinkerStationScreen;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class TableClientEvents extends ClientEventBase {

  @SuppressWarnings({"rawtypes", "unchecked"})
  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    BlockEntityRendererProvider tableRenderer = InventoryBlockEntityRenderer::new;
    event.registerBlockEntityRenderer(TinkerTables.craftingStationTile.get(), tableRenderer);
    event.registerBlockEntityRenderer(TinkerTables.tinkerStationTile.get(), tableRenderer);
    event.registerBlockEntityRenderer(TinkerTables.modifierWorktableTile.get(), tableRenderer);
    event.registerBlockEntityRenderer(TinkerTables.partBuilderTile.get(), tableRenderer);
  }

  @SubscribeEvent
  static void registerScreens(final RegisterMenuScreensEvent event) {
    event.register(TinkerTables.craftingStationContainer.get(), CraftingStationScreen::new);
    event.register(TinkerTables.tinkerStationContainer.get(), TinkerStationScreen::new);
    event.register(TinkerTables.partBuilderContainer.get(), PartBuilderScreen::new);
    event.register(TinkerTables.modifierWorktableContainer.get(), ModifierWorktableScreen::new);
    event.register(TinkerTables.tinkerChestContainer.get(), TinkerChestScreen::new);
  }

    /** Registers the world tint for the dyeable Tinker Chest block. */
  @SubscribeEvent
  static void registerBlockColors(final RegisterColorHandlersEvent.BlockTintSources event) {
    event.register(List.of(new BlockTintSource() {
      @Override
      public int color(BlockState state) {
        return -1;
      }

      @Override
      public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof slimeknights.tconstruct.tables.block.entity.chest.TinkersChestBlockEntity chest) {
          return chest.getColor();
        }
        return -1;
      }
    }), TinkerTables.tinkersChest.get());
  }

  /*
   * Item tinting is data-driven in NeoForge 26.1. The Tinker Chest client item
   * definition uses vanilla minecraft:dye, which reads DataComponents.DYED_COLOR.
   */

}
