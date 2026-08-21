package slimeknights.tconstruct.tools.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Tool;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

import java.util.List;

public class ModifiableSwordItem extends ModifiableItem {
  private static Properties swordProperties(Properties properties) {
    return properties.component(DataComponents.TOOL, new Tool(List.of(), 1.0f, 0, false));
  }

  public ModifiableSwordItem(Properties properties, ToolDefinition toolDefinition) {
    super(swordProperties(properties), toolDefinition);
  }

  public ModifiableSwordItem(Properties properties, ToolDefinition toolDefinition, int maxStackSize) {
    super(swordProperties(properties), toolDefinition, maxStackSize);
  }

}
