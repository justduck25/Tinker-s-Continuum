package slimeknights.tconstruct.tools.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.ArrayList;
import java.util.List;

/** Internal item used by crystalshot modifier */
public class CrystalshotItem extends ArrowItem {
  /** Possible variants for a random crystalshot, so addons can register their own if desired */
  public static final List<String> RANDOM_VARIANTS;
  static {
    RANDOM_VARIANTS = new ArrayList<>();
    RANDOM_VARIANTS.add("amethyst");
    RANDOM_VARIANTS.add("earthslime");
    RANDOM_VARIANTS.add("skyslime");
    RANDOM_VARIANTS.add("ichor");
    RANDOM_VARIANTS.add("enderslime");
    RANDOM_VARIANTS.add("quartz");
  }
  /** NBT key for variants on the stack and entity */
  public static final String TAG_VARIANT = "variant";
  public CrystalshotItem(Properties props) {
    super(props);
  }

  @Override
  public AbstractArrow createArrow(Level pLevel, ItemStack pStack, LivingEntity pShooter, @org.jspecify.annotations.Nullable ItemStack firedFromWeapon) {
    CrystalshotEntity arrow = new CrystalshotEntity(pLevel, pShooter);
    String variant = "random";
    CompoundTag tag = pStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    if (tag.contains(TAG_VARIANT)) {
      variant = tag.getString(TAG_VARIANT).orElse("");
    }
    if ("random".equals(variant)) {
      variant = RANDOM_VARIANTS.get(pShooter.getRandom().nextInt(RANDOM_VARIANTS.size()));
    }
    arrow.setVariant(variant);
    return arrow;
  }

  /** @deprecated kept for old addon callers; Minecraft 26.1 calls the weapon-aware overload above. */
  @Deprecated(forRemoval = true)
  public AbstractArrow createArrow(Level pLevel, ItemStack pStack, LivingEntity pShooter) {
    return createArrow(pLevel, pStack, pShooter, null);
  }

  public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
    return EnchantmentHelper.getItemEnchantmentLevel(player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.INFINITY), bow) > 0;
  }

  /** Creates a crystal shot with the given variant */
  public static ItemStack withVariant(String variant, int size) {
    ItemStack stack = new ItemStack(TinkerTools.crystalshotItem, size);
    CompoundTag tag = new CompoundTag();
    tag.putString(TAG_VARIANT, variant);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    return stack;
  }

  public static class CrystalshotEntity extends AbstractArrow {
    private static final EntityDataAccessor<String> SYNC_VARIANT = SynchedEntityData.defineId(CrystalshotEntity.class, EntityDataSerializers.STRING);

    public CrystalshotEntity(EntityType<? extends CrystalshotEntity> type, Level level) {
      super(type, level);
      setSoundEvent(Sounds.CRYSTALSHOT.getSound());
    }

    public CrystalshotEntity(Level level, LivingEntity shooter) {
      super(TinkerTools.crystalshotEntity.get(), shooter, level, ItemStack.EMPTY, null);
      setSoundEvent(Sounds.CRYSTALSHOT.getSound());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
      return new ItemStack(TinkerTools.crystalshotItem.get());
    }

    @Override
    public void setSoundEvent(SoundEvent sound) {
      if (sound != SoundEvents.ARROW_HIT && sound != SoundEvents.CROSSBOW_HIT) {
        super.setSoundEvent(sound);
      }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(SYNC_VARIANT, "");
    }

    /** Gets the texture variant of this shot */
    public String getVariant() {
      String variant = this.entityData.get(SYNC_VARIANT);
      if (variant.isEmpty()) {
        return "amethyst";
      }
      return variant;
    }

    /** Sets the arrow's variant */
    public void setVariant(String variant) {
      this.entityData.set(SYNC_VARIANT, variant);
    }

    @Override
    public ItemStack getPickupItem() {
      return withVariant(getVariant(), 1);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putString(TAG_VARIANT, getVariant());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      setVariant(input.getStringOr(TAG_VARIANT, ""));
    }
  }
}
