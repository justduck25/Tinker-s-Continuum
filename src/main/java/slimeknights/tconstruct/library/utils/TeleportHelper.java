package slimeknights.tconstruct.library.utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.NeoForge;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.events.teleport.TinkerTeleportEvent;

public class TeleportHelper {
  /** Randomly teleports an entity, mostly copied from chorus fruit. */
  @CanIgnoreReturnValue
  public static boolean randomNearbyTeleport(LivingEntity living) {
    return randomNearbyTeleport(living, TinkerTeleportEvent::new, 16, 16);
  }

  /** Randomly teleports an entity, mostly copied from chorus fruit. */
  @CanIgnoreReturnValue
  public static boolean randomNearbyTeleport(LivingEntity living, ITeleportEventFactory factory) {
    return randomNearbyTeleport(living, factory, 16, 16);
  }

  /** Randomly teleports an entity, mostly copied from chorus fruit. */
  @CanIgnoreReturnValue
  public static boolean randomNearbyTeleport(LivingEntity living, int diameter, int chances) {
    return randomNearbyTeleport(living, TinkerTeleportEvent::new, diameter, chances);
  }

  /** Randomly teleports an entity, mostly copied from chorus fruit. */
  @CanIgnoreReturnValue
  public static boolean randomNearbyTeleport(LivingEntity living, ITeleportEventFactory factory, int diameter, int chances) {
    Level level = living.level();
    if (level.isClientSide()) {
      return true;
    }
    double posX = living.getX();
    double posY = living.getY();
    double posZ = living.getZ();

    RandomSource random = living.getRandom();
    float minHeight = level.getMinY();
    float maxHeight = level.getMaxY() - 1;
    for (int i = 0; i < chances; ++i) {
      double x = posX + (random.nextDouble() - 0.5D) * diameter;
      double y = Mth.clamp(posY + (double)(random.nextInt(diameter) - 8), minHeight, maxHeight);
      double z = posZ + (random.nextDouble() - 0.5D) * diameter;
      if (living.isPassenger()) {
        living.stopRiding();
      }

      level.gameEvent(GameEvent.TELEPORT, living.position(), GameEvent.Context.of(living));
      TinkerTeleportEvent event = factory.create(living, x, y, z);
      NeoForge.EVENT_BUS.post(event);
      if (!event.isCanceled() && living.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
        SoundEvent soundevent = Sounds.SLIME_TELEPORT.getSound();
        level.playSound(null, posX, posY, posZ, soundevent, living.getSoundSource(), 1.0F, 1.0F);
        living.playSound(soundevent, 1.0F, 1.0F);
        return true;
      }
    }
    return false;
  }

  /** Fires the teleport event, then teleports the entity if it works. */
  public static boolean tryTeleport(TinkerTeleportEvent event) {
    NeoForge.EVENT_BUS.post(event);
    if (event.isCanceled()) {
      return false;
    }
    Entity entity = event.getEntity();
    spawnParticles(entity);
    entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
    spawnParticles(entity);
    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Sounds.ENDERPORTING.getSound(), entity.getSoundSource(), 1f, 1f);
    return true;
  }

  /** Predicate to test if the entity can teleport, typically just fires a cancelable event. */
  @FunctionalInterface
  public interface ITeleportEventFactory {
    TinkerTeleportEvent create(LivingEntity entity, double x, double y, double z);
  }

  /** Spawns particles around the entity for teleporting. */
  private static void spawnParticles(Entity entity) {
    Level level = entity.level();
    if (level instanceof ServerLevel serverWorld) {
      for (int i = 0; i < 32; ++i) {
        serverWorld.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + level.getRandom().nextDouble() * 2.0D, entity.getZ(), 1, level.getRandom().nextGaussian(), 0.0D, level.getRandom().nextGaussian(), 0);
      }
    }
  }
}