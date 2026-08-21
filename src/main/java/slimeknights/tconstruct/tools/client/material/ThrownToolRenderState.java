package slimeknights.tconstruct.tools.client.material;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class ThrownToolRenderState extends EntityRenderState {
  public final ItemStackRenderState item = new ItemStackRenderState();
  public float yRot, xRot;
}
