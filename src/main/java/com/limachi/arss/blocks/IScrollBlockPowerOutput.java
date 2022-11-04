package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.lim_lib.scrollSystem.IScrollBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoAccessor;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * can be implemented on block / blockentity
 * do remember that blocks are singleton and not blockentity when using 'this'
 */
public interface IScrollBlockPowerOutput extends IScrollBlock, IProbeInfoAccessor {

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
        return (Arss.SCROLL_KEY.getKeybinding().isUnbound() || Arss.SCROLL_KEY.getState(player)) && player.level.getBlockState(pos).getValue(ArssBlockStateProperties.CAN_SCROLL);
    }

    @Override
    default void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData hit) {
        if (!(state.getBlock() instanceof AnalogRedstoneBlock))
            info.text(Component.translatable("top.info.stored_power", state.getValue(BlockStateProperties.POWER).toString()));
        info.text(Component.translatable("top.info." + (state.getValue(ArssBlockStateProperties.CAN_SCROLL) ? "unlocked" : "locked")));
    }
}
