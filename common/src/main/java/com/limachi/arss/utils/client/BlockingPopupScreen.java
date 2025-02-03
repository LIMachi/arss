package com.limachi.arss.utils.client;

import com.limachi.arss.utils.math.Size2d;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class BlockingPopupScreen extends Screen {
    protected final Screen parent;
    protected Size2d backgroundSize;
    protected int left = 0;
    protected int top = 0;
    protected int tick = 0;

    protected BlockingPopupScreen(Component component, Screen parent, Size2d backgroundSize) {
        super(component);
        this.parent = parent;
        this.backgroundSize = backgroundSize != null ? backgroundSize : new Size2d(Minecraft.getInstance().getWindow().getScreenWidth(), Minecraft.getInstance().getWindow().getScreenHeight());
    }

    public void background(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {}

    @Override
    protected void init() {
        super.init();
        left = (int)(width - backgroundSize.w()) / 2;
        top = (int)(height - backgroundSize.h()) / 2;
        clearWidgets();
    }

    public void foreground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {}

    @Override
    public final void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getInstance().screen == this)
            super.renderBackground(gui, mouseX, mouseY, partialTick);
        if (parent != null) {
            gui.pose().pushPose();
            gui.pose().translate(0, 0, -100);
            parent.render(gui, mouseX, mouseY, partialTick);
            gui.pose().popPose();
        }
        background(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public final void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        super.render(gui, mouseX, mouseY, partialTick);
        foreground(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return parent == null || parent.isPauseScreen();
    }

    @Override
    public void tick() {
        super.tick();
        ++tick;
    }

    public void closing() {}

    @Override
    public final void onClose() {
        closing();
        if (minecraft != null)
            minecraft.setScreen(parent);
    }
}
