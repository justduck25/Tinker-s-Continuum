package slimeknights.tconstruct.common;

import net.minecraft.world.level.block.Block;
import net.minecraft.client.color.block.BlockColors;
import slimeknights.mantle.registration.object.EnumObject;

import java.util.function.Supplier;

/**
 * Contains helpers to use for registering client events
 */
public abstract class ClientEventBase {
  /**
   * Registers a block colors alias for the given block
   */
  protected static void registerBlockItemColorAlias(BlockColors blockColors, Block block) {
  }

  /**
   * Registers a block colors alias for the given block suppliers
   */
  protected static void registerBlockItemColorAlias(BlockColors blockColors, Supplier<? extends Block> block) {
    registerBlockItemColorAlias(blockColors, block.get());
  }

  /**
   * Registers a block colors alias for all blocks in the given instance
   */
  protected static <B extends Block> void registerBlockItemColorAlias(BlockColors blockColors, EnumObject<?,B> blocks) {
    for (B block : blocks.values()) {
      registerBlockItemColorAlias(blockColors, block);
    }
  }
}
