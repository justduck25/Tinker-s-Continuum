package slimeknights.tconstruct.library.tools.item.armor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import slimeknights.mantle.registration.object.IdAwareObject;

/** Armor material that returns 0 except for name, since we bypass all the usages */
@RequiredArgsConstructor
@Getter
public class DummyArmorMaterial implements IdAwareObject {
  private final Identifier id;
  private final SoundEvent equipSound;

  public String getName() {
    return id.toString();
  }
}