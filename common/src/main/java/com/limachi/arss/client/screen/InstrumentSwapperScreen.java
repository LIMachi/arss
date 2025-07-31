package com.limachi.arss.client.screen;

import com.limachi.arss.Arss;
import com.limachi.arss.common.menus.InstrumentSwapperMenu;

import com.limachi.lim_lib.client.annotations.RegisterMenuScreen;
import com.limachi.lim_lib.client.screens.SimpleBackgroundContainerScreen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@RegisterMenuScreen
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class InstrumentSwapperScreen extends SimpleBackgroundContainerScreen<InstrumentSwapperMenu> {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(Arss.INSTANCE.registries.mod_id, "textures/screen/instrument_swapper.png");

    public InstrumentSwapperScreen(InstrumentSwapperMenu menu, Inventory inv, Component title) {
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
