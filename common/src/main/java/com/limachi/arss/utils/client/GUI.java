package com.limachi.arss.utils.client;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.math.Pos2d;
import com.limachi.arss.utils.math.Rect2d;
import com.limachi.arss.utils.math.Size2d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
@SuppressWarnings("unused")
public class GUI {
    public static final Size2d DEFAULT_SIZE = new Size2d(256, 256);
    public static final Pos2d DEFAULT_POS = Pos2d.splat(0);
    public static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, "textures/screen/background.png");
    public static final Rect2d BACKGROUND_RECT = Rect2d.from(DEFAULT_SIZE);

    public static void blitScaled(GuiGraphics gui, ResourceLocation id, Rect2d blit, double scale) { blitScaled(gui, id, blit, scale, DEFAULT_POS, DEFAULT_SIZE); }
    public static void blitScaled(GuiGraphics gui, ResourceLocation id, Rect2d blit) { blitScaled(gui, id, blit, 1., DEFAULT_POS, DEFAULT_SIZE); }
    public static void blitScaled(GuiGraphics gui, ResourceLocation id, Rect2d blit, Size2d file) { blitScaled(gui, id, blit, 1., DEFAULT_POS, file); }
    public static void blitScaled(GuiGraphics gui, ResourceLocation id, Rect2d blit, double scale, Size2d file) { blitScaled(gui, id, blit, scale, DEFAULT_POS, file); }
    public static void blitScaled(GuiGraphics gui, ResourceLocation id, Rect2d blit, double scale, Pos2d image) { blitScaled(gui, id, blit, scale, image, DEFAULT_SIZE); }
    public static void blitScaled(GuiGraphics gui, ResourceLocation id, Rect2d blit, Pos2d image) { blitScaled(gui, id, blit, 1., image, DEFAULT_SIZE); }
    public static void blitScaled(GuiGraphics gui, ResourceLocation id, Rect2d blit, Pos2d image, Size2d file) { blitScaled(gui, id, blit, 1., image, file); }
    public static void blitScaled(GuiGraphics gui, ResourceLocation id, Rect2d blit, double scale, Pos2d image, Size2d file) {
        if (scale == 0)
            return;
        blitStretch(gui, id, blit, Rect2d.from(image, scale * blit.w(), scale * blit.h()), file);
    }

    public static void blitStretch(GuiGraphics gui, ResourceLocation id, Rect2d blit, Rect2d image, Size2d file) {
        if (gui == null || id == null || blit == null || image == null)
            return;
        if (file == null)
            file = DEFAULT_SIZE;
        if (blit.w() <= 0 || blit.h() <= 0 || image.w() <= 0 || image.h() <= 0)
            return;
        gui.blit(id, (int)blit.x(), (int)blit.y(), (int)blit.w(), (int)blit.h(), (float)image.x(), (float)image.y(), (int)image.w(), (int)image.h(), (int)file.w(), (int)file.h());
    }

    public static void blitRepeating(GuiGraphics gui, ResourceLocation id, Rect2d blit) { blitRepeating(gui, id, blit, Rect2d.from(DEFAULT_SIZE), DEFAULT_SIZE); }
    public static void blitRepeating(GuiGraphics gui, ResourceLocation id, Rect2d blit, Size2d file) { blitRepeating(gui, id, blit, Rect2d.from(file), file); }
    public static void blitRepeating(GuiGraphics gui, ResourceLocation id, Rect2d blit, Rect2d image) { blitRepeating(gui, id, blit, image, DEFAULT_SIZE); }
    public static void blitRepeating(GuiGraphics gui, ResourceLocation id, Rect2d blit, Rect2d image, Size2d file) {
        if (blit.w() <= image.w()) {
            if (blit.h() <= image.h())
                blitStretch(gui, id, blit, Rect2d.from(image.pos(), blit.size()), file);
            else {
                double dy = 0;
                double h = blit.h();
                while (h > image.h()) {
                    blitStretch(gui, id, new Rect2d(blit.x(), blit.y() + dy, blit.w(), image.h()), image.withW(blit.w()), file);
                    h -= image.h();
                    dy += image.h();
                }
                if (h > 0)
                    blitStretch(gui, id, blit.withY(blit.y() + dy), Rect2d.from(image.pos(), blit.w(), h), file);
            }
        } else {
            double dx = 0;
            double w = blit.w();
            while (w > image.w()) {
                blitRepeating(gui, id, new Rect2d(blit.x() + dx, blit.y(), image.w(), blit.h()), image, file);
                w -= image.w();
                dx += image.w();
            }
            if (w > 0)
                blitRepeating(gui, id, new Rect2d(blit.x() + dx, blit.y(), w, blit.h()), image.withW(w), file);
        }
    }

    public static void blit9Slices(GuiGraphics gui, ResourceLocation id, Rect2d blit, double border, Size2d file) { blit9Slices(gui, id, blit, Rect2d.from(file), Size2d.splat(border), Size2d.splat(border), file); }
    public static void blit9Slices(GuiGraphics gui, ResourceLocation id, Rect2d blit, double border) { blit9Slices(gui, id, blit, Rect2d.from(DEFAULT_SIZE), Size2d.splat(border), Size2d.splat(border), DEFAULT_SIZE); }
    public static void blit9Slices(GuiGraphics gui, ResourceLocation id, Rect2d blit, Size2d topLeft, Size2d bottomRight) { blit9Slices(gui, id, blit, Rect2d.from(DEFAULT_SIZE), topLeft, bottomRight, DEFAULT_SIZE); }
    public static void blit9Slices(GuiGraphics gui, ResourceLocation id, Rect2d blit, Size2d topLeft, Size2d bottomRight, Size2d file) { blit9Slices(gui, id, blit, Rect2d.from(file), topLeft, bottomRight, file); }
    public static void blit9Slices(GuiGraphics gui, ResourceLocation id, Rect2d blit, Rect2d image, double border, Size2d file) { blit9Slices(gui, id, blit, image, Size2d.splat(border), Size2d.splat(border), file); }
    public static void blit9Slices(GuiGraphics gui, ResourceLocation id, Rect2d blit, Rect2d image, double border) { blit9Slices(gui, id, blit, image, Size2d.splat(border), Size2d.splat(border), DEFAULT_SIZE); }
    public static void blit9Slices(GuiGraphics gui, ResourceLocation id, Rect2d blit, Rect2d image, Size2d topLeft, Size2d bottomRight) { blit9Slices(gui, id, blit, image, topLeft, bottomRight, DEFAULT_SIZE); }
    public static void blit9Slices(GuiGraphics gui, ResourceLocation id, Rect2d blit, Rect2d image, Size2d topLeft, Size2d bottomRight, Size2d file) {

    }

    public static void blit4Corners(GuiGraphics gui, ResourceLocation id, Rect2d blit) { blit4Corners(gui, id, blit, Rect2d.from(DEFAULT_SIZE), DEFAULT_SIZE); }
    public static void blit4Corners(GuiGraphics gui, ResourceLocation id, Rect2d blit, Size2d file) { blit4Corners(gui, id, blit, Rect2d.from(file), file); }
    public static void blit4Corners(GuiGraphics gui, ResourceLocation id, Rect2d blit, Rect2d image) { blit4Corners(gui, id, blit, image, DEFAULT_SIZE); }
    public static void blit4Corners(GuiGraphics gui, ResourceLocation id, Rect2d blit, Rect2d image, Size2d file) {
        if (blit.size().equals(image.size()))
            blitScaled(gui, id, blit, image.pos(), file);
        else {
            Size2d block = blit.size().mul(0.5);
            blitRepeating(gui, id, Rect2d.from(blit.pos(), block), Rect2d.from(image.pos(), block), file);
            blitRepeating(gui, id, Rect2d.from(blit.pos().addX(block.w()), block), Rect2d.from(image.x1() - block.w(), image.y0(), block), file);
            blitRepeating(gui, id, Rect2d.from(blit.pos().addY(block.h()), block), Rect2d.from(image.x0(), image.y1() - block.h(), block), file);
            blitRepeating(gui, id, Rect2d.from(blit.pos().add(block.asPos()), block), Rect2d.from(image.bottomRight().sub(block.asPos()), block), file);
        }
    }

    public static void blitBackground(GuiGraphics gui, Rect2d area) {
        blit4Corners(gui, BACKGROUND_TEXTURE, area, BACKGROUND_RECT, DEFAULT_SIZE);
    }

    public static Vector4f expandColor(int color, boolean isShadow) {
        float shadow = isShadow ? 0.25f : 1.0f;
        return new Vector4f(
                (float)(color >> 16 & 255) / 255.0F * shadow,
                (float)(color >> 8 & 255) / 255.0F * shadow,
                (float)(color & 255) / 255.0F * shadow,
                (float)(color >> 24 & 255) / 255.0F);
    }
}
