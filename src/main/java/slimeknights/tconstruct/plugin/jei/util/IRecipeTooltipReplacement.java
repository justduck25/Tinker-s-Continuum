package slimeknights.tconstruct.plugin.jei.util;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.network.chat.FormattedText;
import javax.annotation.Nullable;
import java.util.List;

/** @deprecated use {@link FluidTooltipCallback} for better handling of advanced tooltip information. */
@Deprecated(forRemoval = true)
@FunctionalInterface
public interface IRecipeTooltipReplacement extends IRecipeSlotRichTooltipCallback {
  IRecipeTooltipReplacement EMPTY = (slot, tooltip) -> {};

  @Override
  default void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
    List<Either<FormattedText, TooltipComponent>> lines = tooltip.getLines();
    if (!lines.isEmpty()) {
      Either<FormattedText, TooltipComponent> name = lines.get(0);
      lines.clear();
      lines.add(name);
    }
    addMiddleLines(recipeSlotView, tooltip);
  }

  /** Adds the lines between the name and mod ID. */
  void addMiddleLines(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip);
}