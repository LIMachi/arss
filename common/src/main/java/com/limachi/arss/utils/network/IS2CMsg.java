package com.limachi.arss.utils.network;

@FunctionalInterface
public interface IS2CMsg<T extends IS2CMsg<T>> extends IMsg<T> {
    @Override
    default Boolean upstream() {
        return false;
    }
}
