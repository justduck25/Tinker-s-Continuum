package slimeknights.tconstruct.library.client.particle;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.SlimeType;

import javax.annotation.Nullable;

// Not part of the TCon particle system since it uses vanilla item particles.
public class SlimeParticle extends BreakingItemParticle {
  public SlimeParticle(ClientLevel level, double x, double y, double z, ItemStack stack) {
    this(level, x, y, z, 0.0, 0.0, 0.0, stack, RandomSource.create());
  }

  public SlimeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed,
                       double zSpeed, ItemStack stack, RandomSource random) {
    super(level, x, y, z, xSpeed, ySpeed, zSpeed, getSprite(stack, level, random));
  }

  private static TextureAtlasSprite getSprite(ItemStack stack, ClientLevel level, RandomSource random) {
    ItemStackRenderState renderState = new ItemStackRenderState();
    Minecraft.getInstance().getItemModelResolver().updateForTopItem(
      renderState, stack, ItemDisplayContext.GROUND, level, null, 0
    );
    Material.Baked material = renderState.pickParticleMaterial(random);
    return material != null
           ? material.sprite()
           : Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite();
  }

  @RequiredArgsConstructor
  public static class Factory implements ParticleProvider<SimpleParticleType> {
    private final ItemLike slime;

    public Factory(SlimeType type) {
      this.slime = TinkerCommons.slimeball.get(type);
    }

    @Nullable
    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
      return new SlimeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, new ItemStack(slime), random);
    }
  }
}
