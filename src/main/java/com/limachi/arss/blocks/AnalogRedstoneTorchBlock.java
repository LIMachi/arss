package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.lim_lib.registries.Registries;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import com.limachi.lim_lib.registries.annotations.HasRedstoneTint;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"unused", "deprecation"})
@ParametersAreNonnullByDefault
public class AnalogRedstoneTorchBlock extends RedstoneTorchBlock implements IScrollBlockPowerOutput {

    @HasRedstoneTint
    @RegisterBlock(name = "analog_redstone_torch")
    public static RegistryObject<Block> R_BLOCK;

    public static RegistryObject<Item> R_ITEM;

    @StaticInit(Stage.BLOCK)
    public static void generateWallVariantAndSetTint() {
        AnalogRedstoneWallTorchBlock.R_BLOCK = Registries.block(Arss.MOD_ID, "analog_redstone_wall_torch", AnalogRedstoneWallTorchBlock::new);
    }

    @StaticInit(Stage.ITEM)
    public static void generateItem() {
        R_ITEM = Registries.item(Arss.MOD_ID, "analog_redstone_torch", ()->new StandingAndWallBlockItem(R_BLOCK.get(), AnalogRedstoneWallTorchBlock.R_BLOCK.get(), new Item.Properties(), Direction.DOWN), "jei.info.analog_redstone_torch");
    }


    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public AnalogRedstoneTorchBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH));
        registerDefaultState(stateDefinition.any().setValue(LIT, true).setValue(POWER, 15).setValue(ArssBlockStateProperties.CAN_SCROLL, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
        builder.add(ArssBlockStateProperties.CAN_SCROLL);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        boolean flag = hasNeighborSignal(level, pos, state);

        if (state.getValue(LIT)) {
            if (flag)
                level.setBlock(pos, state.setValue(LIT, false), 3);
        } else if (!flag) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
        }
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(LIT) && Direction.UP != dir ? state.getValue(POWER) : 0;
    }

    @Override
    public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Item held = player.getItemInHand(hand).getItem();
        if (held == Items.REDSTONE_TORCH || held == AnalogRedstoneTorchBlock.R_ITEM.get()) {
            boolean can_scroll = !state.getValue(ArssBlockStateProperties.CAN_SCROLL);
            level.setBlock(pos, state.setValue(ArssBlockStateProperties.CAN_SCROLL, can_scroll), 3);
            player.displayClientMessage(Component.translatable("display.arss.scrollable_block.can_scroll." + can_scroll), true);
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    public static class AnalogRedstoneWallTorchBlock extends RedstoneWallTorchBlock implements IScrollBlockPowerOutput {

        @HasRedstoneTint
        public static RegistryObject<Block> R_BLOCK;

        public static final IntegerProperty POWER = BlockStateProperties.POWER;

        public AnalogRedstoneWallTorchBlock() {
            super(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH).dropsLike(AnalogRedstoneTorchBlock.R_BLOCK.get()));
            registerDefaultState(stateDefinition.any().setValue(LIT, true).setValue(POWER, 15).setValue(ArssBlockStateProperties.CAN_SCROLL, true));
        }

        @Override
        public @Nonnull String getDescriptionId() { return "block.arss.analog_redstone_torch"; }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(ctx);
            return blockstate == null ? null : defaultBlockState().setValue(FACING, blockstate.getValue(FACING)).setValue(POWER, 15);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(POWER);
            builder.add(ArssBlockStateProperties.CAN_SCROLL);
        }

        @Override
        public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
            boolean flag = hasNeighborSignal(level, pos, state);

            if (state.getValue(LIT)) {
                if (flag)
                    level.setBlock(pos, state.setValue(LIT, false), 3);
            } else if (!flag) {
                level.setBlock(pos, state.setValue(LIT, true), 3);
            }
        }

        @Override
        public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
            return state.getValue(LIT) && state.getValue(FACING) != dir ? state.getValue(POWER) : 0;
        }

        @Override
        public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            Item held = player.getItemInHand(hand).getItem();
            if (held == Items.REDSTONE_TORCH || held == AnalogRedstoneTorchBlock.R_ITEM.get()) {
                boolean can_scroll = !state.getValue(ArssBlockStateProperties.CAN_SCROLL);
                level.setBlock(pos, state.setValue(ArssBlockStateProperties.CAN_SCROLL, can_scroll), 3);
                player.displayClientMessage(Component.translatable("display.arss.scrollable_block.can_scroll." + can_scroll), true);
                return InteractionResult.SUCCESS;
            }
            return super.use(state, level, pos, player, hand, hit);
        }
    }
}
