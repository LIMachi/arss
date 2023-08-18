package com.limachi.arss.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface ICustomItemRenderers {
    ItemStack itemRenderer();
    BlockState blockRenderer();
    BlockState self();
}
