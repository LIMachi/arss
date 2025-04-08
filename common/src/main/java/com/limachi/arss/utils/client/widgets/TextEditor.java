package com.limachi.arss.utils.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class TextEditor extends EditBox {

    @Environment(EnvType.CLIENT)
    public static class Builder {
        protected int x, y, widthInChars;
        protected TextEditor prev;
        protected Font font;
        protected Component title;
        protected Integer width, height;
        protected int insertColor = -3092272, textColor = 14737632;
        protected Consumer<String> inputConsumer;

        public Builder(int x, int y, TextEditor previousInstance) {
            this.x = x;
            this.y = y;
            widthInChars = 16;
            prev = previousInstance;
            if (previousInstance != null) {
                font = previousInstance.font;
                title = previousInstance.getMessage();
                width = previousInstance.width;
                height = previousInstance.height;
            }
        }

        public Builder width(Integer width) { this.width = width; return this; }
        public Builder height(Integer height) { this.height = height; return this; }
        public Builder font(Font font) { this.font = font; return this; }
        public Builder title(Component title) { this.title = title; return this; }
        public Builder widthInChars(int width) { widthInChars = width; return this; }
        public Builder consumer(Consumer<String> inputConsumer) { this.inputConsumer = inputConsumer; return this; }

        public TextEditor build() {
            if (font == null)
                font = Minecraft.getInstance().font;
            if (width == null)
                width = font.width("W") * widthInChars + 4;
            if (height == null)
                height = font.lineHeight + 4;
            if (title == null)
                title = Component.empty();
            if (inputConsumer == null)
                inputConsumer = s->{};
            return new TextEditor(font, x, y, width, height, prev, title, inputConsumer);
        }
    }

    protected Font font;
    protected int backgroundColor = 0;
    protected int unfocusedBorderColor = 0;
    protected int focusedBorderColor = 0;
    protected Consumer<String> inputConsumer;
    protected TextSuggestions suggestions;

    protected TextEditor(Font font, int x, int y, int width, int height, TextEditor previousInstance, Component title, Consumer<String> inputConsumer) {
        super(font, x, y, width, height, previousInstance, title);
        this.inputConsumer = inputConsumer;
        this.font = font;
    }

    protected void setSuggestions(TextSuggestions suggestions) {
        this.suggestions = suggestions;
    }

    protected void renderBackground(GuiGraphics guiGraphics) {
        guiGraphics.fill(getX() + 1, getY() + 1, getX() + width - 2, getY() + height - 2, backgroundColor);
        int color = backgroundColor;
        guiGraphics.setColor((float) ((color & 0xFF0000) >> 16) / 255f, (float) ((color & 0xFF00) >> 8) / 255f, (float) (color & 0xFF) / 255f, alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.hLine(getX(), getX() + width, getY(), -1);
        guiGraphics.hLine(getX(), getX() + width, getY() + height, -1);
        guiGraphics.vLine(getX(), getY(), getY() + height, -1);
        guiGraphics.vLine(getX() + width, getY(), getY() + height, -1);
        guiGraphics.setColor(1, 1, 1, 1);
    }

    public void finish(boolean andConsume) {
        if (andConsume)
            inputConsumer.accept(getValue());
        setEditable(false);
        setFocused(false);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if ((isActive() && isFocused()) || (suggestions != null && suggestions.isActive() && suggestions.isFocused())) {
            if (suggestions != null && suggestions.keyPressed(i, j, k))
                return true;
            switch (i) {
                case GLFW.GLFW_KEY_ENTER /*257*/-> {
                    if (canConsumeInput()) {
                        finish(true);
                        return true;
                    }
                }
                case GLFW.GLFW_KEY_ESCAPE /*256*/-> {
                    if (canConsumeInput()) {
                        finish(false);
                        return true;
                    }
                }
            }
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    protected void onDrag(double d, double e, double f, double g) {
        super.onDrag(d, e, f, g);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (suggestions != null && suggestions.mouseClicked(d, e, i))
            return true;
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (suggestions != null && suggestions.mouseScrolled(d, e, f, g))
            return true;
        return super.mouseScrolled(d, e, f, g);
    }
}
