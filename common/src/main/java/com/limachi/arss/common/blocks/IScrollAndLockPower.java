package com.limachi.arss.common.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.client.ClientDef;
import com.limachi.arss.utils.IAcceptCrouchInteractWithItem;
import com.limachi.arss.utils.scrollSystem.IScrollBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface IScrollAndLockPower extends IScrollBlock, IAcceptCrouchInteractWithItem {
    @Override
    default boolean overrideCrouchInteraction(ItemStack stack, Player player, BlockState state, BlockPos pos) {
        return Arss.isWrench(stack);
    }

    IntegerProperty POWER = BlockStateProperties.POWER;

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
        level.setBlock(pos, state.setValue(POWER, clampModulus(state.getValue(POWER) + delta, 1, 15)), 3);
    }

    @Environment(EnvType.CLIENT)
    @Override
    default void scrollFeedBack(Level level, BlockPos pos, int delta, Player player) {
        player.displayClientMessage(Component.literal(Integer.toString(clampModulus(level.getBlockState(pos).getValue(POWER) + delta, 1, 15))), true);
    }

    @Environment(EnvType.CLIENT)
    @Override
    default boolean canScroll(Player player, BlockPos pos) {
        return (ClientDef.SCROLL_KEY.isUnbound() || ClientDef.SCROLL_KEY.isDown()) && player.level().getBlockState(pos).getValue(CAN_SCROLL);
    }
}
