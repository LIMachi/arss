package com.limachi.arss.utils.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

public abstract class ClassMsg<T extends IMsg<T>> implements IMsg<T> {
    public void write(RegistryFriendlyByteBuf buf) {}
    public T read(RegistryFriendlyByteBuf buf) { return (T)this; }
}
