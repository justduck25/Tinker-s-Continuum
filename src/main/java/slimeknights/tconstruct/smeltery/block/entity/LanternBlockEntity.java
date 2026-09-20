package slimeknights.tconstruct.smeltery.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;

/** Tank variant used by seared and scorched lanterns. */
public class LanternBlockEntity extends TankBlockEntity {
  public LanternBlockEntity(BlockPos pos, BlockState state) {
    this(pos, state, TinkerSmeltery.searedLantern.get());
  }

  /** Main constructor */
  public LanternBlockEntity(BlockPos pos, BlockState state, ITankBlock block) {
    super(TinkerSmeltery.lantern.get(), pos, state, block);
  }

  // fluid is drawn by the tank TESR unless the tank-fluid-model config is on,
  // matching seared tanks. Forcing the model path left placed lanterns empty in 26.1.
}
