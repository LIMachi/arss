package com.limachi.arss.utils.client.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class SimpleBackgroundScreen extends SimpleScreen {
    public SimpleBackgroundScreen(Component component) { super(component); }

    public SimpleBackgroundScreen() { super(); }

    public SimpleBackgroundScreen(Component component, Screen parent) { super(component, parent); }

    public SimpleBackgroundScreen(Screen parent) { super(parent); }

    protected abstract ResourceLocation backgroundLocation();
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(backgroundLocation(), leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }
}
