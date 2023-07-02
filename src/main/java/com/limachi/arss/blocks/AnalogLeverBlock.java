package com.limachi.arss.blocks;

import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.lim_lib.registries.annotations.HasRedstoneTint;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class AnalogLeverBlock extends LeverBlock implements IScrollBlockPowerOutput {

    @HasRedstoneTint
    @RegisterBlock(name = "analog_lever")
    public static RegistryObject<Block> R_BLOCK;

    @RegisterBlockItem(name = "analog_lever", block = "analog_lever", jeiInfoKey = "jei.info.analog_lever")
    public static RegistryObject<Item> R_ITEM;

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public AnalogLeverBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.LEVER));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(FACE, AttachFace.WALL).setValue(POWER, 15).setValue(ArssBlockStateProperties.CAN_SCROLL, true));
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
        builder.add(ArssBlockStateProperties.CAN_SCROLL);
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
