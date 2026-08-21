package slimeknights.tconstruct.library.fluid;

import lombok.Getter;
import lombok.Setter;
import slimeknights.mantle.block.entity.MantleBlockEntity;

public class FluidTankAnimated extends FluidTankBase<MantleBlockEntity> {
  private float renderOffset;

  public float getRenderOffset() { return renderOffset; }
  public void setRenderOffset(float renderOffset) { this.renderOffset = renderOffset; }

  public FluidTankAnimated(int capacity, MantleBlockEntity parent) {
    super(capacity, parent);
  }
}
