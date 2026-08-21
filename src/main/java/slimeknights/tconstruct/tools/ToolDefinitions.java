package slimeknights.tconstruct.tools;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

import static slimeknights.tconstruct.TConstruct.getResource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToolDefinitions {
  // rock
  public static final ToolDefinition PICKAXE = ToolDefinition.create(getResource("pickaxe"));
  public static final ToolDefinition SLEDGE_HAMMER = ToolDefinition.create(getResource("sledge_hammer"));
  public static final ToolDefinition VEIN_HAMMER = ToolDefinition.create(getResource("vein_hammer"));

  // dirt
  public static final ToolDefinition MATTOCK = ToolDefinition.create(getResource("mattock"));
  public static final ToolDefinition PICKADZE = ToolDefinition.create(getResource("pickadze"));
  public static final ToolDefinition EXCAVATOR = ToolDefinition.create(getResource("excavator"));

  // wood
  public static final ToolDefinition HAND_AXE = ToolDefinition.create(getResource("hand_axe"));
  public static final ToolDefinition BROAD_AXE = ToolDefinition.create(getResource("broad_axe"));

  // scythes
  public static final ToolDefinition KAMA = ToolDefinition.create(getResource("kama"));
  public static final ToolDefinition SCYTHE = ToolDefinition.create(getResource("scythe"));
  // swords
  public static final ToolDefinition DAGGER = ToolDefinition.create(getResource("dagger"));
  public static final ToolDefinition SWORD = ToolDefinition.create(getResource("sword"));
  public static final ToolDefinition CLEAVER = ToolDefinition.create(getResource("cleaver"));

  // ranged
  public static final ToolDefinition CROSSBOW = ToolDefinition.create(getResource("crossbow"));
  public static final ToolDefinition LONGBOW = ToolDefinition.create(getResource("longbow"));
  public static final ToolDefinition FISHING_ROD = ToolDefinition.create(getResource("fishing_rod"));
  public static final ToolDefinition JAVELIN = ToolDefinition.create(getResource("javelin"));
  // ammo
  public static final ToolDefinition ARROW = ToolDefinition.create(getResource("arrow"));
  public static final ToolDefinition SHURIKEN = ToolDefinition.create(getResource("shuriken"));
  public static final ToolDefinition THROWING_AXE = ToolDefinition.create(getResource("throwing_axe"));

  // special
  public static final ToolDefinition FLINT_AND_BRICK = ToolDefinition.create(getResource("flint_and_brick"));
  public static final ToolDefinition SKY_STAFF = ToolDefinition.create(getResource("sky_staff"));
  public static final ToolDefinition EARTH_STAFF = ToolDefinition.create(getResource("earth_staff"));
  public static final ToolDefinition ICHOR_STAFF = ToolDefinition.create(getResource("ichor_staff"));
  public static final ToolDefinition ENDER_STAFF = ToolDefinition.create(getResource("ender_staff"));

  // ancient
  public static final ToolDefinition MELTING_PAN = ToolDefinition.create(getResource("melting_pan"));
  public static final ToolDefinition WAR_PICK = ToolDefinition.create(getResource("war_pick"));
  public static final ToolDefinition BATTLESIGN = ToolDefinition.create(getResource("battlesign"));
  public static final ToolDefinition SWASHER = ToolDefinition.create(getResource("swasher"));
  public static final ToolDefinition MINOTAUR_AXE = ToolDefinition.create(getResource("minotaur_axe"));
}
