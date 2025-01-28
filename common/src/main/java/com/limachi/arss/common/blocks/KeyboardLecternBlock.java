package com.limachi.arss.common.blocks;

import com.limachi.arss.common.block_entities.KeyboardLecternBlockEntity;
import com.limachi.arss.common.items.KeyboardItem;
import com.limachi.arss.utils.annotations.RegisterBlock;
import com.limachi.arss.utils.client.annotations.BlockTinter;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

@SuppressWarnings("unused")
public class KeyboardLecternBlock extends LecternBlock implements EntityBlock {

    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

//    @RegisterMsg
//    public record KeyPressVisualFeedbackLecternMsg(BlockPos lectern, int power) implements IRecordMsg {
//        @Override
//        public void serverWork(Player player) {
//            BlockState state = player.level().getBlockState(lectern);
//            if (state.getBlock() instanceof KeyboardLecternBlock && state.getValue(BlockStateProperties.POWER) != power)
//                player.level().setBlockAndUpdate(lectern, state.setValue(BlockStateProperties.POWER, power));
//        }
//    }

    @BlockTinter
    public static int getTint(BlockState state, BlockAndTintGetter getter, BlockPos pos, int index) {
        return KeyboardItem.getTint(index, state.getValue(BlockStateProperties.POWER), state.getValue(POWERED));
    }

    public static void replaceLectern(Level level, BlockPos pos, BlockState lectern, ItemStack stack) {
        level.setBlockAndUpdate(pos, R_BLOCK.get().defaultBlockState().setValue(FACING, lectern.getValue(FACING)).setValue(POWERED, false));
        if (level.getBlockEntity(pos) instanceof KeyboardLecternBlockEntity be)
            be.setKeyboard(stack);
    }

    public static void restoreLectern(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof KeyboardLecternBlockEntity be) {
            ItemStack keyboard = be.getKeyboard();
            if (!player.getInventory().add(keyboard))
                player.drop(keyboard, true);
            be.setKeyboard(ItemStack.EMPTY);
        }
        level.setBlockAndUpdate(pos, Blocks.LECTERN.defaultBlockState().setValue(FACING, state.getValue(FACING)).setValue(POWERED, false));
    }

    public KeyboardLecternBlock() { super(Properties.ofFullCopy(Blocks.LECTERN).dropsLike(Blocks.LECTERN)); }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.POWER);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KeyboardLecternBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS_NO_ITEM_USED;
        if (player.isShiftKeyDown())
            restoreLectern(level, pos, state, player);
        else if (level.getBlockEntity(pos) instanceof KeyboardLecternBlockEntity be)
            be.setController(player);
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state1, boolean bool) {
        if (!state.is(state1.getBlock())) {
            if (level.getBlockEntity(pos) instanceof KeyboardLecternBlockEntity be)
                Containers.dropContents(level, pos, NonNullList.of(ItemStack.EMPTY, be.getKeyboard()));
            super.onRemove(state, level, pos, state1, bool);
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) { return false; }

    @Override
    public int getSignal(BlockState p_54515_, BlockGetter p_54516_, BlockPos p_54517_, Direction p_54518_) { return 0; }

    @Override
    public int getDirectSignal(BlockState p_54566_, BlockGetter p_54567_, BlockPos p_54568_, Direction p_54569_) { return 0; }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(BlockStateProperties.POWER);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return Blocks.LECTERN.getCloneItemStack(levelReader, blockPos, blockState);
    }
}
