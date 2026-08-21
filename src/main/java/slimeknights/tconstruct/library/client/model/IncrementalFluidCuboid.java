package slimeknights.tconstruct.library.client.model;

import com.google.gson.JsonObject;
import com.mojang.math.Quadrant;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;
import slimeknights.mantle.client.render.FluidCuboid;

import java.util.EnumMap;
import java.util.Map;

/** Fluid cuboid whose height is scaled by the contained amount. */
public final class IncrementalFluidCuboid extends FluidCuboid {
  private final int increments;

  public IncrementalFluidCuboid(Vector3f from, Vector3f to, Map<Direction, FluidFace> faces, int increments) {
    super(from, to, faces);
    this.increments = increments;
  }

  public int getIncrements() {
    return increments;
  }

  /** Creates the fluid cuboid for an amount represented by {@code amount/increments}. */
  public CuboidModelElement getPart(int amount, boolean gas) {
    Vector3f from = new Vector3f(getFrom());
    Vector3f to = new Vector3f(getTo());
    float minY = from.y();
    float maxY = to.y();
    if (gas) {
      from.y = maxY + (amount * (minY - maxY) / increments);
    } else {
      to.y = minY + (amount * (maxY - minY) / increments);
    }

    Map<Direction, CuboidFace> faces = new EnumMap<>(Direction.class);
    for (Map.Entry<Direction, FluidFace> entry : getFaces().entrySet()) {
      Direction direction = entry.getKey();
      FluidFace face = entry.getValue();
      boolean flowing = face.isFlowing();
      faces.put(direction, new CuboidFace(
        null, 0, flowing ? "flowing_fluid" : "fluid",
        getFaceUvs(from, to, direction, face.rotation(), flowing ? 0.5f : 1f),
        quadrant(face.rotation())));
    }
    return new CuboidModelElement(from, to, faces);
  }

  private static CuboidFace.UVs getFaceUvs(Vector3f from, Vector3f to, Direction side, int rotation, float scale) {
    float u1;
    float u2;
    float v1;
    float v2;
    switch (side) {
      case DOWN -> {
        u1 = from.x(); v1 = 16f - to.z();
        u2 = to.x();   v2 = 16f - from.z();
      }
      case UP -> {
        u1 = from.x(); v1 = from.z();
        u2 = to.x();   v2 = to.z();
      }
      case NORTH -> {
        u1 = 16f - to.x();   v1 = 16f - to.y();
        u2 = 16f - from.x(); v2 = 16f - from.y();
      }
      case SOUTH -> {
        u1 = from.x(); v1 = 16f - to.y();
        u2 = to.x();   v2 = 16f - from.y();
      }
      case WEST -> {
        u1 = from.z(); v1 = 16f - to.y();
        u2 = to.z();   v2 = 16f - from.y();
      }
      case EAST -> {
        u1 = 16f - to.z();   v1 = 16f - to.y();
        u2 = 16f - from.z(); v2 = 16f - from.y();
      }
      default -> throw new IllegalStateException("Unexpected fluid face: " + side);
    }
    if (rotation >= 180) {
      float temp = v1;
      v1 = 16f - v2;
      v2 = 16f - temp;
    }
    if (rotation == 90 || rotation == 180) {
      float temp = u1;
      u1 = 16f - u2;
      u2 = 16f - temp;
    }
    if ((rotation % 180) == 90) {
      return new CuboidFace.UVs(v1 * scale, u1 * scale, v2 * scale, u2 * scale);
    }
    return new CuboidFace.UVs(u1 * scale, v1 * scale, u2 * scale, v2 * scale);
  }

  private static Quadrant quadrant(int rotation) {
    return switch (Math.floorMod(rotation, 360)) {
      case 90 -> Quadrant.R90;
      case 180 -> Quadrant.R180;
      case 270 -> Quadrant.R270;
      default -> Quadrant.R0;
    };
  }

  public static IncrementalFluidCuboid fromJson(JsonObject json) {
    FluidCuboid base = FluidCuboid.LOADABLE.deserialize(json);
    int increments = GsonHelper.getAsInt(json, "increments");
    return new IncrementalFluidCuboid(base.getFrom(), base.getTo(), base.getFaces(), increments);
  }
}
