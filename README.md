# Continuum Construct

[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-blue.svg)](https://minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.1.2.112+-orange.svg)](https://neoforged.net/)
[![Continuum Core](https://img.shields.io/badge/Continuum%20Core-1.12.3-purple.svg)](../Mantle)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> A modernized, high-performance NeoForge 26.1 port of the quintessential modular tool, metallurgy, and smeltery mod.

**Continuum Construct** is the community-maintained port of **Tinkers' Construct** (v3) targeting **Minecraft 26.1 / NeoForge 26.1**. It preserves the beloved core design—modular tools, custom materials, multi-block smelteries, modifier customization, and fluid casting—while modernizing client rendering, data structures, and cross-mod compatibility for modern Minecraft versions.

> [!NOTE]
> **Mod ID & Architecture:** The technical mod ID `tconstruct` and Java package `slimeknights.tconstruct` are intentionally preserved for full world save, resource pack, and datapack interoperability. Continuum Construct depends directly on **Continuum Core** (`mantle`). This is a community fork and not an official SlimeKnights release.

---

## 🎯 Port Target & Compatibility Matrix

| Component | Target Version | Supported Range |
|---|---|---|
| **Minecraft** | `26.1.2` | `[26.1, 26.2)` |
| **NeoForge** | `26.1.2.112` | `[26.1.2.78, 26.2)` |
| **Continuum Core** | `1.12.3` | `[1.12.3, 1.13)` |
| **Mod Version** | `3.12.7` | NeoForge 26.1 Target |
| **Java Toolchain** | Java 25 | JDK 21+ required to run Gradle |
| **Gradle** | `9.1` | Wrapper included (`gradlew.bat`) |

---

## ⚔️ Key Features & Systems

### 1. Modular Tool & Weapon Smithing
* **Part Building & Tool Assembly**: Craft individual components (heads, handles, bindings, guards) from diverse materials, each imparting unique baseline stats and intrinsic material traits.
* **Extensive Weapon & Tool Arsenal**:
  * *Harvest & Utility*: Pickaxes, mattocks, pickadzes, shovels, excavators, hand axes, broad axes, scythes, kamas, fishing rods, flint and brick.
  * *Melee & Combat*: Broadswords, cleavers, daggers, war picks, battlesigns, swashers, minotaur axes.
  * *Ranged & Projectiles*: Crossbows, longbows, javelins, slime staves (Earth, Sky, Ichor, Ender).
  * *Specialized Tools*: Melting pan, piggybackpack, and modifier worktables.
* **Slot-Based Modifier System**: Customise equipment via Upgrades, Abilities, Defense slots, and Soul traits. Apply abilities such as Silk Touch, Luck/Fortune, Autosmelt, Expanding, Bludgeoning, and more.

### 2. Modular Armor & 3D Elytra Wings
* **Armor Sets**: Traveler's Gear (lightweight mobility), Plate Armor (heavy forged protection), Slimesuit, and Slimelytra.
* **Modern Equipment Layer System**: Built directly on Minecraft 26.1's `EquipmentLayerRenderer` and `EquipmentClientInfo` with a multi-layered model definition (`tinker_armor.json`, `slime_wings.json`).
* **Visual Elytra Wings**: Armor with the `wings` modifier renders fully functional, animated 3D wings on the wearer's back using armor-specific textures (`cuirass_wings`, `maille_wings`, `slime/wings`), with automatic vanilla armor trim suppression on wings to prevent missing-texture artifacts.
* **Armor Overlays & Volatile Flags**: Dynamic overlays for armor modifiers (golden trims, boots, plates, slime helmets), alongside volatile capability flags (powder snow walking, enderman mask prevention, elytra gliding).

### 3. Smeltery, Foundry & Metallurgy
* **Multi-block Smeltery & Foundry**: Highly configurable structures built from seared and scorched bricks. Melt down ores, ingots, equipment, and even entities into molten fluids with high capacity.
* **Alloying & Casting**: Form molten alloys (Manyullyn, Hepatizon, Bronze, Queen's Slime, Slimesteel, Rose Gold, etc.) in the smeltery or standalone alloyer. Pour liquid metals via faucets and channels onto casting tables (casts/molds) or casting basins.
* **Liquid Pipeline**: Compatible with fluid tanks, gauges, ducting, and transparent molten fluid rendering powered by Continuum Core's `FluidRenderer`.

### 4. Slime Islands & Dimensional Gadgets
* **Floating Slime Islands**: Naturally generating in the Overworld, Nether, and End across 5 slime varieties: Earth, Sky, Blood, Ichor, and Ender slime.
* **Slime Fauna & Flora**: Slime grass, dirt, foliage, saplings, and unique slime mobs.
* **Gadgetry & Mobility**: Slimesling and Bouncy Boots for ultra-high velocity traversal, pig backpacks, and dynamic slime balls.

### 5. Interactive In-Game Guidebooks
* Fully integrated with Continuum Core's modern book engine, featuring dynamic 3D isometric structure previews and asynchronous page caching to prevent UI freeze:
  1. *Materials and You* — Foundations of tool crafting and basic materials.
  2. *Puny Smelting* — Getting started with melters, casting, and basic foundry work.
  3. *Mighty Smelting* — Advanced multiblock smeltery construction, alloying, and metallurgy.
  4. *Tinkers' Gadgetry* — Exploring slime gear, gadgets, and island treasures.
  5. *Fantastic Foundry* — Scorched stone, foundry byproducts, and industrial casting.
  6. *Encyclopedia of Tinkering* — Comprehensive compendium of all materials, modifiers, and mechanics.
* **Complete Localization**: Upstream-synchronized translations across 11 languages (`zh_cn`, `ja_jp`, `ru_ru`, `es_mx`, `uk_ua`, `pt_br`, `fr_fr`, `de_de`, `ko_kr`, `it_it`, `lzh`), plus a comprehensive, fully polished **Vietnamese (`vi_vn`)** localization covering books, tooltips, and modifiers.

---

## 🔌 Mod Integrations

Continuum Construct includes built-in integrations for popular modern NeoForge mods:

| Mod | Version | Integration Details |
|---|---|---|
| **JEI (Just Enough Items)** | `29.34.0.90+` | Full recipe categories for casting, melting, alloying, entity melting, modifiers, part builder, and worktable. |
| **Apotheosis / Placebo** | `9.0.3+` / `10.0.2+` | Modifier and enchantment bridge, affix handling, loot category mapping, and post-cap stat calculations. |
| **JsonThings** | `0.18.2+` | Flex item/block extension support for datapack creators. |
| **Jade** | `26.0.0+` | Real-time in-world tooltips for fluid tanks, casting tables, basins, and multiblock structures. |

---

## 🏗️ Building from Source

### Prerequisites
* **Git** installed and accessible from command line.
* **JDK 21** or higher installed (Gradle automatically provisions Java 25 via toolchain).
* Active internet connection for Gradle and NeoForge artifact resolution.
* **Continuum Core JAR**: Continuum Construct consumes the compiled Core JAR from `../Mantle/build/libs/`.

### Step-by-Step Build Instructions

1. **Build Continuum Core first**:
   ```powershell
   cd ..\Mantle
   .\gradlew.bat assemble
   cd ..\Tcon4
   ```

2. **Build and Test Continuum Construct**:
   ```powershell
   # Compile Java sources
   .\gradlew.bat compileJava

   # Run test suite
   .\gradlew.bat test

   # Run datagen (generates recipes, tags, loot tables, and client assets)
   .\gradlew.bat runData
   .\gradlew.bat runClientData

   # Launch the NeoForge development client
   .\gradlew.bat runClient
   ```

Build artifacts will be generated in `build/libs/`.

> [!WARNING]
> Do not manually edit files in `src/generated/`. All files in this directory are generated via `runData` and `runClientData`.

---

## 🐛 Issue Reporting & Feedback

When opening an issue or reporting unexpected behavior, please include:
1. **Versions**: Exact Minecraft (`26.1.2`), NeoForge build (e.g. `26.1.2.112`), and Continuum Construct commit hash.
2. **Environment**: Singleplayer client, integrated server, or dedicated server.
3. **Mod List**: Other installed mods and their versions (especially if related to rendering or mechanics).
4. **Logs & Crash Reports**: Provide the full `logs/latest.log` or crash report from `crash-reports/`.
5. **Reproduction Steps**: Clear, numbered steps to reproduce the issue, along with in-game screenshots if visual.

---

## 📜 Credits and License

* **Original Creators**: Tinkers' Construct was originally created by **mDiyo** and actively maintained/evolved by the **[SlimeKnights](https://github.com/SlimeKnights)** team (fuj1n, KnightMiner, progwml6, boni, alexbegt, and many community contributors).
* **Port Maintainer**: Maintained for NeoForge 26.1 by **justduck** under the public name **Continuum Construct**.
* **License**: Code, textures, and assets are licensed under the [MIT License](LICENSE), unless explicitly noted otherwise in individual source files or asset metadata.
* **Modpack Usage**: You are free to use Continuum Construct in public or private modpacks.
