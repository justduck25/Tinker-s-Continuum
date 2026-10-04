# Worldgen findings

- Runtime log: `[SLIME_SPAWN_DEBUG] type=tconstruct:sky_slime ... below=Block{minecraft:grass_block} tag=tconstruct:slime_spawn/sky result=false difficulty=NORMAL`. This proves the custom predicate rejects ordinary grass; it does not prove the island template was used at that candidate position.
- `StructureTemplate.load` in Minecraft 26.1 loads every entry of NBT `palettes` into `this.palettes`; `StructurePlaceSettings.getRandomPalette` selects a random palette with `nextInt(palettes.size())`.
- Upstream TCon island NBT intentionally contains two palettes for `sky`, `earth`, and `ender`, but palette variants cross soil/grass types:
  - sky palette 0: `earth_slime_dirt`, `sky_earth_slime_grass`; palette 1: `sky_slime_dirt`, `sky_sky_slime_grass`.
  - earth palette 0: `earth_slime_dirt`, `earth_earth_slime_grass`; palette 1: `sky_slime_dirt`, `earth_sky_slime_grass`.
  - ender palette 0: `ichor_slime_dirt`, `ender_ichor_slime_grass`; palette 1: `ender_slime_dirt`, `ender_ender_slime_grass`.
- TCon4 and upstream palettes match by names for these templates. The cross-type result is therefore caused by vanilla random palette selection, not a TCon4 copy difference.
- `blood` has a single palette and uses ichor slime dirt/grass plus magma block/fluid; upstream blood island mob is magma cube, not a TCon slime.
- Upstream `IslandPiece.makeSettings` is identical to TCon4 regarding palette selection; no processor chooses a fixed palette.

Next candidate fix: make each island template deterministic by retaining only the correct palette (`sky` palette 1, `earth` palette 0, `ender` palette 1) in the generated NBT, then rebuild and test. This changes observed island behavior to match the requested per-island soil/grass identity; do not edit until checking all template sizes and source ownership.
