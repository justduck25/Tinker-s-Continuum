package slimeknights.tconstruct.tools.client;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.Identifier;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tools.item.CrystalshotItem.CrystalshotEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CrystalshotRenderer extends ArrowRenderer<CrystalshotEntity, CrystalshotRenderState> {
  private static final Map<String,Identifier> TEXTURES = new HashMap<>();
  private static final Function<String,Identifier> TEXTURE_GETTER = variant -> TConstruct.getResource("textures/entity/arrow/" + variant + ".png");
  public CrystalshotRenderer(Context context) {
    super(context);
  }

  @Override
  public Identifier getTextureLocation(CrystalshotRenderState state) {
    return state.texture;
  }

  @Override
  public CrystalshotRenderState createRenderState() {
    return new CrystalshotRenderState();
  }

  @Override
  public void extractRenderState(CrystalshotEntity entity, CrystalshotRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.texture = TEXTURES.computeIfAbsent(entity.getVariant(), TEXTURE_GETTER);
  }
}
