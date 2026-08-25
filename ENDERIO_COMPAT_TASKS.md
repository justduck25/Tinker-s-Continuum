# EnderIO Compat Tasks

Scope: Tinker's Continuum / TConstruct 26.1.2 compat with EnderIO 9.0.5 alpha.

Keep compat data gated behind filled common tags or `neoforge:mod_loaded` for `enderio` where possible. Prefer data JSON/datagen first; only add Java hooks if a feature cannot be expressed cleanly in data.

## Agent Split

- EnderIO data survey agent:
  - Read EnderIO source/data only.
  - Report exact ids, tags, fluid ids, recipe JSON shapes, conduit/facade hooks.
  - No file edits.
- TCon compat pattern agent:
  - Read TCon4 source/data only.
  - Report existing datagen classes, compat fluid patterns, conditional recipe patterns, and minimal write set.
  - No file edits.
- Coordinator:
  - Merge both reports.
  - Decide implementation order.
  - Apply edits in the main workspace after the write set is clear.
  - Run compile/datagen/client checks.

## Phase 1: Survey And Baseline

- [x] Confirm EnderIO mod id is `enderio`.
- [x] Confirm current EnderIO alloy item ids:
  - `enderio:conductive_alloy_ingot`
  - `enderio:energetic_alloy_ingot`
  - `enderio:vibrant_alloy_ingot`
  - `enderio:redstone_alloy_ingot`
  - `enderio:pulsating_alloy_ingot`
  - `enderio:dark_steel_ingot`
  - `enderio:soularium_ingot`
  - `enderio:end_steel_ingot`
- [x] Confirm EnderIO common tags for ingots/nuggets/storage blocks/gears/dusts.
- [x] Confirm EnderIO recipe JSON format for:
  - Alloy Smelter
  - SAG Mill
  - Soul Binder, only if needed later
- [x] Check TCon's existing `SmelteryCompat` patterns before adding new compat.

## Phase 2: Molten Alloy Support

- [x] Add or reuse molten fluid definitions for EnderIO alloy materials:
  - conductive alloy
  - energetic alloy
  - vibrant alloy
  - redstone alloy
  - pulsating alloy
  - dark steel
  - soularium
  - end steel
- [x] Add fluid tags under `c:fluids/molten_*` and `tconstruct:molten_*` if matching current TCon convention.
- [x] Add melting recipes:
  - ingot -> 90 mB
  - nugget -> 10 mB
  - storage block -> 810 mB
  - gear, only where EnderIO gear tags/items exist and amount is clear
- [x] Add casting recipes:
  - ingot cast -> ingot
  - nugget cast -> nugget
  - block cast/basin -> storage block
  - gear cast, only if this does not conflict with existing TCon gear behavior

## Phase 3: Alloy Recipe Parity

- [x] Mirror EnderIO alloy recipes in TCon smeltery/alloying where sensible:
  - [x] conductive alloy: molten iron + molten copper -> 2 ingots
  - [x] redstone alloy: molten copper + `c:redstone` fluid -> 1 ingot
  - [x] pulsating alloy: molten iron + molten ender -> 2 ingots
  - [x] soularium: molten gold + liquid soul -> 1 ingot
  - [x] energetic alloy: molten conductive alloy + molten gold + `c:redstone` fluid -> 2 ingots
  - [x] vibrant alloy: molten energetic alloy + molten ender + `c:glowstone` fluid -> 2 ingots
  - [ ] dark steel skipped for now: EnderIO requires coal dust, and TCon has no coal fluid to map cleanly.
  - [ ] end steel skipped for now: EnderIO requires end stone, and TCon has no molten end stone fluid to map cleanly.
- [x] Avoid recipe loops or cheaper duplication than EnderIO's own Alloy Smelter.
- [x] Gate each recipe with `enderio` loaded condition.
- [x] Verify JEI/recipe book visibility stays clean when EnderIO is absent.
  - Source/jar check: all EnderIO alloy recipes are gated with `neoforge:mod_loaded` for `enderio`.

## Phase 4: SAG Mill And Powder Flow

