package com.limachi.arss.common.blocks;

import com.limachi.arss.client.ClientDef;

import com.limachi.arss.common.ArssBlockStateProperties;
import com.limachi.arss.common.items.ICustomItemRenderers;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.Stage;
import com.limachi.arss.utils.client.annotations.FabricLayer;
import com.limachi.arss.utils.client.annotations.HasRedstoneTint;
import com.limachi.arss.utils.annotations.RegisterItem;
import com.limachi.arss.utils.annotations.StaticInit;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

@SuppressWarnings({"unused", "deprecation"})
@HasRedstoneTint("analog_redstone_torch")
public class AnalogRedstoneTorchBlock extends RedstoneTorchBlock implements IScrollBlockPowerOutput {

    @FabricLayer("cutout")
    public static RegistrySupplier<Block> R_BLOCK;

    @StaticInit(Stage.BLOCK)
    public static void generateWallVariantAndSetTint() {
        R_BLOCK = ModBase.registries.block("analog_redstone_torch", AnalogRedstoneTorchBlock::new);
        AnalogRedstoneWallTorchBlock.R_BLOCK = ModBase.registries.block("analog_redstone_wall_torch", AnalogRedstoneWallTorchBlock::new);
    }

    public static class AnalogRedstoneTorchItem extends StandingAndWallBlockItem implements ICustomItemRenderers {
        @RegisterItem("analog_redstone_torch")
        public static RegistrySupplier<Item> R_ITEM;

        public AnalogRedstoneTorchItem() {
            super(R_BLOCK.get(), AnalogRedstoneWallTorchBlock.R_BLOCK.get(), new Properties(), Direction.DOWN);
        }

//        @Override
//        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
//            consumer.accept(new IClientItemExtensions() {
//                @Override
//                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
//                    return CustomItemStackRenderer.getInstance();
//                }
//            });
//        }

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
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        ClientDef.commonHoverText("analog_redstone_torch", list);
    }

    public AnalogRedstoneTorchBlock() {
        super(Properties.ofFullCopy(Blocks.REDSTONE_TORCH));
        registerDefaultState(stateDefinition.any().setValue(LIT, true).setValue(POWER, 15).setValue(CAN_SCROLL, true).setValue(ArssBlockStateProperties.BOOSTED, false));
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
        builder.add(POWER, CAN_SCROLL, ArssBlockStateProperties.BOOSTED);
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return use(stack, state, level, pos, player, hand, ()->super.useItemOn(stack, state, level, pos, player, hand, hit));
    }

    @HasRedstoneTint("analog_redstone_wall_torch")
    public static class AnalogRedstoneWallTorchBlock extends RedstoneWallTorchBlock implements IScrollBlockPowerOutput {

        @FabricLayer("cutout")
        public static RegistrySupplier<Block> R_BLOCK;

        public AnalogRedstoneWallTorchBlock() {
            super(Properties.ofFullCopy(Blocks.REDSTONE_TORCH).dropsLike(AnalogRedstoneTorchBlock.R_BLOCK.get()));
            registerDefaultState(stateDefinition.any().setValue(LIT, true).setValue(POWER, 15).setValue(CAN_SCROLL, true).setValue(ArssBlockStateProperties.BOOSTED, false));
        }

        @Override
        public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55703_, boolean p_55704_) {
            if (state.getValue(LIT) == hasNeighborSignal(level, pos, state) && !level.getBlockTicks().willTickThisTick(pos, this)) {
                level.scheduleTick(pos, this, state.getValue(ArssBlockStateProperties.BOOSTED) ? 1 : 2);
            }
        }

        @Override
        public String getDescriptionId() { return "block.arss.analog_redstone_torch"; }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(ctx);
            return blockstate == null ? null : defaultBlockState().setValue(FACING, blockstate.getValue(FACING)).setValue(POWER, 15);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(POWER, CAN_SCROLL, ArssBlockStateProperties.BOOSTED);
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
        protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            return use(stack, state, level, pos, player, hand, ()->super.useItemOn(stack, state, level, pos, player, hand, hit));
        }
    }
}
