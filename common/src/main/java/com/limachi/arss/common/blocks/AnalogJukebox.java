package com.limachi.arss.common.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.client.ClientDef;
import com.limachi.arss.common.menus.AnalogJukeboxMenu;
import com.limachi.arss.utils.annotations.RegisterBlock;
import com.limachi.arss.utils.annotations.RegisterBlockItem;
import com.limachi.arss.utils.client.annotations.FabricLayer;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

import static com.limachi.arss.common.ArssBlockStateProperties.HIDE_DOT;

@SuppressWarnings({"unused", "deprecation"})
public class AnalogJukebox extends Block implements EntityBlock {

    @FabricLayer("cutout")
    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

    @RegisterBlockItem
    public static RegistrySupplier<Item> R_ITEM;

    public AnalogJukebox() {
        super(Properties.ofFullCopy(Blocks.JUKEBOX));
        registerDefaultState(stateDefinition.any().setValue(HIDE_DOT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HIDE_DOT);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, components, flags);
        ClientDef.commonHoverText("analog_jukebox", components);
    }

    /**
     * since record item have their own logic for rigth click on jukebox, might have to code insertion here
     */

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack recordStack = player.getItemInHand(hand);
        if (Arss.isWrench(recordStack)) {
            level.setBlock(pos, state.setValue(HIDE_DOT, !state.getValue(HIDE_DOT)), 3);
            return ItemInteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof com.limachi.arss.common.block_entities.AnalogJukebox) {
            if (recordStack.has(DataComponents.JUKEBOX_PLAYABLE)) {
                if (((com.limachi.arss.common.block_entities.AnalogJukebox)be).insertRecord(recordStack)) {
                    ItemStack out = recordStack.copy();
                    out.shrink(1);
                    player.setItemInHand(hand, out);
                }
            }
            else
                AnalogJukeboxMenu.open(player, (com.limachi.arss.common.block_entities.AnalogJukebox)be);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55045_, boolean p_55046_) {
        if (level instanceof ServerLevel) {
            int power = level.getBestNeighborSignal(pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof com.limachi.arss.common.block_entities.AnalogJukebox && power != ((com.limachi.arss.common.block_entities.AnalogJukebox) be).playing())
                ((com.limachi.arss.common.block_entities.AnalogJukebox) be).play(power);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state1, boolean bool) {
        if (!state.is(state1.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof com.limachi.arss.common.block_entities.AnalogJukebox)
                ((com.limachi.arss.common.block_entities.AnalogJukebox)be).dropAllRecords();
            super.onRemove(state, level, pos, state1, bool);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState p_54296_) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new com.limachi.arss.common.block_entities.AnalogJukebox(pos, state);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof com.limachi.arss.common.block_entities.AnalogJukebox)
            return ((com.limachi.arss.common.block_entities.AnalogJukebox)be).getAnalogOutputSignal();
        return 0;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level unusedLevel, BlockState unusedState, BlockEntityType<T> type) {
        return (level, pos, state, be) -> {
            if (be instanceof com.limachi.arss.common.block_entities.AnalogJukebox o && level instanceof ServerLevel)
                o.tick();
        };
    }
}
