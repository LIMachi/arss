package com.limachi.arss.utils.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {
    protected final Screen parent;
    protected final ConfigManager config;

    protected int leftPos;
    protected int topPos;
    protected int imageWidth = 176;
    protected int imageHeight = 166;

    public ConfigScreen(Screen parent, ConfigManager config) {
        super(Component.literal("test config screen"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;
        addRenderableOnly(new FittingMultiLineTextWidget(leftPos + 2, topPos + 2, 250, 16, Component.literal(config.client.dump()), font));
    }
}
