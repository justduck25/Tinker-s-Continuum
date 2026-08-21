package slimeknights.tconstruct.tools.client.material;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

public class CombatFishingHookRenderState extends EntityRenderState {
  public int luminosity;
  public int color = -1;
  public net.minecraft.resources.Identifier texture;
  public Vec3 lineOriginOffset = Vec3.ZERO;
  public int sideOffset = 1;
  public boolean isFirstPerson;
  public Vec3 playerPos = Vec3.ZERO;
  public float eyeHeightOffset;
  public float attackAnim;
  public float yBodyRot;
}
