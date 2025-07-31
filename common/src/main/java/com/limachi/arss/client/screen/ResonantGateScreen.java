package com.limachi.arss.client.screen;

import com.limachi.arss.common.block_entities.ResonantGateBlockEntity;

import com.limachi.lim_lib.client.screens.SimpleScreen;

import com.limachi.lim_lib.client.widgets.TextEditor;

import com.limachi.lim_lib.common.annotations.RegisterMsg;
import com.limachi.lim_lib.common.network.IC2SMsg;

import dev.architectury.networking.NetworkManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ResonantGateScreen extends SimpleScreen {

    public static void client_open(BlockPos target) {
        if (Minecraft.getInstance().isSameThread())
            Minecraft.getInstance().setScreen(new ResonantGateScreen(target));
    }

    protected TextEditor namer;
    protected final BlockPos target;

    int prevSuggestionLength;

    public ResonantGateScreen(BlockPos target) {
        super();
        this.target = target;
        imageWidth = 200;
        imageHeight = 100;
        prevSuggestionLength = ResonantGateBlockEntity.clientNames.size();
        new ResonantGateBlockEntity.RequestNames().sendToServer();
    }

    @RegisterMsg
    public record SetNetworkFromScreen(String frequency, BlockPos pos) implements IC2SMsg<SetNetworkFromScreen> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            if (ctx.getPlayer().level().getBlockEntity(pos) instanceof ResonantGateBlockEntity be)
                be.changeFrequency(frequency);
        }
    }

    public ResonantGateBlockEntity getBlockEntity() {
        if (Minecraft.getInstance().level instanceof Level level && level.getBlockEntity(target) instanceof ResonantGateBlockEntity be)
            return be;
        return null;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (prevSuggestionLength != ResonantGateBlockEntity.clientNames.size())
            namer.updateSuggestions(ResonantGateBlockEntity.clientNames);
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        boolean first = namer == null;
        var be = getBlockEntity();
        addRenderableWidget(namer = TextEditor.builder(leftPos + 10, topPos + 10, namer)
                .width(180)
                .suggestions(ResonantGateBlockEntity.clientNames)
                .maxSuggestions(7)
                .suggestionsBelow(true)
                .build());
        if (first && be != null)
            namer.setValue(be.getFrequency());
    }

    @Override
    public boolean stillValid() { return getBlockEntity() != null; }

    @Override
    public void closing() { new SetNetworkFromScreen(namer.getValue(), target).sendToServer(); }
}
