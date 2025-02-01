package com.limachi.arss.utils.network;

import com.limachi.arss.utils.ModBase;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface IMsg<T extends IMsg<T>> extends CustomPacketPayload {
    void run(NetworkManager.PacketContext ctx);

    default Boolean upstream() { return null; }

    @Override
    default Type<T> type() {
        return (Type<T>) ModBase.registries.getMessageType(this);
    }

    default boolean sendToClient(ServerPlayer player) {
        if (NetworkManager.canPlayerReceive(player, type())) {
            NetworkManager.sendToPlayer(player, this);
            return true;
        }
        return false;
    }

    default boolean sendToClients(Iterable<ServerPlayer> players) {
        for (ServerPlayer player : players)
            if (!NetworkManager.canPlayerReceive(player, type()))
                return false;
        NetworkManager.sendToPlayers(players, this);
        return true;
    }

    default boolean sendToClients() {
        return EnvExecutor.getInEnv(Env.SERVER, ()->()->{
            var server = GameInstance.getServer();
            if (server == null)
                return false;
            return sendToClients(server.getPlayerList().getPlayers());
        }).orElse(false);
    }

    default boolean sendToServer() {
        return EnvExecutor.getInEnv(Env.CLIENT, ()->()-> {
            if (NetworkManager.canServerReceive(type())) {
                NetworkManager.sendToServer(this);
                return true;
            }
            return false;
        }).orElse(false);
    }
}