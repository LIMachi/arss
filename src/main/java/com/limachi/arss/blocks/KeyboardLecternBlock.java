package com.limachi.arss.blocks;

import com.limachi.arss.blockEntities.KeyboardLecternBlockEntity;
import com.limachi.arss.items.KeyboardItem;
import com.limachi.lim_lib.PlayerUtils;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class KeyboardLecternBlock extends LecternBlock implements EntityBlock {

    @RegisterBlock
    public static RegistryObject<Block> R_BLOCK;

    @RegisterMsg
    public record KeyPressVisualFeedbackLecternMsg(BlockPos lectern, int power) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            BlockState state = player.level().getBlockState(lectern);
            if (state.getBlock() instanceof KeyboardLecternBlock && state.getValue(BlockStateProperties.POWER) != power)
                player.level().setBlockAndUpdate(lectern, state.setValue(BlockStateProperties.POWER, power));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static int getTint(BlockState state, BlockAndTintGetter getter, BlockPos pos, int index) {
        return KeyboardItem.getTint(index, state.getValue(BlockStateProperties.POWER), state.getValue(POWERED));
    }

    @StaticInitClient
    public static void registerTint() {
        ClientRegistries.setColor(R_BLOCK, KeyboardLecternBlock::getTint);
    }

    public static void replaceLectern(Level level, BlockPos pos, BlockState lectern, ItemStack stack) {
        level.setBlockAndUpdate(pos, R_BLOCK.get().defaultBlockState().setValue(FACING, lectern.getValue(FACING)).setValue(POWERED, false));
        if (level.getBlockEntity(pos) instanceof KeyboardLecternBlockEntity be)
            be.setKeyboard(stack);
    }

    public static void restoreLectern(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof KeyboardLecternBlockEntity be) {
            PlayerUtils.giveOrDrop(player, be.getKeyboard());
            be.setKeyboard(ItemStack.EMPTY);
        }
        level.setBlockAndUpdate(pos, Blocks.LECTERN.defaultBlockState().setValue(FACING, state.getValue(FACING)).setValue(POWERED, false));
    }

    public KeyboardLecternBlock() { super(Properties.copy(Blocks.LECTERN).lootFrom(()->Blocks.LECTERN)); }

    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.POWER);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new KeyboardLecternBlockEntity(pos, state);
    }

    @Override
    @Nonnull
    public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown())
            restoreLectern(level, pos, state, player);
        else if (level.getBlockEntity(pos) instanceof KeyboardLecternBlockEntity be)
            be.setController(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState state1, boolean bool) {
        if (!state.is(state1.getBlock())) {
            if (level.getBlockEntity(pos) instanceof KeyboardLecternBlockEntity be)
                Containers.dropContents(level, pos, NonNullList.of(ItemStack.EMPTY, be.getKeyboard()));
            super.onRemove(state, level, pos, state1, bool);
        }
    }

    @Override
    public boolean isSignalSource(@Nonnull BlockState state) { return false; }

    @Override
    public int getAnalogOutputSignal(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos) { return 0; }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return Blocks.LECTERN.getCloneItemStack(state, target, level, pos, player);
    }
}
