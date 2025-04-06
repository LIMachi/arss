package com.limachi.arss.client.screen;

import com.limachi.arss.common.menus.AnalogJukeboxMenu;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.client.annotations.RegisterMenuScreen;

import com.limachi.arss.utils.client.screens.SimpleBackgroundContainerScreen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@RegisterMenuScreen
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class AnalogJukeboxScreen extends SimpleBackgroundContainerScreen<AnalogJukeboxMenu> {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, "textures/screen/analog_jukebox.png");

    public AnalogJukeboxScreen(AnalogJukeboxMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 73;
    }

    @Override
    protected ResourceLocation backgroundLocation() { return BACKGROUND; }
}
