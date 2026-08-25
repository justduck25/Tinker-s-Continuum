# Powah Compat Tasks

Scope: Tinker's Continuum / TConstruct 26.1.2 compat with Powah 7.0.4 alpha.

Keep compat data gated behind filled common tags or `neoforge:mod_loaded` for `powah`. Prefer datagen/data JSON first; only add Java hooks if a feature cannot be expressed cleanly in data.

## Phase 1: Survey And Baseline

- [x] Confirm Powah mod id is `powah`.
- [x] Confirm current Powah item ids:
  - `powah:steel_energized`
  - `powah:crystal_blazing`
  - `powah:crystal_niotic`
  - `powah:crystal_spirited`
  - `powah:crystal_nitro`
  - `powah:uraninite`
  - `powah:uraninite_raw`
- [x] Confirm Powah provides broad common tags:
  - `c:gems`
  - `c:ingots`
  - `c:raw_materials/uraninite`
  - `c:storage_blocks/uraninite`
- [x] Add missing fine-grained optional common tags needed by TCon material recipes.

## Phase 2: Tool-Part Materials

- [x] Add TCon material ids:
  - Energized Steel
  - Blazing Crystal
  - Niotic Crystal
  - Spirited Crystal
  - Nitro Crystal
  - Uraninite
- [x] Gate materials behind filled common tags.
- [x] Add first-pass melee/harvest, ranged, armor, and ammo stats.
- [x] Add first-pass traits by Powah theme.
- [x] Add material recipes from ingot, gem, raw material, and storage block tags.
- [x] Add material render colors and client sprite palettes.
- [x] Add English material names/descriptions.
- [x] Run datagen/client-data and verify generated material JSON/textures.
  - `compileJava` passed.
  - `runData` passed; generated material definitions, stats, traits, and material recipes.
  - `runClientData` passed; generated material render JSON and part textures.
- [ ] Manual test in Part Builder/Tinker Station: all six materials appear, craft into valid parts, and show correct traits.

## Phase 3: Smeltery And Processing

- [ ] Decide whether Powah crystals should melt into existing TCon fluids or stay item-only.
- [ ] Add Uraninite ore/raw melting only if it maps cleanly without duplicating Powah progression.
- [ ] Add casting recipes only for materials with a real molten-fluid mapping.
- [ ] Check JEI visibility for all Powah compat recipes with Powah installed and absent.

## Notes

- Powah 7.0.4 is alpha, so keep compat conservative.
- Avoid adding energy-storage runtime behavior to TCon tools unless there is a clear gameplay design.
- Nitro is intentionally strong but should not become a free best-in-slot material without slot or progression cost.
