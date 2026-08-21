package slimeknights.tconstruct.library.tools.context;

import lombok.Data;
import lombok.With;
import net.minecraft.world.item.Item;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

/**
 * Implementation of the limited view of {@link IToolStackView} for use in tool rebuild hooks
 */
public class ToolRebuildContext implements IToolContext {
  /** Item being rebuilt */
  private final Item item;
  /** Tool definition of the item being rebuilt */
  private final ToolDefinition definition;
  /** Materials on the tool being rebuilt */
  private final MaterialNBT materials;
  /** List of recipe modifiers on the tool being rebuilt */
  private final ModifierNBT upgrades;
  /** List of all modifiers on the tool being rebuilt, from recipes and traits */
  private final ModifierNBT modifiers;
  /** Persistent modifier data, intentionally read only */
  private final IModDataView persistentData;

  public ToolRebuildContext(Item item, ToolDefinition definition, MaterialNBT materials, ModifierNBT upgrades, ModifierNBT modifiers, IModDataView persistentData) {
    this.item = item;
    this.definition = definition;
    this.materials = materials;
    this.upgrades = upgrades;
    this.modifiers = modifiers;
    this.persistentData = persistentData;
  }

  @Override
  public Item getItem() { return item; }
  @Override
  public ToolDefinition getDefinition() { return definition; }
  public MaterialNBT getMaterials() { return materials; }
  public ModifierNBT getUpgrades() { return upgrades; }
  public ModifierNBT getModifiers() { return modifiers; }
  @Override
  public IModDataView getPersistentData() { return persistentData; }

  /** Returns a copy with new modifiers */
  public ToolRebuildContext withModifiers(ModifierNBT newModifiers) {
    return new ToolRebuildContext(item, definition, materials, upgrades, newModifiers, persistentData);
  }
}
