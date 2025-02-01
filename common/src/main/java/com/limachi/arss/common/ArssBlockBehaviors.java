package com.limachi.arss.common;

import com.limachi.arss.common.blocks.AnalogRedstoneTorch;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import static com.limachi.arss.common.ArssBlockStateProperties.HIDE_DOT;
import static com.limachi.arss.utils.scrollSystem.IScrollBlock.CAN_SCROLL;

public class ArssBlockBehaviors {

    @FunctionalInterface
    public interface UseItemOn {
        ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit);
    }

    public static boolean isWrench(ItemStack stack) {
        return stack.is(Items.REDSTONE_TORCH) || stack.is(AnalogRedstoneTorch.R_ITEM.get());
    }

    public static ItemInteractionResult useItemOnRedstoneDotBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (isWrench(stack) && !player.isCrouching()) {
            level.setBlock(pos, state.setValue(HIDE_DOT, !state.getValue(HIDE_DOT)), 3);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static ItemInteractionResult useItemOnScrollableBlockPowerToLock(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (isWrench(stack) && player.isCrouching()) {
            boolean can_scroll = !state.getValue(CAN_SCROLL);
            level.setBlock(pos, state.setValue(CAN_SCROLL, can_scroll), 3);
            player.displayClientMessage(Component.translatable("display.arss.scrollable_block.can_scroll." + can_scroll), true);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, UseItemOn ... actions) {
        ItemInteractionResult res = ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        for (UseItemOn use : actions)
            if ((res = use.useItemOn(stack, state, level, pos, player, hand, hit)).consumesAction())
                break;
        return res;
    }
}
