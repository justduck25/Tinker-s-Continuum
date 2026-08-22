package slimeknights.tconstruct.plugin.jsonthings;

/** Handles anything that requires clientside class loading. */
public class PluginClient {
  private PluginClient() {}

  /**
   * JsonThings 0.18.x no longer exposes the old per-item ItemColorHandler API.
   * Tcon4 registers item model/tint behavior through its global NeoForge client events.
   */
  public static void init() {
    // No JsonThings-specific client registration is required on NeoForge 26.1.
  }
}
