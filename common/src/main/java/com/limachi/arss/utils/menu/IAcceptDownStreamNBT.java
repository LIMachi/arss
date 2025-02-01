package com.limachi.arss.utils.menu;

import net.minecraft.nbt.CompoundTag;

public interface IAcceptDownStreamNBT {
    void downstreamNBTMessage(int slot, CompoundTag data);
}
