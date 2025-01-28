package com.limachi.arss.common.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface ICustomItemRenderers {
    ItemStack itemRenderer();
    BlockState blockRenderer();
    BlockState self();
}
