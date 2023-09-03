package com.limachi.arss.client;

import com.limachi.arss.Arss;
import com.limachi.arss.items.ICustomItemRenderers;
import com.limachi.arss.blocks.AnalogRedstoneTorchBlock;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import javax.annotation.Nonnull;
import java.lang.Math;
import java.util.Iterator;

@SuppressWarnings({"unused", "deprecation"})
@OnlyIn(Dist.CLIENT)
public class CustomItemStackRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation ARSS_OVERLAY = new ResourceLocation(Arss.MOD_ID, "textures/item/arss_decal.png");

    private static CustomItemStackRenderer INSTANCE = null;

    public CustomItemStackRenderer(BlockEntityRenderDispatcher berDispatcher, EntityModelSet models) {
        super(berDispatcher, models);
    }

    public static CustomItemStackRenderer getInstance() {
        if (INSTANCE == null) {
            Minecraft mc = Minecraft.getInstance();
            INSTANCE = new CustomItemStackRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        }
        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, @Nonnull ItemDisplayContext ctx, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (stack.getItem() instanceof ICustomItemRenderers bi) {

            Minecraft mc = Minecraft.getInstance();

            if (ctx == ItemDisplayContext.GUI) {

                GuiGraphics gg = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

                Iterator<PoseStack.Pose> it = pose.poseStack.descendingIterator();
                if (it.hasNext()) {
                    it.next();
                    if (it.hasNext()) {
                        Matrix4f mat = it.next().pose();
                        gg.pose().translate(mat.m30() - 8, mat.m31() - 8, mat.m32() + 50); //reverse lines GuiGraphics::473 offset, except on z axis where we increment to make sure the render will show above model, ony use the translation and discard scaling/rotation
                    }
                }

                ItemStack itemRender = bi.itemRenderer();
                BlockState blockRender = bi.blockRenderer();
                if (itemRender != null) {
                    gg.pose().pushPose();
                    gg.pose().translate(0., 0., -200.);
                    gg.renderItem(itemRender, 0, 0);
                    gg.pose().popPose();
                } else if (blockRender != null)
                    mc.getBlockRenderer().renderSingleBlock(blockRender, pose, buffer, combinedLight, combinedOverlay);
                RenderSystem.enableDepthTest();
                gg.blit(ARSS_OVERLAY, 0, 0, 0, 0, 16, 16, 16, 16);

            } else {
                pose.pushPose();
                pose.rotateAround(new Quaternionf(new AxisAngle4d(-Math.PI / 2., new Vector3f(0, 1, 0))), 0, 0, 0);
                pose.translate(0, 0, -1);
                if (bi.self().is(AnalogRedstoneTorchBlock.R_BLOCK.get()))
                    pose.translate(0, 0.25, 0);
                mc.getBlockRenderer().renderSingleBlock(bi.self(), pose, buffer, combinedLight, combinedOverlay);
                pose.popPose();
            }
        }
    }
}
