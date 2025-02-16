package com.limachi.arss.utils.network;

import com.limachi.arss.utils.Game;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.GameInstance;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
@FunctionalInterface
public interface IS2CMsg<T extends IS2CMsg<T>> extends IMsg<T> {
    @Override
    default Boolean upstream() { return false; }

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
        return Game.getLogical(null, ()->()->{
            var server = GameInstance.getServer();
            if (server == null)
                return false;
            return sendToClients(server.getPlayerList().getPlayers());
        }, ()->false);
    }

    default boolean sendToClients(ServerLevel level, BlockPos pos, int radius) {
        double limit = radius * radius;
        return pos == null ? sendToClients(level.getPlayers(p->true)) : sendToClients(level.getPlayers(p->p.position().distanceToSqr(pos.getCenter()) <= limit));
    }

    default boolean sendToClients(ServerLevel level, BlockPos pos) { return sendToClients(level, pos, 256); }

    default boolean sendToClients(ServerLevel level) { return sendToClients(level.getPlayers(p->true)); }
}