- [x] Map EnderIO powders/dusts to TCon melting where appropriate:
  - [x] powdered iron: already covered by existing `c:dusts/iron` melting.
  - [x] powdered gold: already covered by existing `c:dusts/gold` melting.
  - [x] powdered copper: already covered by existing `c:dusts/copper` melting.
  - [x] powdered tin: already covered by existing `c:dusts/tin` melting.
  - [x] powdered obsidian: already covered by existing `c:dusts/obsidian` melting.
  - [x] powdered quartz: already covered by existing `c:dusts/quartz` melting.
  - [ ] powdered ender pearl skipped for now: EnderIO uses 9 powder = 1 pearl, while TCon molten ender is 250 mB per pearl, so there is no integer per-powder amount that preserves value.
  - [ ] powdered lapis skipped for now: TCon has no molten lapis fluid to receive it cleanly.
- [x] Decide whether TCon materials should get SAG Mill outputs.
  - No new SAG Mill recipes for TCon materials in this phase. Extra ore/material outputs would change progression and should be balanced as a separate feature, not basic compat.
- [x] If adding SAG Mill recipes, keep output rate balanced against TCon smeltery yields.
  - No SAG Mill recipes added, so no yield changes.
- [x] Check EnderIO grinding ball data map; do not add TCon items as grinding balls unless there is a strong gameplay reason.
  - EnderIO already has a complete grinding ball tier list from flint through its alloys. No TCon grinding balls added.

## Phase 5: XP Fluid Compatibility

- [x] Confirm EnderIO `xp_juice` is tagged as `c:experience`.
  - `data/c/tags/fluid/experience.json` contains `enderio:fluid_xp_juice_still`.
- [x] Check whether TCon tanks/channels are likely to accept `enderio:fluid_xp_juice_still`.
  - Source check: `SimpleFluidTank#isFluidValid` accepts any non-empty fluid by default, and tank item transfer accepts any non-empty `FluidResource`.
- [x] Add missing compat only if TCon uses a narrower XP fluid tag.
  - No compat data/code added. TCon does not appear to use a narrower XP fluid tag for general tanks/channels.
- [ ] Manual client test: filling, draining, and moving EnderIO XP Juice through TCon tanks/channels.
  - Source check passed; needs in-world bucket/tank/channel interaction to fully pass.

## Phase 6: Conduit And Facade Compatibility

- [ ] Manual client test: EnderIO conduit facades with TCon blocks:
  - seared blocks
  - scorched blocks
  - clear glass variants
  - slimy blocks
  - Current blocker: Athena 4.7.3 is installed in `run/mods`, but this EnderIO alpha jar still ships several stale model JSON files that reference unregistered legacy loaders:
    - `athena:athena`
    - `enderio:io_overlay`
    - `enderio:conduit_item`
  - Do not trust facade/conduit visual results until using a fixed EnderIO runtime jar or explicitly patching the runtime test jar/resource pack.
- [x] Check whether EnderIO uses tags to allow or deny facade targets.
  - Source check: facade data is stored through `BLOCK_PAINT`; no TCon-specific allow-list found.
  - `enderio:hide_facades` is for hiding EnderIO facade/conduit items, not for allowing painted blocks.
- [x] Avoid special Java API integration unless facades fail for TCon blocks.
  - No Java integration added. EnderIO facades consume any valid `BlockItem` paint selected by its Painting Machine rules.
- [x] Check if Yeta Wrench should be accepted by any TCon wrench-like interactions.
  - No shared wrench hook found on TCon blocks. Yeta Wrench is tagged as `c:tools/wrench`, but TCon block interactions do not appear to read that tag.
- [ ] Manual client test: Painting Machine accepts or rejects representative TCon paint blocks.
  - EnderIO requires the paint block to be a non-painted `BlockItem` with a full-block collision shape, so transparent/nonstandard TCon blocks may intentionally fail.
  - Current blocker: EnderIO alpha runtime model assets still need a fixed jar or approved runtime resource patch before trusting facade render results.

## Phase 7: Testing Checklist

- [x] Launch client with TCon + Mantle + EnderIO only.
  - Minimal `run/mods` pass used EnderIO, EnderCore, and Athena. JEI/Jade still load from Gradle runtime dependencies.
  - Client reached resource reload, `RecipeManager` loaded 5017 recipes, and TCon dynamic material/modifier data loaded.
  - Same EnderIO alpha model-loader errors remain; no new TCon compat errors found.
  - In-world portion of this launch is not counted: the reused dev world contains blocks/components from Apotheosis and Mob Grinding Utils, which are intentionally absent in this minimal pass.
