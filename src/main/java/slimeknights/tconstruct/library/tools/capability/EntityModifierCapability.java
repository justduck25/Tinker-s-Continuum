package slimeknights.tconstruct.library.tools.capability;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Capability to allow an entity to store modifiers, used on projectiles fired from modifiable items. */
public class EntityModifierCapability {
  public static final EntityModifiers EMPTY = new EntityModifiers() {
    @Override public ModifierNBT getModifiers() { return ModifierNBT.EMPTY; }
    @Override public void setModifiers(ModifierNBT nbt) {}
    @Override public void addModifiers(ModifierNBT nbt) {}
  };

  private EntityModifierCapability() {}

  private static final List<Predicate<Entity>> ENTITY_PREDICATES = new ArrayList<>();
  private static final Identifier ID = TConstruct.getResource("modifiers");
  public static final EntityCapability<EntityModifiers, Void> CAPABILITY = EntityCapability.createVoid(ID, EntityModifiers.class);

  public static EntityModifiers getCapability(Entity entity) {
    EntityModifiers modifiers = CAPABILITY.getCapability(entity, null);
    return modifiers == null ? EMPTY : modifiers;
  }

  public static ModifierNBT getOrEmpty(Entity entity) {
    return getCapability(entity).getModifiers();
  }

  public static boolean supportCapability(Entity entity) {
    for (Predicate<Entity> entityPredicate : ENTITY_PREDICATES) {
      if (entityPredicate.test(entity)) {
        return true;
      }
    }
    return false;
  }

  public static void registerEntityPredicate(Predicate<Entity> predicate) {
    ENTITY_PREDICATES.add(predicate);
  }

  public static void register() {
    TConstruct.MOD_EVENT_BUS.addListener(EntityModifierCapability::registerCapabilities);
  }

  private static void registerCapabilities(RegisterCapabilitiesEvent event) {
    for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE.stream().toList()) {
      event.registerEntity(CAPABILITY, type, (entity, ctx) -> supportCapability(entity) ? new Provider() : null);
    }
  }

  private static class Provider implements EntityModifiers {
    @Getter @Setter
    private ModifierNBT modifiers = ModifierNBT.EMPTY;
  }

  public interface EntityModifiers {
    ModifierNBT getModifiers();
    void setModifiers(ModifierNBT nbt);

    default void addModifiers(ModifierNBT nbt) {
      ModifierNBT existing = getModifiers();
      if (existing.isEmpty()) {
        setModifiers(nbt);
      } else {
        setModifiers(ModifierNBT.builder().add(existing).add(nbt).build());
      }
    }
  }
}