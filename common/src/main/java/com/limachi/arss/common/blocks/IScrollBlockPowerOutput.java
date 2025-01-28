package com.limachi.arss.common.blocks;

import com.limachi.arss.client.ClientDef;
import com.limachi.arss.common.ArssBlockStateProperties;
import com.limachi.arss.utils.scrollSystem.IScrollBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.Supplier;

/**
 * can be implemented on block / blockentity
 * do remember that blocks are singleton and not blockentity when using 'this'
 */
public interface IScrollBlockPowerOutput extends IScrollBlock {

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

    default ItemInteractionResult use(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Supplier<ItemInteractionResult> alternative) {
        Item held = stack.getItem();
        if ((held == Items.REDSTONE_TORCH || held == AnalogRedstoneTorchBlock.AnalogRedstoneTorchItem.R_ITEM.get()) && player.isCrouching()) {
            boolean can_scroll = !state.getValue(CAN_SCROLL);
            level.setBlock(pos, state.setValue(CAN_SCROLL, can_scroll), 3);
            player.displayClientMessage(Component.translatable("display.arss.scrollable_block.can_scroll." + can_scroll), true);
            return ItemInteractionResult.SUCCESS;
        }
        return alternative.get();
    }
}
