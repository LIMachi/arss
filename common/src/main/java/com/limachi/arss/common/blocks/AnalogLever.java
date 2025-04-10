package com.limachi.arss.common.blocks;

import com.limachi.arss.client.ClientDef;

import com.limachi.arss.common.ArssBlockBehaviors;

import com.limachi.lim_lib.client.annotations.FabricLayer;

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
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

@SuppressWarnings("unused")
public class AnalogLever extends LeverBlock implements IScrollAndLockPower {

    @HasRedstoneTint
    @FabricLayer("cutout")
    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

    @RegisterBlockItem
    public static RegistrySupplier<BlockItem> R_ITEM;

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, components, flags);
        ClientDef.commonHoverText("analog_lever", components);
    }

    public AnalogLever() {
        super(Properties.ofFullCopy(Blocks.LEVER));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(FACE, AttachFace.WALL).setValue(POWER, 15).setValue(CAN_SCROLL, true));
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWERED) ? state.getValue(POWER) : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWERED) && getConnectedDirection(state) == dir ? state.getValue(POWER) : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
        builder.add(CAN_SCROLL);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ArssBlockBehaviors.useItemOn(stack, state, level, pos, player, hand, hit, ArssBlockBehaviors::useItemOnScrollableBlockPowerToLock, super::useItemOn);
    }
}
