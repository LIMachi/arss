package com.limachi.arss.blocks.scrollSystem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * can be implemented on block / blockentity
 * do remember that blocks are singleton and not blockentity when using 'this'
 */
public interface IScrollBlock {
    /**
     * called server side only, for calculation, only called when count down reached
     */
    void scroll(Level level, BlockPos pos, int delta, Player player);

    /**
     * called client side only, for visual/audio feedback, might be called a lot, delta should not be integrated
     */
    void scrollFeedBack(Level level, BlockPos pos, int delta, Player player);
}
