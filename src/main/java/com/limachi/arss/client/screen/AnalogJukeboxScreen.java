package com.limachi.arss.client.screen;

import com.limachi.arss.Arss;
import com.limachi.arss.menu.AnalogJukeboxMenu;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.NonnullDefault;

import javax.annotation.Nonnull;

@RegisterMenuScreen(menu = "analog_jukebox")
@NonnullDefault
@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class AnalogJukeboxScreen extends AbstractContainerScreen<AnalogJukeboxMenu> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Arss.MOD_ID, "textures/screen/analog_jukebox.png");

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
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float tick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        gui.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }
}
