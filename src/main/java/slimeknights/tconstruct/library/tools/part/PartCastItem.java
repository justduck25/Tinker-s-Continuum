package slimeknights.tconstruct.library.tools.part;

import net.minecraft.world.item.Item;
import slimeknights.tconstruct.TConstruct;

import java.util.function.Supplier;

/** Item which shows the cast cost in the tooltip */
public class PartCastItem extends Item {
  public static final String COST_KEY = TConstruct.makeTranslationKey("item", "cast.cost");

  private final Supplier<? extends IMaterialItem> part;
  public PartCastItem(Properties props, Supplier<? extends IMaterialItem> part) {
    super(props);
    this.part = part;
  }

}
