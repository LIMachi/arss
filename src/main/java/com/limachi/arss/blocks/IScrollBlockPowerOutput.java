package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.lim_lib.scrollSystem.IScrollBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * can be implemented on block / blockentity
 * do remember that blocks are singleton and not blockentity when using 'this'
 */
public interface IScrollBlockPowerOutput extends IScrollBlock {

    static int clampModulus(int val, int base, int modulus) {
        while (val < base)
            val += modulus;
        while (val >= base + modulus)
            val -= modulus;
        return val;
    }

    @Override
    default void scroll(Level level, BlockPos pos, int delta, Player player) {
        BlockState state = level.getBlockState(pos);
        level.setBlock(pos, state.setValue(BlockStateProperties.POWER, clampModulus(state.getValue(BlockStateProperties.POWER) + delta, 1, 15)), 3);
    }

    @Override
    default void scrollFeedBack(Level level, BlockPos pos, int delta, Player player) {
        player.displayClientMessage(Component.literal(Integer.toString(clampModulus(level.getBlockState(pos).getValue(BlockStateProperties.POWER) + delta, 1, 15))), true);
    }

    @Override
    default boolean canScroll(Player player, BlockPos pos) {
        return (Arss.SCROLL_KEY.getKeybinding().isUnbound() || Arss.SCROLL_KEY.getState(player)) && player.level().getBlockState(pos).getValue(ArssBlockStateProperties.CAN_SCROLL);
    }

    default @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Supplier<InteractionResult> alternative) {
        Item held = player.getItemInHand(hand).getItem();
        if (held == Items.REDSTONE_TORCH || held == AnalogRedstoneTorchBlock.R_ITEM.get()) {
            boolean can_scroll = !state.getValue(ArssBlockStateProperties.CAN_SCROLL);
            level.setBlock(pos, state.setValue(ArssBlockStateProperties.CAN_SCROLL, can_scroll), 3);
            player.displayClientMessage(Component.translatable("display.arss.scrollable_block.can_scroll." + can_scroll), true);
            return InteractionResult.SUCCESS;
        }
        return alternative.get();
    }
}
