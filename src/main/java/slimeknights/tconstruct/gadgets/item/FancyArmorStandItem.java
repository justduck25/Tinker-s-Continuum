package slimeknights.tconstruct.gadgets.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.gadgets.entity.FancyArmorStandEntity;
import slimeknights.tconstruct.gadgets.entity.FancyArmorStandEntity.StandType;

import java.util.function.Consumer;

/** Item to place {@link FancyArmorStandEntity} */
public class FancyArmorStandItem extends Item {
  private final StandType type;
  public FancyArmorStandItem(Properties properties, StandType type) {
    super(properties);
    this.type = type;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Direction direction = context.getClickedFace();
    if (direction == Direction.DOWN) {
      return InteractionResult.FAIL;
    }

    Level level = context.getLevel();
    BlockPos pos = new BlockPlaceContext(context).getClickedPos();
    Vec3 center = Vec3.atBottomCenterOf(pos);
    AABB bounds = TinkerGadgets.armorStandEntity.get().getDimensions().makeBoundingBox(center.x(), center.y(), center.z());
    if (!level.noCollision(null, bounds) || !level.getEntities(null, bounds).isEmpty()) {
      return InteractionResult.FAIL;
    }

    ItemStack stack = context.getItemInHand();
    if (level instanceof ServerLevel server) {
      Player player = context.getPlayer();
      FancyArmorStandEntity stand = TinkerGadgets.armorStandEntity.get().create(server, EntitySpawnReason.SPAWN_ITEM_USE);
      if (stand == null) {
        return InteractionResult.FAIL;
      }
      var data = stack.get(DataComponents.ENTITY_DATA);
      if (data != null) {
        EntityType.updateCustomEntityTag(server, player, stand, data);
      }
      type.onPlace(stand);
      if (player != null) {
        stand.setLeft((context.getHand() == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.LEFT));
      }

      float rot = Mth.floor((Mth.wrapDegrees(context.getRotation() - 180f) + 22.5f) / 45f) * 45f;
      stand.snapTo(center.x(), center.y(), center.z(), rot, 0.0F);
      server.addFreshEntityWithPassengers(stand);
      level.playSound(null, stand.getX(), stand.getY(), stand.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
      stand.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
    }

    stack.shrink(1);
    return InteractionResult.SUCCESS;
  }
}
