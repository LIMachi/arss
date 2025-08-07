package com.limachi.arss.common.blocks;

import com.limachi.arss.client.ClientDef;

import com.limachi.arss.common.ArssBlockBehaviors;

import com.limachi.lim_lib.client.annotations.FabricLayer;
import com.limachi.lim_lib.client.annotations.HasRedstoneTint;

import com.limachi.lim_lib.common.annotations.RegisterBlock;
import com.limachi.lim_lib.common.annotations.RegisterBlockItem;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

import static com.limachi.arss.common.ArssBlockStateProperties.HIDE_DOT;

@SuppressWarnings("unused")
public class AnalogRedstoneBlock extends PoweredBlock implements IScrollAndLockPower {

    @HasRedstoneTint
    @FabricLayer("cutout")
    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

    @HasRedstoneTint
    @RegisterBlockItem
    public static RegistrySupplier<BlockItem> R_ITEM;

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        ClientDef.commonHoverText("analog_redstone_block", list);
    }

    public AnalogRedstoneBlock() {
        super(Properties.ofFullCopy(Blocks.REDSTONE_BLOCK));
        registerDefaultState(stateDefinition.any().setValue(POWER, 15).setValue(CAN_SCROLL, true).setValue(HIDE_DOT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER, CAN_SCROLL, HIDE_DOT);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWER);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ArssBlockBehaviors.useItemOn(stack, state, level, pos, player, hand, hit, ArssBlockBehaviors::useItemOnScrollableBlockPowerToLock, ArssBlockBehaviors::useItemOnRedstoneDotBlock, super::useItemOn);
    }
}
