package com.limachi.arss.client;

import com.limachi.arss.common.block_entities.KeyboardLectern;
import com.limachi.arss.utils.client.ClientStage;
import com.limachi.arss.utils.client.annotations.StaticInitClient;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;

@Environment(EnvType.CLIENT)
@SuppressWarnings("unused")
public class KeyboardLecternBlockEntityRenderer implements BlockEntityRenderer<KeyboardLectern> {

    @StaticInitClient(ClientStage.LAST)
    public static void register() {
        BlockEntityRendererRegistry.register(KeyboardLectern.TYPE.get(), KeyboardLecternBlockEntityRenderer::new);
    }

    protected final BlockEntityRendererProvider.Context ctx;

    public KeyboardLecternBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) { this.ctx = ctx; }

    @Override
    public void render(KeyboardLectern be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BakedModel keyboard = Minecraft.getInstance().getItemRenderer().getModel(be.getKeyboard(), be.getLevel(), null, 0);
    }
}
