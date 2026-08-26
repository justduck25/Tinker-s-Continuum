# Compat Candidates

Notes from scanning the PrismLauncher 26.1.2 instance mods folder:

`C:\Users\trong\AppData\Roaming\PrismLauncher\instances\26.1.2\minecraft\mods`

The folder currently has 140 jar files, 139 enabled and 1 disabled.

## Already Supported Or In Progress

- Apotheosis / Apothic Enchanting
  - Existing TCon modifier and affix compat is already implemented.
  - Keep testing post-cap enchant recipes, tooltip requirements, and loot affix behavior.
- EnderIO
  - Existing molten fluid, alloy, material, and tool-part compat is implemented.
  - Keep testing smeltery alloying, casting, material stats, traits, and tool textures.
- Jade
  - Existing Jade plugin compat is implemented.
  - Keep testing tooltip/provider display on TCon blocks and fluids.
- JEI
  - Existing recipe visibility compat is present.
  - Keep testing conditional recipe visibility with optional mods installed/uninstalled.

## High Priority

### Applied Energistics 2 Family

Mods:

- `appliedenergistics2`
- `AdvancedAE`
- `ExtendedAE`

Potential compat:

- Add TCon materials for AE2 crystals and alloys: **implemented**
  - Certus Quartz
  - Fluix
  - Entro materials from AdvancedAE/ExtendedAE family
  - Quantum Alloy from Advanced AE
- Add tool-part materials for relevant crystal/alloy items: **implemented**
- Consider modifier recipes from Advanced AE cards: **partially implemented**
  - `attack_speed_card` -> attack speed style modifier
  - `reach_card` -> reach style modifier
  - `luck_card` -> luck modifier
  - `strength_card` -> damage modifier
  - `magnet_card` -> magnetic modifier
  - movement cards -> armor/travel modifiers if item balance is reasonable
  - `flight_card` -> wings
  - `jump_height_card` -> leaping
  - `water_breathing_card` -> respiration
  - `swim_speed_card` -> depth strider
  - `lava_immunity_card` -> fire protection
  - `hp_buffer_card` / `regeneration_card` -> revitalizing

Skipped for now:

- `night_vision_card`, `camo_card`, `recharging_card`, `portable_workbench_card`, `pick_craft_card`, and grid/inventory automation cards do not have a close TCon modifier equivalent without adding runtime logic.

Notes:

- This is probably the cleanest next compat target because AE2 has obvious materials and tools.
- Watch balance: AE2 materials should feel technical/crystal-based, not strictly stronger than all native TCon materials.

### Powah

Potential compat:

- Add materials or modifiers around the Powah tier chain:
  - Energized Steel
  - Blazing Crystal
  - Niotic Crystal
  - Spirited Crystal
  - Nitro Crystal
  - Uraninite
- Add smeltery/melting/casting support where items behave like ingots/crystals.
- Consider high-tier energy-themed traits for advanced materials.

Notes:

- Good second target after AE2.
- Strong identity and clear progression, but needs careful balance so Nitro does not become a free best-in-slot material.

### World Loot Integration

Mods:

- Terralith
- Dungeons and Taverns
- YUNG's structure mods
- Lootr

Potential compat:

- Inject low-rate TCon tools and materials into structure loot tables: **implemented**
- Use the existing balanced loot modifier pools: **implemented**
  - Only 2-3 random modifiers per generated tool.
  - Pick modifiers from a pool appropriate for each tool type.
- Add loot by structure theme: **implemented**
  - Smith/fortified loot -> metal tools, repair kits, casts
  - Dungeon loot -> weapons and armor
  - Mage/rare loot -> slime/crystal/ability-themed modifiers
  - Village loot -> low-tier tools and basic materials

Notes:

- High player-facing value.
- Avoid flooding loot tables or making TCon gear too common.
- Lootr itself does not need direct loot table entries; it should inherit injected table contents from the underlying structure loot tables.

### Biomes O' Plenty

Potential compat:

- Add smeltery/casting recipes for special blocks/items where sensible.
- Investigate materials from:
  - Brimstone
  - Blood fluid/bucket
  - Special biome blocks or gems if present
- Add wood/plank recipe compat for BOP wood families where TCon recipes reference vanilla wood families.

Notes:

- Good environmental compat.
- Needs inspection of BOP tags and item registry before choosing material candidates.

## Medium Priority

### Productive Bees

Potential compat:

- Process combs/honey items into molten materials or nuggets where the mod already represents metals/resources.
- Add bee-product smeltery recipes carefully.

Notes:

- Can become overpowered quickly if comb processing duplicates ore processing too efficiently.
- Needs strict output rates and mod-loaded conditions.

### Cyclic

Potential compat:

- Add material support for Amethyst tools if Cyclic items/tags are stable.
- Consider modifier recipes from charms:
  - attack speed
  - luck
  - knockback resistance
  - speed
  - fire
  - long fall
- Investigate fluids/items such as biomass for smeltery recipes.

Notes:

- Cyclic has many mixed utility items, so compat should be narrow and curated.

### Artifacts / Curios

Potential compat:

- Armor/tool interactions with Curios slots or attributes.
- Possible salvage/recipe bridge for artifact-like modifiers.

Notes:

- Likely code-heavy compared to datapack/material compat.
- Do after material and loot compat targets.

## Low Priority Or No Direct Compat Needed

Mostly client, library, config, UI, optimization, or dependency mods:

- Sodium
- Sodium Extra
- Reese's Sodium Options
- Iris
- Lithium
- C2ME
- FerriteCore
- Entity Culling
- Entity Model Features
- Entity Texture Features
- Mouse Tweaks
- Better Advancements
- Advancement Plaques
- AppleSkin
- Balm
- Cloth Config
- YetAnotherConfigLib
- KotlinForForge
- CreativeCore
- Geckolib
- PuzzlesLib
- ResourcefulLib
- FTB Library
- FTB Teams
- FTB Backups
- FTB Chunks
- FTB Ultimine
- JEI Optimizer
- Pipez Lag Fix
- ThreadTweak
- BadOptimizations
- Shulker Box Tooltip
- PickUpNotifier
- Enchantment Descriptions
- Better Stats
- Chat Animation
- Ambient Sounds
- Sounds
- Visuality
- Not Enough Animations

These should only get compat if a specific bug or integration request appears.

## Suggested Order

1. AE2 family material and modifier compat.
2. Powah material and smeltery compat.
3. Terralith / Dungeons and Taverns / YUNG loot table compat.
4. Biomes O' Plenty smeltery/material/wood compat.
5. Productive Bees comb processing compat.
6. Cyclic curated modifier/material compat.
7. Artifacts / Curios code-side integration.
