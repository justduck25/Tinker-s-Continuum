package slimeknights.tconstruct.library.tools.definition.module.build;

import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

/** Callback used by addons to contribute volatile tool data during rebuilds. */
@FunctionalInterface
public interface ToolVolatileDataHook {
  void addVolatileData(IToolContext context, ToolDataNBT volatileData);
}
