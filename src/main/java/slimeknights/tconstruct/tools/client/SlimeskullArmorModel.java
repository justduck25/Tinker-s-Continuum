package slimeknights.tconstruct.tools.client;

import net.minecraft.client.model.object.skull.PiglinHeadModel;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.data.listener.ISafeManagerReloadListener;
import slimeknights.tconstruct.library.client.armor.MultilayerArmorModel;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Model to render a slimeskull helmet with both the helmet and skull. */
public class SlimeskullArmorModel extends MultilayerArmorModel {
  public static final SlimeskullArmorModel INSTANCE = new SlimeskullArmorModel();
  public static final ISafeManagerReloadListener RELOAD_LISTENER = manager -> {};

  private static final Map<MaterialVariantId, HeadModelInfo> HEAD_MODELS = new HashMap<>();

  private SlimeskullArmorModel() {}

  public static void registerHeadModel(MaterialVariantId material, ModelLayerLocation layer, Identifier texture) {
    registerHeadModel(material, models -> new SkullModel(models.bakeLayer(layer)), texture);
  }

  public static void registerHeadModel(MaterialVariantId material, Function<EntityModelSet, SkullModelBase> modelFactory, Identifier texture) {
    HEAD_MODELS.put(material, new HeadModelInfo(modelFactory, texture, false));
  }

  public static void registerPiglinHeadModel(MaterialVariantId material, ModelLayerLocation layer, Identifier texture) {
    HEAD_MODELS.put(material, new HeadModelInfo(models -> new PiglinHeadModel(models.bakeLayer(layer)), texture, true));
  }

  private record HeadModelInfo(Function<EntityModelSet, SkullModelBase> modelFactory, Identifier texture, boolean piglin) {}
}