package com.limachi.arss.utils.client.screens;

import com.limachi.arss.utils.client.GUI;
import com.limachi.arss.utils.math.Rect2d;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Environment(EnvType.CLIENT)
public class SimpleContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IParentedScreen {

    protected final Screen parent;
    public SimpleContainerScreen(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        this.parent = null;
    }

    public SimpleContainerScreen(T abstractContainerMenu, Inventory inventory, Component component, Screen parent) {
        super(abstractContainerMenu, inventory, component);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
    }

    public void foreground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        foreground(guiGraphics, mouseX, mouseY, partialTick);
        if (Minecraft.getInstance().screen == this)
            renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GUI.blitBackground(guiGraphics, new Rect2d(leftPos, topPos, imageWidth, imageHeight));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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

    @Override
    public <T extends Screen & IParentedScreen> T parent() {
        return (T)parent;
    }
}
