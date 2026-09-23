package slimeknights.tconstruct.tools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.aoe.AreaOfEffectIterator;
import slimeknights.tconstruct.library.tools.definition.module.aoe.AreaOfEffectIterator.AOEMatchType;
import slimeknights.tconstruct.library.tools.definition.module.mining.IsEffectiveToolHook;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.BlockSideHitListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ToolRenderEvents {
  /** Maximum number of blocks from the iterator to render */
  private static final int MAX_BLOCKS = 60;

  /**
   * Renders the outline on the extra blocks.
   *
   * @param event the outline extraction event
   */
  @SubscribeEvent
  static void renderBlockHighlights(ExtractBlockOutlineRenderStateEvent event) {
    Level world = event.getLevel();
    Player player = Minecraft.getInstance().player;
    if (world == null || player == null) {
      return;
    }

    ItemStack stack = player.getMainHandItem();
    if (stack.isEmpty() || !stack.is(TinkerTags.Items.MODIFIABLE)) {
      return;
    }

    ToolStack tool = ToolStack.from(stack);
    if (tool.isBroken()) {
      return;
    }

    BlockHitResult blockTrace = event.getHitResult();
    BlockPos origin = event.getBlockPos();
    BlockState state = event.getBlockState();
    AOEMatchType matchType = AOEMatchType.BREAKING;
    if (tool.getModifiers().has(TinkerTags.Modifiers.AOE_INTERACTION)) {
      matchType = AOEMatchType.DISPLAY;
    } else if (!IsEffectiveToolHook.isEffective(tool, state)) {
      return;
    }

    UseOnContext context = new UseOnContext(world, player, InteractionHand.MAIN_HAND, stack, blockTrace);
    Iterator<BlockPos> extraBlocks = tool.getHook(ToolHooks.AOE_ITERATOR).getBlocks(tool, context, state, matchType).iterator();
    if (!extraBlocks.hasNext()) {
      return;
    }

    Vec3 camera = event.getCamera().position();
    List<ExtraOutline> outlines = new ArrayList<>();
    int rendered = 0;
    do {
      BlockPos pos = extraBlocks.next();
      if (!pos.equals(origin) && world.getWorldBorder().isWithinBounds(pos)) {
        VoxelShape shape = world.getBlockState(pos).getShape(world, pos, event.getCollisionContext());
        if (shape.isEmpty()) {
          continue;
        }
        outlines.add(new ExtraOutline(pos.immutable(), shape));
        rendered++;
      }
    } while (rendered < MAX_BLOCKS && extraBlocks.hasNext());

    if (!outlines.isEmpty()) {
      event.addCustomRenderer(new AoeOutlineRenderer(List.copyOf(outlines), camera));
    }
  }

  private record ExtraOutline(BlockPos pos, VoxelShape shape) {}

  private record AoeOutlineRenderer(List<ExtraOutline> outlines, Vec3 camera) implements CustomBlockOutlineRenderer {
    @Override
    public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource buffer, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {
      if (translucentPass != renderState.isTranslucent()) {
        return false;
      }

      VertexConsumer vertexBuilder = buffer.getBuffer(RenderTypes.lines());
      int color = renderState.highContrast() ? 0xFFFFFFFF : 0xFF000000;
      float alpha = renderState.highContrast() ? 1.0F : 0.4F;
      for (ExtraOutline outline : outlines) {
        BlockPos pos = outline.pos();
        ShapeRenderer.renderShape(poseStack, vertexBuilder, outline.shape(), pos.getX() - camera.x(), pos.getY() - camera.y(), pos.getZ() - camera.z(), color, alpha);
      }
      return false;
    }
  }
  /**
   * Submits the crack overlay for AOE blocks. Vanilla only cracks the targeted block.
   * This runs in the same submit pass as {@code LevelRenderer#submitBlockDestroyAnimation}.
   */
  @SubscribeEvent
  static void renderBlockDamageProgress(SubmitCustomGeometryEvent event) {
    MultiPlayerGameMode controller = Minecraft.getInstance().gameMode;
    if (controller == null || !controller.isDestroying()) {
      return;
    }
    int progress = controller.getDestroyStage();
    if (progress < 0) {
      return;
    }
    Level world = Minecraft.getInstance().level;
    Player player = Minecraft.getInstance().player;
    if (world == null || player == null) {
      return;
    }
    ItemStack stack = player.getMainHandItem();
    if (stack.isEmpty() || !stack.is(TinkerTags.Items.HARVEST)) {
      return;
    }
    HitResult result = Minecraft.getInstance().hitResult;
    if (!(result instanceof BlockHitResult blockTrace) || result.getType() != HitResult.Type.BLOCK) {
      return;
    }
    ToolStack tool = ToolStack.from(stack);
    if (tool.isBroken()) {
      return;
    }
    BlockPos target = blockTrace.getBlockPos();
    BlockState state = world.getBlockState(target);
    if (!IsEffectiveToolHook.isEffective(tool, state)) {
      return;
    }
    UseOnContext context = new UseOnContext(world, player, InteractionHand.MAIN_HAND, stack, blockTrace.withDirection(BlockSideHitListener.getClientSideHit()));
    Iterator<BlockPos> extraBlocks = tool.getHook(ToolHooks.AOE_ITERATOR).getBlocks(tool, context, state, AreaOfEffectIterator.AOEMatchType.BREAKING).iterator();
    if (!extraBlocks.hasNext()) {
      return;
    }

    Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
    PoseStack poseStack = event.getPoseStack();
    SubmitNodeCollector collector = event.getSubmitNodeCollector();
    int rendered = 0;
    do {
      BlockPos pos = extraBlocks.next();
      if (pos.equals(target) || !world.getWorldBorder().isWithinBounds(pos)) {
        continue;
      }
      BlockState extraState = world.getBlockState(pos);
      if (extraState.getRenderShape() != RenderShape.MODEL) {
        continue;
      }
      BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(extraState);
      poseStack.pushPose();
      poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
      collector.submitBreakingBlockModel(poseStack, model, extraState.getSeed(pos), progress);
      poseStack.popPose();
      rendered++;
    } while (rendered < MAX_BLOCKS && extraBlocks.hasNext());
  }
}
