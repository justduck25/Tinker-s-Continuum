package slimeknights.tconstruct.library.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;

/** Compatibility helpers for the NeoForge 26.1 GUI extraction pipeline. */
public class GuiUtil {
  public static boolean isHovered(int checkX, int checkY, int x, int y, int width, int height) {
    return checkX >= x && checkY >= y && checkX < x + width && checkY < y + height;
  }

  public static void drawBackground(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, Identifier background) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, background, screen.getLeftPos(), screen.getTopPos(), 0, 0, screen.getXSize(), screen.getYSize(), 256, 256);
  }

  public static void renderHighlight(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
    graphics.fill(x, y, x + width, y + height, 0x80FFFFFF);
  }

  public static void renderPattern(GuiGraphicsExtractor graphics, Pattern pattern, int x, int y) {
    TextureAtlas blocksAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
    TextureAtlasSprite sprite = blocksAtlas.getSprite(pattern.getTexture());
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16);
  }

  public static void renderFluidTank(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, FluidStack fluid, int capacity, int x, int y, int width, int height, int z) {
    renderFluidTank(graphics, screen, fluid, fluid.getAmount(), capacity, x, y, width, height, z);
  }

  public static void renderFluidTank(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, FluidStack fluid, int amount, int capacity, int x, int y, int width, int height, int z) {
    if (!fluid.isEmpty() && capacity > 0) {
      int fluidHeight = Math.min(height * amount / capacity, height);
      renderTiledFluid(graphics, screen, fluid, x, y + height - fluidHeight, width, fluidHeight, z);
    }
  }

  public static void renderTiledFluid(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, FluidStack fluid, int x, int y, int width, int height, int z) {
    if (fluid.isEmpty() || width <= 0 || height <= 0) {
      return;
    }
    TextureAtlas blocksAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
    TextureAtlasSprite sprite = blocksAtlas.getSprite(RenderUtils.getStillTexture(fluid, fluid.getFluid().getFluidType()));
    int color = RenderUtils.getFluidColor(fluid, fluid.getFluid().getFluidType());
    if (color == -1) {
      color = 0xFFFFFFFF;
    } else if ((color & 0xFF000000) == 0) {
      color |= 0xFF000000;
    }

    int startX = screen.getLeftPos() + x;
    int startY = screen.getTopPos() + y;
      for (int yOffset = 0; yOffset < height; yOffset += 16) {
        int renderHeight = Math.min(16, height - yOffset);
        for (int xOffset = 0; xOffset < width; xOffset += 16) {
          int renderWidth = Math.min(16, width - xOffset);
          graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, startX + xOffset, startY + yOffset, renderWidth, renderHeight, color);
        }
      }
  }

  public static void drawProgressUp(GuiGraphicsExtractor graphics, slimeknights.mantle.client.screen.ScalableElementScreen bar, int x, int y, float progress) {
    bar.drawScaledYUp(graphics, x, y, (int)(bar.h * progress));
  }
}
