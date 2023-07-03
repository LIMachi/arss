package com.limachi.arss.blocks;

import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.lim_lib.registries.annotations.HasRedstoneTint;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredBlock;
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
public class AnalogRedstoneBlock extends PoweredBlock implements IScrollBlockPowerOutput {

    @HasRedstoneTint
    @RegisterBlock(name = "analog_redstone_block")
    public static RegistryObject<Block> R_BLOCK;

    @RegisterBlockItem(name = "analog_redstone_block", block = "analog_redstone_block", jeiInfoKey = "jei.info.analog_redstone_block")
    public static RegistryObject<Item> R_ITEM;

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public AnalogRedstoneBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK));
        registerDefaultState(stateDefinition.any().setValue(POWER, 15).setValue(ArssBlockStateProperties.CAN_SCROLL, true));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
        builder.add(ArssBlockStateProperties.CAN_SCROLL);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return state.getValue(POWER);
    }

    @Override
    public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return use(state, level, pos, player, hand, ()->super.use(state, level, pos, player, hand, hit));
    }
}
