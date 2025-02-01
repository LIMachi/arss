package com.limachi.arss.utils.network;

import com.limachi.arss.utils.annotations.RegisterMsg;
import com.limachi.arss.utils.menu.IAcceptDownStreamNBT;
import com.limachi.arss.utils.menu.IAcceptUpStreamNBT;

import dev.architectury.networking.NetworkManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@RegisterMsg
public record ScreenNBTMsg(int menuId, int slot, CompoundTag data, Boolean upstream) implements IC2SMsg<ScreenNBTMsg>, IS2CMsg<ScreenNBTMsg> {
    @Override
    public void run(NetworkManager.PacketContext ctx) {
        if (upstream) {
            if (ctx.getPlayer().containerMenu.containerId == menuId && ctx.getPlayer().containerMenu instanceof IAcceptUpStreamNBT m)
                m.upstreamNBTMessage(slot, data);
        } else {
            if (ctx.getPlayer().containerMenu.containerId == menuId && ctx.getPlayer().containerMenu instanceof IAcceptDownStreamNBT m)
                m.downstreamNBTMessage(slot, data);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void send(int slot, CompoundTag nbt) {
        Player player = Minecraft.getInstance().player;
        if (player != null)
            new ScreenNBTMsg(player.containerMenu.containerId, slot, nbt, true).sendToServer();
    }

    public static void send(Player player, int slot, CompoundTag nbt) {
        if (player instanceof ServerPlayer serverPlayer)
            new ScreenNBTMsg(player.containerMenu.containerId, slot, nbt, false).sendToClient(serverPlayer);
    }
}
