package slimeknights.tconstruct.shared.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerEffect;
import slimeknights.tconstruct.library.events.teleport.ReturningTeleportEvent;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.utils.TeleportHelper;
import slimeknights.tconstruct.shared.TinkerEffects;

public class ReturningEffect extends TinkerEffect {
  private static final Identifier KEY = TConstruct.getResource("returning");
  public ReturningEffect() {
    super(MobEffectCategory.NEUTRAL, 0xa92dff, true);
    NeoForge.EVENT_BUS.addListener((MobEffectEvent.Added event) -> this.onEffectAdded(event));
  }

  /** Called to set the return position when the effect is added */
  private void onEffectAdded(MobEffectEvent.Added event) {
    // store entity's current position when the effect is added
    if (!(event.getEntity() instanceof LivingEntity entity)) {
      return;
    }
    if (!entity.level().isClientSide() && event.getOldEffectInstance() == null && event.getEffectInstance().getEffect().equals(TinkerEffects.returning)) {
      ModDataNBT data = PersistentDataCapability.getOrWarn(entity);
      CompoundTag pos = new CompoundTag();
      BlockPos blockPos = entity.blockPosition();
      pos.putInt("x", blockPos.getX());
      pos.putInt("y", blockPos.getY());
      pos.putInt("z", blockPos.getZ());
      pos.putString("dimension", entity.level().dimension().identifier().toString());
      data.put(KEY, pos);
    }
  }

  public boolean isDurationEffectTick(int duration, int amplifier) {
    return duration == 1;
  }

  public void applyEffectTick(LivingEntity living, int amplifier) {
    ModDataNBT data = PersistentDataCapability.getOrWarn(living);
    if (data.contains(KEY, Tag.TAG_COMPOUND)) {
      CompoundTag tag = data.getCompound(KEY);
      Identifier dimension = Identifier.tryParse(tag.getString("dimension").orElse(""));
      // no teleporting if you switched dimensions
      // TODO: look into cross dimensional teleport, its doable with entity#teleportTo
      if (dimension != null && dimension.equals(living.level().dimension().identifier())) {
        int x = tag.getInt("x").orElse(0);
        int y = tag.getInt("y").orElse(0);
        int z = tag.getInt("z").orElse(0);
        TeleportHelper.tryTeleport(new ReturningTeleportEvent(living, x + 0.5, y, z + 0.5));
      }
    }
  }
}
