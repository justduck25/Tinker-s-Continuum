package slimeknights.tconstruct.tools.data.material;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.world.TinkerWorld;

/** Provider for trim materials. */
public class TrimMaterialProvider {
  private static final String TRIM_FORMAT = TConstruct.makeDescriptionId("trim_material", "format");

  /** Registers all providers. */
  public static void register(RegistrySetBuilder builder) {
    builder.add(Registries.TRIM_MATERIAL, TrimMaterialProvider::registerTrimMaterials);
  }

  /** Registers all trim materials with the context. */
  private static void registerTrimMaterials(BootstrapContext<TrimMaterial> context) {
    material(context, MaterialIds.slimesteel,     TinkerMaterials.slimesteel,     0x27C6C6);
    material(context, MaterialIds.amethystBronze, TinkerMaterials.amethystBronze, 0xC687BD);
    material(context, MaterialIds.pigIron,        TinkerMaterials.pigIron,        0xF0A8A4);
    material(context, MaterialIds.roseGold,       TinkerMaterials.roseGold,       0xF7CDBB);

    material(context, MaterialIds.cobalt,      TinkerMaterials.cobalt,      0x2376dd);
    material(context, MaterialIds.steel,       TinkerMaterials.steel,       0x959595);
    material(context, MaterialIds.manyullyn,   TinkerMaterials.manyullyn,   0x9261cc);
    material(context, MaterialIds.hepatizon,   TinkerMaterials.hepatizon,   0x60496b);
    material(context, MaterialIds.cinderslime, TinkerMaterials.cinderslime, 0xB80000);
    material(context, MaterialIds.queensSlime, TinkerMaterials.queensSlime, 0x236c45);
    material(context, MaterialIds.knightmetal, TinkerMaterials.knightmetal, 0xC4D6AE);
    material(context, MaterialIds.knightslime, TinkerMaterials.knightslime, 0xB771FF);

    material(context, MaterialIds.earthslime, TinkerWorld.earthGeode, 0x01cd4e);
    material(context, MaterialIds.skyslime,   TinkerWorld.skyGeode,   0x01cbcd);
    material(context, MaterialIds.ichor,      TinkerWorld.ichorGeode, 0xff970d);
    material(context, MaterialIds.enderslime, TinkerWorld.enderGeode, 0xaf4cf6);
  }

  /** Registers a trim material using the ingot with the context. */
  private static void material(BootstrapContext<TrimMaterial> context, MaterialId material, MetalItemObject ingredient, int color) {
    material(context, material, ingredient.getIngot(), color);
  }

  /** Registers a trim material with the context. Ingredient is kept in the signature for source compatibility with old datagen calls. */
  private static void material(BootstrapContext<TrimMaterial> context, MaterialId material, ItemLike ingredient, int color) {
    ResourceKey<TrimMaterial> key = ResourceKey.create(Registries.TRIM_MATERIAL, material.getId());
    Component description = Component.translatable(TRIM_FORMAT, Component.translatable(Util.makeDescriptionId("material", material.getId()))).withStyle(style -> style.withColor(color));
    context.register(key, new TrimMaterial(MaterialAssetGroup.create(material.getSuffix()), description));
  }
}