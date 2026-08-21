package slimeknights.tconstruct.gadgets;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.gadgets.client.FancyItemFrameRenderer;
import slimeknights.tconstruct.tools.client.material.ThrownShurikenRenderer;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class GadgetClientEvents extends ClientEventBase {
  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(TinkerGadgets.itemFrameEntity.get(), FancyItemFrameRenderer::new);
    event.registerEntityRenderer(TinkerGadgets.glowBallEntity.get(), ctx -> new ThrownItemRenderer(ctx));
    event.registerEntityRenderer(TinkerGadgets.eflnEntity.get(), ctx -> new ThrownItemRenderer(ctx));
    event.registerEntityRenderer(TinkerGadgets.quartzShurikenEntity.get(), ThrownShurikenRenderer::new);
    event.registerEntityRenderer(TinkerGadgets.flintShurikenEntity.get(), ThrownShurikenRenderer::new);
  }
}