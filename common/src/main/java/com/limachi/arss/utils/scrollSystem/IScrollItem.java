package com.limachi.arss.utils.scrollSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

/**
 * can be implemented on item
 * do remember that items are singleton when using 'this'
 */

public interface IScrollItem {
    /**
     * called server side only, for calculation, only called when count down reached
     */
    void scroll(Player player, int slot, int delta);

    /**
     * called client side only, for visual/audio feedback, might be called a lot, delta should not be integrated
     */
    @Environment(EnvType.CLIENT)
    void scrollFeedBack(Player player, int slot, int delta);

    /**
     * called client side to test if the scroll is locked on this item
     */
    @Environment(EnvType.CLIENT)
    boolean canScroll(Player player, int slot);
}
