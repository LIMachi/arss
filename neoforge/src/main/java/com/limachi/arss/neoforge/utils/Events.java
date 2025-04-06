package com.limachi.arss.neoforge.utils;

import com.limachi.arss.utils.IAcceptCrouchInteractWithItem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = "arss")
public class Events {
    @SubscribeEvent
    public static void acceptSneakUseOfBlockWithItem(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (player.isShiftKeyDown() && !stack.isEmpty()) {
            BlockState state = event.getLevel().getBlockState(event.getHitVec().getBlockPos());
            if (state.getBlock() instanceof IAcceptCrouchInteractWithItem a && a.overrideCrouchInteraction(stack, player, state, event.getPos())) {
                event.setUseBlock(TriState.TRUE);
                event.setUseItem(TriState.FALSE);
            }
        }
    }
}
