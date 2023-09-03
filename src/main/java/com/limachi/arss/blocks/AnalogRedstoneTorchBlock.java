package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.arss.items.ICustomItemRenderers;
import com.limachi.arss.client.CustomItemStackRenderer;
import com.limachi.lim_lib.registries.Registries;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import com.limachi.lim_lib.registries.annotations.HasRedstoneTint;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "deprecation"})
@ParametersAreNonnullByDefault
public class AnalogRedstoneTorchBlock extends RedstoneTorchBlock implements IScrollBlockPowerOutput {

    @HasRedstoneTint
    @RegisterBlock(name = "analog_redstone_torch")
    public static RegistryObject<Block> R_BLOCK;

    @StaticInit(Stage.BLOCK)
    public static void generateWallVariantAndSetTint() {
        AnalogRedstoneWallTorchBlock.R_BLOCK = Registries.block(Arss.MOD_ID, "analog_redstone_wall_torch", AnalogRedstoneWallTorchBlock::new);
    }

    public static class AnalogRedstoneTorchItem extends StandingAndWallBlockItem implements ICustomItemRenderers {
        @RegisterItem(name = "analog_redstone_torch")
        public static RegistryObject<Item> R_ITEM;

        public AnalogRedstoneTorchItem() {
            super(R_BLOCK.get(), AnalogRedstoneWallTorchBlock.R_BLOCK.get(), new Item.Properties(), Direction.DOWN);
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return CustomItemStackRenderer.getInstance();
                }
            });
        }

        @Override
        public ItemStack itemRenderer() {
            return new ItemStack(Items.REDSTONE_TORCH);
        }

        @Override
        public BlockState blockRenderer() {
            return null;
        }

        @Override
        public BlockState self() {
            return R_BLOCK.get().defaultBlockState();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, level, components, flags);
        Arss.commonHoverText("analog_redstone_torch", components);
    }

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public AnalogRedstoneTorchBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH));
        registerDefaultState(stateDefinition.any().setValue(LIT, true).setValue(POWER, 15).setValue(ArssBlockStateProperties.CAN_SCROLL, true).setValue(ArssBlockStateProperties.BOOSTED, false));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55703_, boolean p_55704_) {
        if (state.getValue(LIT) == hasNeighborSignal(level, pos, state) && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, state.getValue(ArssBlockStateProperties.BOOSTED) ? 1 : 2);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER, ArssBlockStateProperties.CAN_SCROLL, ArssBlockStateProperties.BOOSTED);
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
        return use(state, level, pos, player, hand, ()->super.use(state, level, pos, player, hand, hit));
    }

    public static class AnalogRedstoneWallTorchBlock extends RedstoneWallTorchBlock implements IScrollBlockPowerOutput {

        @HasRedstoneTint
        public static RegistryObject<Block> R_BLOCK;

        public static final IntegerProperty POWER = BlockStateProperties.POWER;

        public AnalogRedstoneWallTorchBlock() {
            super(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH).dropsLike(AnalogRedstoneTorchBlock.R_BLOCK.get()));
            registerDefaultState(stateDefinition.any().setValue(LIT, true).setValue(POWER, 15).setValue(ArssBlockStateProperties.CAN_SCROLL, true).setValue(ArssBlockStateProperties.BOOSTED, false));
        }

        @Override
        public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55703_, boolean p_55704_) {
            if (state.getValue(LIT) == hasNeighborSignal(level, pos, state) && !level.getBlockTicks().willTickThisTick(pos, this)) {
                level.scheduleTick(pos, this, state.getValue(ArssBlockStateProperties.BOOSTED) ? 1 : 2);
            }
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
            builder.add(POWER, ArssBlockStateProperties.CAN_SCROLL, ArssBlockStateProperties.BOOSTED);
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
            return use(state, level, pos, player, hand, ()->super.use(state, level, pos, player, hand, hit));
        }
    }
}