- [x] Launch client with full modpack.
  - Log reached resource reload with EnderIO, Athena, Jade, JEI, Mantle, and TCon loaded.
  - TCon Jade plugin and EnderIO Jade plugin both loaded.
- [x] Check all compat recipes are absent when EnderIO is removed.
  - Static recipe check passed via `neoforge:mod_loaded` conditions on each EnderIO alloy recipe.
- [x] In JEI, verify no broken/missing-output recipes.
  - Automated jar/source check passed for TCon EnderIO recipe outputs/tags.
  - Manual JEI visual scan still recommended after the EnderIO alpha model-loader issue is resolved.
- [ ] Melt/cast each EnderIO alloy ingot, nugget, and block.
- [ ] Test at least one alloy recipe from raw inputs to molten output.
- [ ] Test EnderIO Alloy Smelter recipes still work normally.
- [ ] Test EnderIO SAG Mill recipes still work normally.
- [ ] Test XP Juice in TCon tanks or channels.
- [ ] Test conduit facade rendering with selected TCon blocks.
- [x] Build jar after verification.
  - Built and inspected `build/libs/TinkersConstruct-26.1.2-3.11.2.jar`; it contains EnderIO molten fluid tags, melting/casting data, and alloy recipes without `DEV` in the mod metadata.

## Latest Automated Verification

- [x] `compileJava` passes.
- [x] Built jar contains all six EnderIO alloy recipes:
  - molten conductive alloy
  - molten redstone alloy
  - molten pulsating alloy
  - molten soularium
  - molten energetic alloy
  - molten vibrant alloy
- [x] Built jar contains EnderIO molten fluid tags and generated client fluid texture metadata.
- [x] Client log no longer shows `fluid.tconstruct...` missing translation/resource issues for the new TCon fluids.
- [x] Athena 4.7.3 NeoForge jar is installed in `run/mods`.
- [ ] Client log still shows EnderIO-side render/model loader errors even with Athena installed:
  - `enderio:models/block/athena/*_capacitor_bank_part.json` references legacy `athena:athena`.
  - `enderio:models/block/io_overlay.json` references legacy `enderio:io_overlay`.
  - `enderio:models/item/conduit.json` references legacy `enderio:conduit_item`.
  - EnderIO source already contains newer `RegisterBlockStateModels` / `RegisterItemModels` hooks while these legacy JSONs remain in the alpha jar, so this is tracked as an EnderIO alpha runtime asset blocker, not a TCon compat failure.

## Phase 8: EnderIO Tool-Part Materials

- [x] Add TCon material ids for EnderIO alloys:
  - conductive alloy
  - redstone alloy
  - pulsating alloy
  - energetic alloy
  - vibrant alloy
  - soularium
  - dark steel
  - end steel
- [x] Gate materials behind filled common ingot tags such as `c:ingots/conductive_alloy`, so they only appear with an implementation that provides those ingots.
- [x] Add melee/harvest, ranged, and armor stats for all eight materials.
- [x] Assign first-pass TCon traits by EnderIO theme:
  - conductive alloy -> Conductive
  - redstone alloy -> Supercharged
  - pulsating alloy -> Enderference, Enderclearance on armor
  - energetic alloy -> Lightweight
  - vibrant alloy -> Godspeed
  - soularium -> Soulbound
  - dark steel -> Ductile, Stalwart on armor
  - end steel -> Valiant, Enderclearance on armor
- [x] Add material recipes from EnderIO ingot tags.
- [x] Add material melting/casting recipes using the existing EnderIO molten fluids.
- [x] Add material render palettes and English language entries.
- [x] Run datagen and verify generated material definition/stat/trait/recipe/render JSON.
  - `runData runClientData` passed.
  - Generated definition, stats, traits, material recipes, melting/casting, render info, and part textures exist for all eight materials.
- [ ] Manual test in Part Builder/Tinker Station: all eight materials appear, craft into valid parts, and show correct traits.

## Notes

- EnderIO 9.0.5 is alpha, so keep compat conservative.
- Avoid hard dependencies on EnderIO Java classes unless absolutely needed.
- If EnderIO changes recipe serializers or ids, this compat should fail gracefully through conditional data.
