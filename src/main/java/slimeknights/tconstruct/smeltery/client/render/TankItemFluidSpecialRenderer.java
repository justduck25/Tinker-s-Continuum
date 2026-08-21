package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.MantleRenderTypes;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.smeltery.item.TankItem;

/** Special item renderer that adds the contained tank fluid layer to tank block items. */
public class TankItemFluidSpecialRenderer implements SpecialModelRenderer<TankItemFluidSpecialRenderer.TankContents> {
  private static final FluidCuboid TANK_FLUID = FluidCuboid.builder()
    .from(0.08f, 0.08f, 0.08f)
    .to(15.92f, 15.92f, 15.92f)
    .build();
  private static final FluidCuboid LANTERN_FLUID = FluidCuboid.builder()
    .from(5.05f, 1.0f, 5.05f)
    .to(10.95f, 6.0f, 10.95f)
    .build();

  @Override
  public @Nullable TankContents extractArgument(ItemStack stack) {
    if (stack.isEmpty() || !(stack.getItem() instanceof TankItem)) {
      return null;
    }
    FluidTank tank = TankItem.getTank(stack, 1);
    FluidStack fluid = tank.getFluid();
    if (fluid.isEmpty()) {
      return null;
    }
    Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    FluidCuboid cuboid = itemId.getPath().endsWith("_lantern") ? LANTERN_FLUID : TANK_FLUID;
    return new TankContents(fluid.copy(), tank.getCapacity(), cuboid);
  }

  @Override
  public void submit(@Nullable TankContents contents, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
    if (contents == null || contents.fluid().isEmpty() || contents.capacity() <= 0) {
      return;
    }
    submitNodeCollector.submitCustomGeometry(poseStack, MantleRenderTypes.FLUID, (pose, buffer) -> {
      PoseStack localPoseStack = new PoseStack();
      localPoseStack.last().pose().set(pose.pose());
      localPoseStack.last().normal().set(pose.normal());
      RenderUtils.renderScaled(localPoseStack, buffer, contents.cuboid(), contents.fluid(), 0, contents.capacity(), lightCoords, true);
    });
  }

  @Override
  public void getExtents(Consumer<Vector3fc> output) {
    output.accept(new Vector3f(0, 0, 0));
    output.accept(new Vector3f(1, 1, 1));
  }

  public record TankContents(FluidStack fluid, int capacity, FluidCuboid cuboid) {}

  public record Unbaked() implements SpecialModelRenderer.Unbaked<TankContents> {
    public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

    @Override
    public @Nullable SpecialModelRenderer<TankContents> bake(SpecialModelRenderer.BakingContext context) {
      return new TankItemFluidSpecialRenderer();
    }

    @Override
    public MapCodec<? extends SpecialModelRenderer.Unbaked<TankContents>> type() {
      return MAP_CODEC;
    }
  }

  public static void register(net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event) {
    event.register(TConstruct.getResource("tank_fluid"), Unbaked.MAP_CODEC);
  }
}