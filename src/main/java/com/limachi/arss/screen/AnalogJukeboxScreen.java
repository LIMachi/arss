package com.limachi.arss.screen;

import com.limachi.arss.Arss;
import com.limachi.arss.ClientRegistries;
import com.limachi.arss.menu.AnalogJukeboxMenu;
import com.limachi.arss.utils.StaticInitializer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.NonnullDefault;

@StaticInitializer.StaticClient
@NonnullDefault
@OnlyIn(Dist.CLIENT)
public class AnalogJukeboxScreen extends AbstractContainerScreen<AnalogJukeboxMenu> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Arss.MOD_ID, "textures/screen/analog_jukebox.png");

    static {
        ClientRegistries.registerMenu(AnalogJukeboxMenu.MENU, AnalogJukeboxScreen::new);
    }

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
    protected void renderBg(PoseStack ps, float tick, int mouseX, int mouseY) {
        renderBackground(ps);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        blit(ps, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }
}
