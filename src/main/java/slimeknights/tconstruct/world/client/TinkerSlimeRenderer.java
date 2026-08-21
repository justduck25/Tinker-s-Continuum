package slimeknights.tconstruct.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.world.entity.ArmoredSlimeEntity;

public class TinkerSlimeRenderer extends MobRenderer<Slime, ArmoredSlimeRenderState, SlimeModel> {
  public static final Factory SKY_SLIME_FACTORY = new Factory(TConstruct.getResource("textures/entity/sky_slime.png"), TConstruct.getResource("textures/entity/steel_slime.png"));
  public static final Factory ENDER_SLIME_FACTORY = new Factory(TConstruct.getResource("textures/entity/ender_slime.png"), TConstruct.getResource("textures/entity/knightmetal_slime.png"));

  private final Identifier slime, metal;
  public TinkerSlimeRenderer(EntityRendererProvider.Context context, Identifier slime, Identifier metal) {
    super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
    this.slime = slime;
    this.metal = metal;
    addLayer(new SlimeArmorLayer<>(this, context.getModelSet(), false));
  }

  @Override
  public Identifier getTextureLocation(ArmoredSlimeRenderState state) {
    return state.texture;
  }

  @Override
  public ArmoredSlimeRenderState createRenderState() {
    return new ArmoredSlimeRenderState();
  }

  @Override
  public void extractRenderState(Slime entity, ArmoredSlimeRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish);
    state.size = entity.getSize();
    state.texture = (slime != metal && ((ArmoredSlimeEntity) entity).isMetal()) ? metal : slime;
  }

  @Override
  protected void scale(ArmoredSlimeRenderState state, PoseStack poseStack) {
    float s = 0.999F;
    poseStack.scale(0.999F, 0.999F, 0.999F);
    poseStack.translate(0.0F, 0.001F, 0.0F);
    int size = state.size;
    float ss = state.squish / (size * 0.5F + 1.0F);
    float w = 1.0F / (ss + 1.0F);
    poseStack.scale(w * size, 1.0F / w * size, w * size);
  }

  @Override
  protected float getShadowRadius(ArmoredSlimeRenderState state) {
    return state.size * 0.25F;
  }

  private record Factory(Identifier slime, Identifier metal) implements EntityRendererProvider<Slime> {
    public Factory(Identifier texture) {
      this(texture, texture);
    }

    @Override
    public EntityRenderer<Slime, ArmoredSlimeRenderState> create(Context context) {
      return new TinkerSlimeRenderer(context, slime, metal);
    }
  }
}
