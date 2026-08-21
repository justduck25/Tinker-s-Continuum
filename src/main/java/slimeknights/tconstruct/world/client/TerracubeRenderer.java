package slimeknights.tconstruct.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.MagmaCubeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;
import slimeknights.tconstruct.TConstruct;

public class TerracubeRenderer extends MobRenderer<Slime, SlimeRenderState, MagmaCubeModel> {
  private static final Identifier TEXTURE = TConstruct.getResource("textures/entity/terracube.png");
  public TerracubeRenderer(EntityRendererProvider.Context context) {
    super(context, new MagmaCubeModel(context.bakeLayer(ModelLayers.MAGMA_CUBE)), 0.25F);
    addLayer(new SlimeArmorLayer<>(this, context.getModelSet(), true));
  }

  @Override
  public Identifier getTextureLocation(SlimeRenderState state) {
    return TEXTURE;
  }

  @Override
  public SlimeRenderState createRenderState() {
    return new SlimeRenderState();
  }

  @Override
  public void extractRenderState(Slime entity, SlimeRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish);
    state.size = entity.getSize();
  }

  @Override
  protected void scale(SlimeRenderState state, PoseStack poseStack) {
    int size = state.size;
    float squishFactor = state.squish / ((float)size * 0.5F + 1.0F);
    float invertedSquish = 1.0F / (squishFactor + 1.0F);
    poseStack.scale(invertedSquish * (float)size, 1.0F / invertedSquish * (float)size, invertedSquish * (float)size);
  }
}
