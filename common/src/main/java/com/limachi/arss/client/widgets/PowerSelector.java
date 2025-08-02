package com.limachi.arss.client.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RedStoneWireBlock;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class PowerSelector extends AbstractWidget {
    public boolean negative;
    public boolean unknown;
    public boolean active;
    public boolean hexadecimal;
    public int value;
    public Consumer<PowerSelector> onChange;

    public PowerSelector(int x, int y, Component title, PowerSelector prev) {
        super(x, y, prev != null ? prev.getWidth() : 11, prev != null ? prev.getHeight() : 11, title);
        if (prev != null) {
            negative = prev.negative;
            unknown = prev.unknown;
            active = prev.active;
            value = prev.value;
            hexadecimal = prev.hexadecimal;
        } else {
            negative = true;
            unknown = false;
            active = true;
            value = 0;
            hexadecimal = true;
        }
    }

    public static int color(int value) {
        if (value >= 0 && value < 16)
            return RedStoneWireBlock.getColorForPower(value) | 0xFF000000;
        return RedStoneWireBlock.getColorForPower(8) | 0xFF000000;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        int color;
        Font font = Minecraft.getInstance().font;
        int dy = Math.round((getHeight() - font.lineHeight) / 2f);
        int dx = Math.round((getWidth() - font.width("0")) / 2f);
        if (negative) {
            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color(value));
            color = -1;
        } else
            color = color(value);
        if (value >= 0 && value < 16) {
            if (hexadecimal)
                guiGraphics.drawString(font, "0123456789ABCDEF".substring(value, value + 1), getX() + dx, getY() + dy, color);
            else
                guiGraphics.drawString(font, "" + value, getX() + dx, getY() + dy, color);
        } else
            guiGraphics.drawString(font, "?", getX() + dx, getY() + dy, color);
        guiGraphics.drawString(font, getMessage(), getX() - font.width(getMessage()) - 1, getY() + dy, 4210752, false);
    }

    public void applyOffset(int offset) {
        int prev = value;
        value = Mth.clamp(value + offset, 0, 15 + (unknown ? 1 : 0));
        if (prev != value && onChange != null)
            onChange.accept(this);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (active && isMouseOver(d, e)) {
            applyOffset(g > 0 ? 1 : -1);
            return true;
        }
        return super.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (active && isMouseOver(d, e)) {
            int offset = 0;
            if (i == GLFW.GLFW_MOUSE_BUTTON_1)
                offset = 1;
            else if (i == GLFW.GLFW_MOUSE_BUTTON_2)
                offset = -1;
            if (offset != 0) {
                if (Screen.hasShiftDown())
                    offset *= 5;
                else if (Screen.hasControlDown())
                    offset *= 15;
                applyOffset(offset);
                return true;
            }
        }
        return super.mouseClicked(d, e, i);
    }

    protected Tooltip unknownTooltip = null;

    protected static final Tooltip[] decimals = Util.make(new Tooltip[16], t->{
        for (int i = 0; i < 16; ++i)
            t[i] = Tooltip.create(Component.translatable("values.decimal." + i));
    });
    protected static final Tooltip[] hexa = Util.make(new Tooltip[16], t->{
        for (int i = 0; i < 16; ++i)
            t[i] = Tooltip.create(Component.translatable("values.hexa." + i));
    });

    public PowerSelector setUnknownTooltip(Tooltip tooltip) {
        unknownTooltip = tooltip;
        return this;
    }

    @Override
    public @Nullable Tooltip getTooltip() {
//        return value < 16 ? hexadecimal ? hexa[value] : decimals[value] : unknownTooltip;
        return null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
