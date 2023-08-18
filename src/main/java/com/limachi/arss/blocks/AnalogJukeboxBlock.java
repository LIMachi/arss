package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.items.BlockItemWithCustomRenderer;
import com.limachi.arss.blockEntities.AnalogJukeboxBlockEntity;
import com.limachi.arss.menu.AnalogJukeboxMenu;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@SuppressWarnings({"unused", "deprecation"})
@ParametersAreNonnullByDefault
public class AnalogJukeboxBlock extends BaseEntityBlock {

    @RegisterBlock(name = "analog_jukebox")
    public static RegistryObject<Block> R_BLOCK;

    public static class AnalogJukebox extends BlockItemWithCustomRenderer {

        @RegisterItem
        public static RegistryObject<BlockItem> R_ITEM;

        public AnalogJukebox() { super(R_BLOCK.get(), new Item.Properties(), Blocks.JUKEBOX); }
    }

    public AnalogJukeboxBlock() { super(Properties.copy(Blocks.JUKEBOX)); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, level, components, flags);
        Arss.commonHoverText("analog_jukebox", components);
    }

    /**
     * since record item have their own logic for rigth click on jukebox, might have to code insertion here
     */
    @Override
    public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack recordStack = player.getItemInHand(hand);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AnalogJukeboxBlockEntity) {
            if (recordStack.getItem() instanceof RecordItem) {
                if (((AnalogJukeboxBlockEntity)be).insertRecord(recordStack)) {
                    ItemStack out = recordStack.copy();
                    out.shrink(1);
                    player.setItemInHand(hand, out);
                }
            } else
                AnalogJukeboxMenu.open(player, (AnalogJukeboxBlockEntity)be);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55045_, boolean p_55046_) {
        if (level instanceof ServerLevel) {
            int power = level.getBestNeighborSignal(pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AnalogJukeboxBlockEntity && power != ((AnalogJukeboxBlockEntity) be).playing())
                ((AnalogJukeboxBlockEntity) be).play(power);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state1, boolean bool) {
        if (!state.is(state1.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AnalogJukeboxBlockEntity)
                ((AnalogJukeboxBlockEntity)be).dropAllRecords();
            super.onRemove(state, level, pos, state1, bool);
        }
    }

    @Override
    public @Nonnull RenderShape getRenderShape(BlockState p_54296_) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnalogJukeboxBlockEntity(pos, state);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AnalogJukeboxBlockEntity)
            return ((AnalogJukeboxBlockEntity)be).getAnalogOutputSignal();
        return 0;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level unusedLevel, BlockState unusedState, BlockEntityType<T> type) {
        return (level, pos, state, be) -> {
            if (be instanceof AnalogJukeboxBlockEntity o && level instanceof ServerLevel)
                o.tick();
        };
    }
}
