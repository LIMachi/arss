package com.limachi.arss.blocks.scrollSystem;

import com.limachi.arss.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * can be implemented on block / blockentity
 * do remember that blocks are singleton and not blockentity when using 'this'
 */
public interface IScrollBlockPowerOutput extends IScrollBlock {
    default void scroll(Level level, BlockPos pos, int delta, Player player) {
        BlockState state = level.getBlockState(pos);
        level.setBlock(pos, state.setValue(BlockStateProperties.POWER, MathUtils.clampModulus(state.getValue(BlockStateProperties.POWER) + delta, 1, 15)), 3);
    }

    default void scrollFeedBack(Level level, BlockPos pos, int delta, Player player) {
        player.displayClientMessage(new TextComponent(Integer.toString(MathUtils.clampModulus(level.getBlockState(pos).getValue(BlockStateProperties.POWER) + delta, 1, 15))), true);
    }
}
