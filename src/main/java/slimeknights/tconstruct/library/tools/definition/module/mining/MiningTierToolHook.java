package slimeknights.tconstruct.library.tools.definition.module.mining;

import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.Collection;

/** Hook to adjust the harvest tier in a tool */
public interface MiningTierToolHook {
  /** Updates the tier based on this logic */
  Object modifyTier(IToolStackView tool, Object tier);

  /** Gets the tier for the given tool */
  static Object getTier(IToolStackView tool) {
    return tool.getHook(ToolHooks.MINING_TIER).modifyTier(tool, tool.getStats().get(ToolStats.HARVEST_TIER));
  }

  /** Merger that runs all hooks, composing the result */
  record ComposeMerger(Collection<MiningTierToolHook> hooks) implements MiningTierToolHook {
    @Override
    public Object modifyTier(IToolStackView tool, Object tier) {
      for (MiningTierToolHook hook : hooks) {
        tier = hook.modifyTier(tool, tier);
      }
      return tier;
    }
  }
}
