package slimeknights.tconstruct.plugin.jei.util;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;

import java.util.ArrayList;
import java.util.List;

/** Helper for working with fluid tooltips */
@FunctionalInterface
public interface FluidTooltipCallback extends IRecipeSlotRichTooltipCallback {
  String AMOUNT_KEY = "jei.tooltip.liquid.amount";

  /** Default instance, simply replaces mb units with our unit handler. */
  FluidTooltipCallback UNITS = (fluid, recipeSlotView, tooltip) -> FluidTooltipHandler.appendMaterial(fluid, tooltip);
  /** Default instance, does not add an amount line. */
  FluidTooltipCallback NO_AMOUNT = (fluid, recipeSlotView, tooltip) -> {};

  @Override
  default void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
    List<Either<FormattedText, TooltipComponent>> lines = tooltip.getLines();
    for (int i = 0; i < lines.size(); i++) {
      FormattedText formattedText = lines.get(i).left().orElse(null);
      if (formattedText instanceof Component component
          && component.getContents() instanceof TranslatableContents translatable
          && AMOUNT_KEY.equals(translatable.getKey())) {
        lines.remove(i);
        FluidStack fluid = recipeSlotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
        List<Component> replacement = new ArrayList<>();
        onFluidTooltip(fluid, recipeSlotView, replacement);
        for (int j = 0; j < replacement.size(); j++) {
          lines.add(i + j, Either.left(replacement.get(j)));
        }
        return;
      }
    }
    FluidStack fluid = recipeSlotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
    List<Component> appended = new ArrayList<>();
    onFluidTooltip(fluid, recipeSlotView, appended);
    for (Component component : appended) {
      lines.add(Either.left(component));
    }
  }

  /** Adds information about the fluid to the tooltip. */
  void onFluidTooltip(FluidStack fluid, IRecipeSlotView recipeSlotView, List<Component> tooltip);
}