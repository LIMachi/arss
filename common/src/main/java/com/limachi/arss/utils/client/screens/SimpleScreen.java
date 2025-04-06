package com.limachi.arss.utils.client.screens;

import com.limachi.arss.utils.client.GUI;
import com.limachi.arss.utils.math.Rect2d;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class SimpleScreen extends Screen implements IParentedScreen {
    protected int leftPos;
    protected int topPos;
    protected int centerX;
    protected int centerY;
    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected final Screen parent;

    public SimpleScreen(Component component) {
        super(component);
        parent = null;
    }

    public SimpleScreen() {
        super(Component.empty());
        parent = null;
    }

    public SimpleScreen(Component component, Screen parent) {
        super(component);
        this.parent = parent;
    }

    public SimpleScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    protected void init() {
        centerX = width / 2;
        centerY = height / 2;
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;
        clearWidgets();
    }

    public void renderFg(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GUI.blitBackground(guiGraphics, new Rect2d(leftPos, topPos, imageWidth, imageHeight));
    }

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!stillValid()) {
            onClose();
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderFg(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public final void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (shouldRenderBlur())
            renderTransparentBackground(guiGraphics);
        if (parent != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, -100);
            parent.render(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.pose().popPose();
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() { return parent == null || parent.isPauseScreen(); }

    public void closing() {}

    @Override
    public final void onClose() {
        closing();
        if (minecraft != null)
            minecraft.setScreen(parent);
    }

    public boolean stillValid() { return true; }

    public boolean keyPressed(int i, int j, int k) {
        if (getFocused() != null && getFocused().keyPressed(i, j, k))
            return true;
        else if (i == 256 && shouldCloseOnEsc()) {
            onClose();
            return true;
        } else {
            FocusNavigationEvent event = switch (i) {
                case 258 -> new FocusNavigationEvent.TabNavigation(!hasShiftDown());
                case 262 -> new FocusNavigationEvent.ArrowNavigation(ScreenDirection.RIGHT);
                case 263 -> new FocusNavigationEvent.ArrowNavigation(ScreenDirection.LEFT);
                case 264 -> new FocusNavigationEvent.ArrowNavigation(ScreenDirection.DOWN);
                case 265 -> new FocusNavigationEvent.ArrowNavigation(ScreenDirection.UP);
                default -> null;
            };
            if (event != null) {
                ComponentPath componentPath = nextFocusPath(event);
                if (componentPath == null && event instanceof FocusNavigationEvent.TabNavigation) {
                    clearFocus();
                    componentPath = nextFocusPath(event);
                }
                if (componentPath != null)
                    changeFocus(componentPath);
            }
            return false;
        }
    }

    @Override
    public <T extends Screen & IParentedScreen> T parent() {
        return (T)parent;
    }
}
