package com.limachi.arss.common.blocks;


import com.limachi.arss.client.ClientDef;
import com.limachi.arss.common.menus.InstrumentSwapperMenu;

import com.limachi.lim_lib.client.annotations.FabricLayer;

import com.limachi.lim_lib.common.annotations.RegisterBlock;
import com.limachi.lim_lib.common.annotations.RegisterBlockItem;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class InstrumentSwapper extends Block implements EntityBlock {

    @FabricLayer("cutout")
    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

    @RegisterBlockItem
    public static RegistrySupplier<BlockItem> R_ITEM;

    public InstrumentSwapper() {
        super(Properties.ofFullCopy(Blocks.NOTE_BLOCK).isRedstoneConductor((s, l, p)->false));
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT, NoteBlockInstrument.HARP).setValue(BlockStateProperties.POWER, 0));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, components, flags);
        ClientDef.commonHoverText("instrument_swapper_block", components);
    }

//    @Override
//    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
//        return direction != Direction.UP && direction != Direction.DOWN;
//    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.NOTEBLOCK_INSTRUMENT, BlockStateProperties.POWER);
    }

    /**
     * since record item have their own logic for rigth click on jukebox, might have to code insertion here
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.getBlockEntity(blockPos) instanceof com.limachi.arss.common.block_entities.InstrumentSwapper be) {
            InstrumentSwapperMenu.open(player, be);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
    }

    public static int getBestNeighborWithBlackListedSides(Level level, BlockPos pos, Direction ... blacklist) {
        int i = 0;

        List<Direction> skip = Arrays.asList(blacklist);

        for (Direction direction : Direction.values()) {
            if (skip.contains(direction)) continue;
            int j = level.getSignal(pos.relative(direction), direction);
            if (j >= 15) {
                return 15;
            }

            if (j > i) {
                i = j;
            }
        }

        return i;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55045_, boolean p_55046_) {
        if (level instanceof ServerLevel) {
            int power = getBestNeighborWithBlackListedSides(level, pos, Direction.UP);
            if (power != state.getValue(BlockStateProperties.POWER) && level.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.InstrumentSwapper be)
                be.updateInstrument(state.setValue(BlockStateProperties.POWER, power));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state1, boolean bool) {
        if (!state.is(state1.getBlock())) {
            if (level.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.InstrumentSwapper be)
                be.dropAllInstruments();
            super.onRemove(state, level, pos, state1, bool);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState p_54296_) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new com.limachi.arss.common.block_entities.InstrumentSwapper(pos, state); }
}
