package slimeknights.tconstruct.shared.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.tconstruct.shared.particle.FluidParticleData;

public class FluidParticle {

  public static class Factory implements ParticleProvider<FluidParticleData> {
    @Override
    public Particle createParticle(FluidParticleData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
      return null; // TODO 1.21.1: fix particle
    }
  }
}
