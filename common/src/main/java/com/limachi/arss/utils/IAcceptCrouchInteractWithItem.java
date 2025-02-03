package com.limachi.arss.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * to be implemented on blocks that expect an interaction with an item even if the player is crouching
 * (the vanilla behavior is to skip the block interaction if the player is crouching)
 */
@FunctionalInterface
public interface IAcceptCrouchInteractWithItem {
    /**
     * @return 2 tri state (the first determines if the block interaction will be used, the second if the item interaction will be used)
     */
    boolean overrideCrouchInteraction(ItemStack stack, Player player, BlockState state, BlockPos pos);
}
