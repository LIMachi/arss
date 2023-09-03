package com.limachi.arss.client;

import com.limachi.arss.blockEntities.KeyboardLecternBlockEntity;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@StaticInitClient
@SuppressWarnings("unused")
public class KeyboardLecternBlockEntityRenderer implements BlockEntityRenderer<KeyboardLecternBlockEntity> {

    static {
        ClientRegistries.setBer(KeyboardLecternBlockEntity.TYPE, KeyboardLecternBlockEntityRenderer::new);
    }

    protected final BlockEntityRendererProvider.Context ctx;

    public KeyboardLecternBlockEntityRenderer(@Nonnull BlockEntityRendererProvider.Context ctx) { this.ctx = ctx; }

    @Override
    public void render(KeyboardLecternBlockEntity be, float partialTick, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int light, int overlay) {
        BakedModel keyboard = Minecraft.getInstance().getItemRenderer().getModel(be.getKeyboard(), be.getLevel(), null, 0);

    }
}
