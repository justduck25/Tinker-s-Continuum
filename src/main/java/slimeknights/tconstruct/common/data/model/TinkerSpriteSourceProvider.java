package slimeknights.tconstruct.common.data.model;

import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import net.neoforged.neoforge.client.data.SpriteSourceProvider;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tools.client.ShieldBannerModifierSpriteSource;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provider to stitch textures from additional folders
 */
public class TinkerSpriteSourceProvider extends SpriteSourceProvider {
  /** List of trim variants supported, must all exist in vanilla */
  private static final String[] TRIMS = {
    "coast", "sentry", "dune", "wild", "ward", "eye", "vex", "tide", "snout",
    "rib", "spire", "wayfinder", "shaper", "silence", "raiser", "host", "flow", "bolt"
  };
  private static final String PALETTE_FOLDER = "trims/color_palettes/";
  private static final String TRIM_FOLDER = "trims/entity/";
  private static final Identifier BLOCKS_ATLAS = Identifier.parse("blocks");

  public TinkerSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries, TConstruct.MOD_ID);
  }

  @SuppressWarnings("removal")
  @Override
  protected void gather() {
    Identifier trimPalette = Identifier.parse(PALETTE_FOLDER + "trim_palette");
    // map of material suffix to material paeltte for trims
    Map<String, Identifier> tinkerMaterials = Arrays.stream(MaterialIds.TRIM_MATERIALS).collect(Collectors.toMap(id -> id.getId().getPath(), id -> id.getId().withPrefix(PALETTE_FOLDER)));

    atlas(BLOCKS_ATLAS)
      // We load our fluid textures from here
      .addSource(directory("fluid"))
      // patterns load from this directory
      .addSource(directory("gui/modifiers"))
      // we typically use this directory for modifier icons that are not items nor blocks
      .addSource(directory("gui/tinker_pattern"))
      // banner modifier icons
      .addSource(new ShieldBannerModifierSpriteSource(2, 2, 10, 20, TConstruct.getResource("item/tool/armor/plate/shield/banner_large/"), 11, 8, 32));
    // add armor trims in our materials
    atlas(Identifier.parse("armor_trims"))
      .addSource(new PalettedPermutations(
        Arrays.stream(TRIMS).flatMap(name -> Stream.of(Identifier.parse(TRIM_FOLDER + "humanoid/" + name), Identifier.parse(TRIM_FOLDER + "humanoid_leggings/" + name))).toList(),
        trimPalette, tinkerMaterials));
  }

  /** Creates a directory lister where the source matches the prefix. */
  private static DirectoryLister directory(String path) {
    return new DirectoryLister(path, path + '/');
  }
}

