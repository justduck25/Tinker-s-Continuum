package slimeknights.tconstruct.library;

import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.registries.RegisterEvent;
import slimeknights.tconstruct.TConstruct;

/** Custom transform types used for tinkers item rendering */
public class TinkerItemDisplays {
  private TinkerItemDisplays() {}

  public static void init() {
    TConstruct.MOD_EVENT_BUS.addListener(TinkerItemDisplays::registerDisplay);
  }

  /** Used by the melter and smeltery for display of items its melting */
  public static ItemDisplayContext MELTER = create("melter", ItemDisplayContext.NONE);
  /** Used by the part builder, crafting station, tinkers station, and tinker anvil */
  public static ItemDisplayContext TABLE = create("table", ItemDisplayContext.NONE);
  /** Used by the casting table for item rendering */
  public static ItemDisplayContext CASTING_TABLE = create("casting_table", ItemDisplayContext.FIXED);
  /** Used by the casting basin for item rendering */
  public static ItemDisplayContext CASTING_BASIN = create("casting_basin", ItemDisplayContext.NONE);
  /** Used by the fluid cannon for display of the item in front */
  public static ItemDisplayContext FLUID_CANNON = create("fluid_cannon", ItemDisplayContext.FIXED);
  /** Used by throwing to allow adjusting the tool position */
  public static ItemDisplayContext THROWN = create("thrown", ItemDisplayContext.FIXED);

  /** Creates a transform type */
  private static ItemDisplayContext create(String name, ItemDisplayContext fallback) {
    return switch (name) {
      case "melter" -> TinkerItemDisplayProxies.MELTER.getValue();
      case "table" -> TinkerItemDisplayProxies.TABLE.getValue();
      case "casting_table" -> TinkerItemDisplayProxies.CASTING_TABLE.getValue();
      case "casting_basin" -> TinkerItemDisplayProxies.CASTING_BASIN.getValue();
      case "fluid_cannon" -> TinkerItemDisplayProxies.FLUID_CANNON.getValue();
      case "thrown" -> TinkerItemDisplayProxies.THROWN.getValue();
      default -> fallback;
    };
  }

  /** Registers all item display types */
  private static void registerDisplay(RegisterEvent event) {
  }
}
