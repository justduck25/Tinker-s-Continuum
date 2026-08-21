package slimeknights.tconstruct.library;

import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/** Enum extension proxies for custom item display contexts. */
public class TinkerItemDisplayProxies {
  private TinkerItemDisplayProxies() {}

  public static final EnumProxy<ItemDisplayContext> MELTER = new EnumProxy<>(ItemDisplayContext.class, -1, "tconstruct:melter", null);
  public static final EnumProxy<ItemDisplayContext> TABLE = new EnumProxy<>(ItemDisplayContext.class, -1, "tconstruct:table", null);
  public static final EnumProxy<ItemDisplayContext> CASTING_TABLE = new EnumProxy<>(ItemDisplayContext.class, -1, "tconstruct:casting_table", "FIXED");
  public static final EnumProxy<ItemDisplayContext> CASTING_BASIN = new EnumProxy<>(ItemDisplayContext.class, -1, "tconstruct:casting_basin", null);
  public static final EnumProxy<ItemDisplayContext> FLUID_CANNON = new EnumProxy<>(ItemDisplayContext.class, -1, "tconstruct:fluid_cannon", "FIXED");
  public static final EnumProxy<ItemDisplayContext> THROWN = new EnumProxy<>(ItemDisplayContext.class, -1, "tconstruct:thrown", "FIXED");
}
