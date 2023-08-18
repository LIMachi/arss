package com.limachi.arss.client;

import com.limachi.arss.Arss;
//import com.limachi.arss.blocks.IRenderDotInGUI;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Arss.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    public static final ResourceLocation ARSS_OVERLAY = new ResourceLocation(Arss.MOD_ID, "textures/item/arss_decal.png");

    /*
    @SubscribeEvent
    public static void redstoneDotOverlay(ContainerScreenEvent.Render.Foreground event) {
        AbstractContainerScreen<?> screen = event.getContainerScreen();
        AbstractContainerMenu menu = screen.getMenu();
        GuiGraphics gg = event.getGuiGraphics();
        PoseStack pose = gg.pose();
        boolean blitting = false;
        for (Slot slot : menu.slots) {
            if (!slot.hasItem())
                continue;
            ItemStack stack = slot.getItem();
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof IRenderDotInGUI) {
                if (!blitting) {
                    blitting = true;
                    pose.pushPose();
                    pose.translate(0., 0., 350.);
                }
                gg.blit(ARSS_OVERLAY, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
            }
        }
        if (blitting)
            pose.popPose();
    }
     */
}
