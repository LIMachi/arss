package com.limachi.arss.utils.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;

@FunctionalInterface
public interface IC2SMsg<T extends IC2SMsg<T>> extends IMsg<T> {
    @Override
    default Boolean upstream() { return true; }

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
