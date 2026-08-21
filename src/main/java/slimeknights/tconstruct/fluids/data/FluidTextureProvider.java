package slimeknights.tconstruct.fluids.data;

import net.minecraft.data.PackOutput;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;
import slimeknights.mantle.fluid.texture.FluidTexture;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.fluids.TinkerFluids;

import java.util.function.Function;

import static slimeknights.tconstruct.TConstruct.getResource;
import static slimeknights.tconstruct.fluids.TinkerFluids.withoutMolten;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
public class FluidTextureProvider extends AbstractFluidTextureProvider {
  public FluidTextureProvider(PackOutput packOutput) {
    super(packOutput, TConstruct.MOD_ID);
  }

  @Override
  public void addTextures() {
    addTextures(this::texture);
  }

  /** Adds all TCon fluid texture definitions. Shared by datagen and NeoForge 26.1 fluid model registration. */
  public static void addTextures(Function<FluidObject<?>, FluidTexture.Builder> textures) {
    new TextureRules(textures).addTextures();
  }

  private record TextureRules(Function<FluidObject<?>, FluidTexture.Builder> textures) {
    private void addTextures() {
      // basic
      root(TinkerFluids.powderedSnow);
      root(TinkerFluids.potion).color(0xfff800f8);
      // slime
      waterFog(slime(TinkerFluids.earthSlime, "earth"));
      waterFog(slime(TinkerFluids.skySlime, "sky"));
      slime(TinkerFluids.ichor, "ichor");
      waterFog(slime(TinkerFluids.enderSlime, "ender"));
      slime(TinkerFluids.magma);
      waterFog(slime(TinkerFluids.venom));
      moltenFog(slime(TinkerFluids.liquidSoul, "soul"));
      // food
      waterFog(folder(TinkerFluids.honey, "food"));
      tintedStew(TinkerFluids.beetrootSoup).color(0xFF84160D);
      tintedStew(TinkerFluids.mushroomStew).color(0xFFCD8C6F);
      tintedStew(TinkerFluids.rabbitStew).color(0xFF984A2C);
      tintedStew(TinkerFluids.meatSoup).color(0xFFE03E35);

      // molten
      molten(TinkerFluids.moltenGlass).fog(0.25f, 8);
      named(TinkerFluids.blazingBlood, "molten/blaze");
      // stone
      tintedStone(TinkerFluids.searedStone).color(0xFF4F4A47);
      tintedStone(TinkerFluids.scorchedStone).color(0xFF3E3029);
      tintedStone(TinkerFluids.moltenClay).color(0xFF9B6045);
      stone(TinkerFluids.moltenPorcelain);
      stone(TinkerFluids.moltenObsidian);
      tintedStone(TinkerFluids.moltenEnder).color(0xFF105E51);

      // ore - non-metal
      moltenFog(ore(TinkerFluids.moltenDiamond));
      moltenFog(ore(TinkerFluids.moltenEmerald));
      moltenFog(ore(TinkerFluids.moltenAmethyst));
      ore(TinkerFluids.moltenQuartz);
      tintedStone(TinkerFluids.moltenDebris).color(0xFF411E15);
      // ore - tinkers
      ore(TinkerFluids.moltenCopper);
      ore(TinkerFluids.moltenIron);
      ore(TinkerFluids.moltenGold);
      ore(TinkerFluids.moltenCobalt);
      ore(TinkerFluids.moltenSteel);

      // alloy - overworld
      alloy(TinkerFluids.moltenSlimesteel);
      alloy(TinkerFluids.moltenAmethystBronze);
      alloy(TinkerFluids.moltenPigIron);
      alloy(TinkerFluids.moltenRoseGold);
      // alloy - nether
      alloy(TinkerFluids.moltenManyullyn);
      alloy(TinkerFluids.moltenHepatizon);
      alloy(TinkerFluids.moltenCinderslime);
      alloy(TinkerFluids.moltenQueensSlime).fogColor(0x478A33);
      alloy(TinkerFluids.moltenNetherite);
      // alloy - end
      alloy(TinkerFluids.moltenSoulsteel);
      alloy(TinkerFluids.moltenKnightmetal);
      alloy(TinkerFluids.moltenKnightslime);

      // compat - ore
      compatOre(TinkerFluids.moltenAluminum);
      compatOre(TinkerFluids.moltenLead);
      compatOre(TinkerFluids.moltenNickel);
      compatOre(TinkerFluids.moltenOsmium);
      compatOre(TinkerFluids.moltenPlatinum);
      compatOre(TinkerFluids.moltenSilver);
      compatOre(TinkerFluids.moltenTin);
      compatOre(TinkerFluids.moltenTungsten);
      compatOre(TinkerFluids.moltenUranium);
      compatOre(TinkerFluids.moltenZinc);
      tintedMolten(TinkerFluids.moltenChromium).color(0xFFC4B180);
      tintedMolten(TinkerFluids.moltenCadmium).color(0xFF8AB8C8);
      // compat - alloy
      compatAlloy(TinkerFluids.moltenBrass);
      compatAlloy(TinkerFluids.moltenBronze);
      compatAlloy(TinkerFluids.moltenConstantan);
      compatAlloy(TinkerFluids.moltenElectrum);
      compatAlloy(TinkerFluids.moltenInvar);
      compatAlloy(TinkerFluids.moltenPewter);
      // thermal
      compatAlloy(TinkerFluids.moltenEnderium);
      compatAlloy(TinkerFluids.moltenLumium);
      compatAlloy(TinkerFluids.moltenSignalum);
      // mekanism
      compatAlloy(TinkerFluids.moltenRefinedObsidian);
      compatAlloy(TinkerFluids.moltenRefinedGlowstone);
      // metalborn
      compatAlloy(TinkerFluids.moltenNicrosil);
      compatAlloy(TinkerFluids.moltenDuralumin);
      tintedMolten(TinkerFluids.moltenBendalloy).color(0xFFD6D2C1);
      // twilight
      compatOre(TinkerFluids.moltenSteeleaf);
      slime(TinkerFluids.fieryLiquid, "fiery");
    }

