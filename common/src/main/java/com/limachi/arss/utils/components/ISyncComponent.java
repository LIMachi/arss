package com.limachi.arss.utils.components;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ISyncComponent<T extends ISyncComponent<T>> extends IComponent<T> {
    StreamCodec<RegistryFriendlyByteBuf, T> sync();
}
