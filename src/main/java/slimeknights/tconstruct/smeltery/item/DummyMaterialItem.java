package slimeknights.tconstruct.smeltery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.tconstruct.TConstruct;

import java.util.function.Consumer;

/** Item for creating casts that looks like a tool part */
public class DummyMaterialItem extends Item {
  private static final Component DUMMY_TOOL_PART = TConstruct.makeTranslation("item", "dummy_tool_part.tooltip").withStyle(ChatFormatting.GRAY);
  public DummyMaterialItem(Properties pProperties) {
    super(pProperties);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(DUMMY_TOOL_PART);
  }
}
