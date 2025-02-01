package com.limachi.arss.utils.network;

@FunctionalInterface
public interface IC2SMsg<T extends IC2SMsg<T>> extends IMsg<T> {
    @Override
    default Boolean upstream() {
        return true;
    }
}
