package slimeknights.tconstruct.plugin.craftingtweaks;

import net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI;

public final class CraftingTweaksPlugin {
  private CraftingTweaksPlugin() {}

  public static void onConstruct() {
    CraftingTweaksAPI.registerCraftingGridProvider(new TinkersCraftingGridProvider());
  }
}
