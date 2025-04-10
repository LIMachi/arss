package com.limachi.arss.common.blocks;

import com.limachi.arss.client.keyboardSystem.KeyboardHandler;
import com.limachi.arss.common.ArssItemStackComponents;
import com.limachi.arss.common.items.Keyboard;

import com.limachi.lim_lib.client.annotations.BlockTinter;

import com.limachi.lim_lib.common.annotations.RegisterBlock;
import com.limachi.lim_lib.common.annotations.RegisterMsg;
import com.limachi.lim_lib.common.network.IC2SMsg;
import com.limachi.lim_lib.common.utils.Game;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.phys.BlockHitResult;

@SuppressWarnings("unused")
public class KeyboardLectern extends LecternBlock implements EntityBlock {

    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

    @RegisterMsg
    public record KeyPressVisualFeedbackLecternMsg(BlockPos lectern, int mask) implements IC2SMsg<KeyPressVisualFeedbackLecternMsg> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            if (ctx.getPlayer() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(lectern) instanceof com.limachi.arss.common.block_entities.KeyboardLectern be) {
                    be.getKeyboard().set(ArssItemStackComponents.OUTPUT.get(), mask);
                }
            }
        }
    }

    @BlockTinter
    public static int getTint(BlockState state, BlockAndTintGetter getter, BlockPos pos, int index) {
        if (getter.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.KeyboardLectern be)
            return Keyboard.getTint(be.getKeyboard(), index);
        return -1;
    }

    public static void replaceLectern(Level level, BlockPos pos, BlockState lectern, ItemStack stack) {
        level.setBlockAndUpdate(pos, R_BLOCK.get().defaultBlockState().setValue(FACING, lectern.getValue(FACING)).setValue(POWERED, false));
        if (level.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.KeyboardLectern be)
            be.setKeyboard(stack);
    }

    public static void restoreLectern(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.KeyboardLectern be) {
            ItemStack keyboard = be.getKeyboard();
            if (!player.getInventory().add(keyboard))
                player.drop(keyboard, true);
            be.setKeyboard(ItemStack.EMPTY);
        }
        level.setBlockAndUpdate(pos, Blocks.LECTERN.defaultBlockState().setValue(FACING, state.getValue(FACING)).setValue(POWERED, false));
    }

    public KeyboardLectern() { super(Properties.ofFullCopy(Blocks.LECTERN).dropsLike(Blocks.LECTERN)); }

//    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
//        super.createBlockStateDefinition(builder);
//        builder.add(BlockStateProperties.POWER);
//    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new com.limachi.arss.common.block_entities.KeyboardLectern(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.KeyboardLectern be)
                Game.runLogical(()->()->KeyboardHandler.useLectern(be, Keyboard.isListening(be.getKeyboard())), null);
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        if (player.isShiftKeyDown())
            restoreLectern(level, pos, state, player);
        else if (level.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.KeyboardLectern be)
            be.setController(player);
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state1, boolean bool) {
        if (!state.is(state1.getBlock())) {
            if (level.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.KeyboardLectern be)
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

//    @Override
//    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
//        return state.getValue(BlockStateProperties.POWER);
//    }

    @Override
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return Blocks.LECTERN.getCloneItemStack(levelReader, blockPos, blockState);
    }
}