    private static FluidTexture.Builder waterFog(FluidTexture.Builder builder) {
      return builder.fog(-8, 24);
    }

    private static FluidTexture.Builder moltenFog(FluidTexture.Builder builder) {
      return builder.fog(0.25f, 4);
    }

    private FluidTexture.Builder texture(FluidObject<?> fluid) {
      return textures.apply(fluid);
    }

    private FluidTexture.Builder root(FluidObject<?> fluid) {
      return texture(fluid).wrapId("fluid/", "/", false, false);
    }

    private FluidTexture.Builder named(FluidObject<?> fluid, String name) {
      return texture(fluid).root(getResource("fluid/" + name + "/"))
        .still().flowing().camera().calculateFogColor(true).fog(0.25f, 2);
    }

    private FluidTexture.Builder folder(FluidObject<?> fluid, String folder) {
      return named(fluid, folder + '/' + fluid.getId().getPath());
    }

    private FluidTexture.Builder slime(FluidObject<?> fluid) {
      return folder(fluid, "slime");
    }

    private FluidTexture.Builder slime(FluidObject<?> fluid, String name) {
      return named(fluid, "slime/" + name);
    }

    private FluidTexture.Builder molten(FluidObject<?> fluid) {
      return named(fluid, "molten/" + withoutMolten(fluid));
    }

    private FluidTexture.Builder moltenFolder(FluidObject<?> fluid, String folder) {
      return named(fluid, "molten/" + folder + "/" + withoutMolten(fluid));
    }

    private FluidTexture.Builder stone(FluidObject<?> fluid) {
      return moltenFolder(fluid, "stone");
    }

    private FluidTexture.Builder ore(FluidObject<?> fluid) {
      return moltenFolder(fluid, "ore");
    }

    private FluidTexture.Builder alloy(FluidObject<?> fluid) {
      return moltenFolder(fluid, "alloy");
    }

    private FluidTexture.Builder compatOre(FluidObject<?> fluid) {
      return moltenFolder(fluid, "compat_ore");
    }

    private FluidTexture.Builder compatAlloy(FluidObject<?> fluid) {
      return moltenFolder(fluid, "compat_alloy");
    }

    private FluidTexture.Builder tintedStew(FluidObject<?> fluid) {
      return named(fluid, "food/stew");
    }

    private FluidTexture.Builder tintedStone(FluidObject<?> fluid) {
      return named(fluid, "molten/stone");
    }

    private FluidTexture.Builder tintedMolten(FluidObject<?> fluid) {
      return named(fluid, "molten");
    }
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Fluid Texture Providers";
  }
}
