package slimeknights.tconstruct.gadgets.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.library.utils.Util;

public class FancyItemFrameEntity extends ItemFrame implements IEntityWithComplexSpawn {
  private static final int DIAMOND_TIMER = 300;
  private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(FancyItemFrameEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> FRAME_ROTATION = SynchedEntityData.defineId(FancyItemFrameEntity.class, EntityDataSerializers.INT);
  private static final String TAG_VARIANT = "Variant";
  private static final String TAG_ROTATION_TIMER = "RotationTimer";

  private int rotationTimer = 0;
  public FancyItemFrameEntity(EntityType<? extends FancyItemFrameEntity> type, Level level) {
    super(type, level);
  }

  public FancyItemFrameEntity(Level levelIn, BlockPos blockPos, Direction face, FrameType variant) {
    super(TinkerGadgets.itemFrameEntity.get(), levelIn);
    this.pos = blockPos;
    this.setDirection(face);
    this.entityData.set(VARIANT, variant.getId());
  }

  private static boolean doesRotate(int type) {
    return type == FrameType.GOLD.getId() || type == FrameType.REVERSED_GOLD.getId() || type == FrameType.DIAMOND.getId();
  }

  public void updateRotationTimer(boolean overturn) {
    this.rotationTimer = overturn ? -DIAMOND_TIMER : 0;
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
    if (!player.isShiftKeyDown() && getFrameId() == FrameType.CLEAR.getId() && !getItem().isEmpty()) {
      BlockPos behind = blockPosition().relative(getDirection().getOpposite());
      BlockState state = level().getBlockState(behind);
      if (!state.isAir()) {
        BlockHitResult hit = Util.createTraceResult(behind, getDirection(), false);
        InteractionResult result = state.useItemOn(player.getItemInHand(hand), level(), player, hand, hit);
        if (result.consumesAction()) {
          return result;
        }
        if (result instanceof InteractionResult.TryEmptyHandInteraction && hand == InteractionHand.MAIN_HAND) {
          result = state.useWithoutItem(level(), player, hit);
          if (result.consumesAction()) {
            return result;
          }
        }
      }
    }
    return super.interact(player, hand, location);
  }

  @Override
  public void tick() {
    super.tick();
    int frameId = getFrameId();
    Level level = level();
    if (frameId == FrameType.DIAMOND.getId()) {
      rotationTimer++;
      if (rotationTimer >= 300) {
        rotationTimer = 0;
        if (!level.isClientSide()) {
          int curRotation = getRotation();
          if (curRotation > 0) {
            this.setRotation(curRotation - 1, true);
          }
        }
      }
      return;
    }
    if (!level.isClientSide()) {
      if (doesRotate(frameId)) {
        rotationTimer++;
        if (rotationTimer >= 20) {
          rotationTimer = 0;
          int curRotation = getRotation();
          if (frameId == FrameType.REVERSED_GOLD.getId()) {
            curRotation -= 1;
            if (curRotation == -1) {
              curRotation = 7;
            }
            this.setRotation(curRotation);
          } else {
            this.setRotation(curRotation + 1);
          }
        }
      }
    }
  }

  @Override
  public void setItem(ItemStack stack) {
    super.setItem(stack);
    if (!level().isClientSide() && doesRotate(getFrameId())) {
      setRotation(0, false);
    }
  }

  private void setRotationRaw(int rotationIn, boolean updateComparator) {
    this.entityData.set(FRAME_ROTATION, rotationIn);
    if (updateComparator) {
      this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
    }
  }

  @Override
  public int getRotation() {
    return this.entityData.get(FRAME_ROTATION);
  }

  @Override
  public void setRotation(int rotationIn) {
    setRotation(rotationIn, true);
  }

  protected void setRotation(int rotationIn, boolean updateComparator) {
    this.rotationTimer = 0;
    int id = getFrameId();
    if (FrameType.hasMoreRotations(id)) {
      if (id == FrameType.DIAMOND.getId()) {
        if (!level().isClientSide() && updateComparator) {
          this.playSound(Sounds.ITEM_FRAME_CLICK.getSound(), 1.0f, 1.0f);
        }
        rotationIn = Math.min(rotationIn, 16);
      } else {
        rotationIn = rotationIn % 16;
      }
      setRotationRaw(rotationIn, updateComparator);
    } else {
      setRotationRaw(rotationIn % 8, updateComparator);
    }
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(VARIANT, 0);
    builder.define(FRAME_ROTATION, 0);
  }

  public FrameType getFrameType() {
    return FrameType.byId(this.getFrameId());
  }

  public Item getFrameItem() {
    return TinkerGadgets.itemFrame.get(getFrameType());
  }

  protected int getFrameId() {
    return this.entityData.get(VARIANT);
  }

  @Override
  protected ItemStack getFrameItemStack() {
    return new ItemStack(getFrameItem());
  }

  @Override
  public ItemStack getPickResult() {
    ItemStack held = this.getItem();
    if (held.isEmpty()) {
      return new ItemStack(getFrameItem());
    } else {
      return held.copy();
    }
  }

  @Override
  protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
    super.addAdditionalSaveData(output);
    int frameId = this.getFrameId();
    output.putInt(TAG_VARIANT, frameId);
    if (doesRotate(frameId)) {
      output.putInt(TAG_ROTATION_TIMER, rotationTimer);
    }
  }

  @Override
  protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
    super.readAdditionalSaveData(input);
    this.entityData.set(FRAME_ROTATION, (int)input.getByteOr("ItemRotation", (byte)0));
    int frameId = input.getIntOr(TAG_VARIANT, 0);
    this.entityData.set(VARIANT, frameId);
    if (doesRotate(frameId)) {
      rotationTimer = input.getIntOr(TAG_ROTATION_TIMER, 0);
    }
  }

  @Override
  public boolean fireImmune() {
    return super.fireImmune() || getFrameId() == FrameType.NETHERITE.getId();
  }

  @Override
  public boolean ignoreExplosion(Explosion explosion) {
    return super.ignoreExplosion(explosion) || getFrameId() == FrameType.NETHERITE.getId();
  }

  @Override
  public int getAnalogOutput() {
    if (this.getItem().isEmpty()) {
      return 0;
    }
    int rotation = getRotation();
    if (FrameType.hasMoreRotations(getFrameId())) {
      return rotation;
    }
    return rotation % 8 + 1;
  }

  @Override
  public void writeSpawnData(RegistryFriendlyByteBuf buf) {
    buf.writeVarInt(this.getFrameId());
    buf.writeBlockPos(this.pos);
    buf.writeVarInt(this.getDirection().get3DDataValue());
  }

  @Override
  public void readSpawnData(RegistryFriendlyByteBuf buf) {
    this.entityData.set(VARIANT, buf.readVarInt());
    this.pos = buf.readBlockPos();
    this.setDirection(Direction.from3DDataValue(buf.readVarInt()));
  }

  @Override
  protected Component getTypeName() {
    return Component.translatable(getFrameItem().getDescriptionId());
  }
}
