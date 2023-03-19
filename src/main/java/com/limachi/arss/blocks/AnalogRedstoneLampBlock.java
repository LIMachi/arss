package com.limachi.arss.blocks;

import com.limachi.lim_lib.registries.StaticInit;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
//import net.minecraft.util.RandomSource; //VERSION 1.19.2
import java.util.Random; //VERSION 1.18.2
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@StaticInit
@ParametersAreNonnullByDefault
public class AnalogRedstoneLampBlock extends RedstoneLampBlock {

    public static final Properties PROPS = BlockBehaviour.Properties.of(Material.BUILDABLE_GLASS).lightLevel(AnalogRedstoneLampBlock::litBlockEmission).strength(0.3F).sound(SoundType.GLASS);

    @RegisterBlock(name = "analog_redstone_lamp")
    public static RegistryObject<Block> R_BLOCK;

    @RegisterBlockItem(name = "analog_redstone_lamp", block = "analog_redstone_lamp", jeiInfoKey = "jei.info.analog_redstone_lamp")
    public static RegistryObject<Item> R_ITEM;

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    static int litBlockEmission(BlockState state) {
        return state.getValue(BlockStateProperties.LIT) ? state.getValue(POWER) : 0;
    }

    public AnalogRedstoneLampBlock() { super(PROPS); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        int s = ctx.getLevel().getBestNeighborSignal(ctx.getClickedPos());
        return defaultBlockState().setValue(LIT, s > 0).setValue(POWER, s);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos pos2, boolean bool) {
        if (!level.isClientSide) {
            boolean flag = state.getValue(LIT);
            int p = state.getValue(POWER);
            int s = level.getBestNeighborSignal(pos);
            if (p != s) {
                if (flag && s == 0)
                    level.scheduleTick(pos, this, 4);
                else
                    level.setBlock(pos, state.setValue(LIT, true).setValue(POWER, s), 2);
            }

        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos,
//                     RandomSource //VERSION 1.19.2
                             Random //VERSION 1.18.2
                             rng) {
        if (state.getValue(LIT) && !level.hasNeighborSignal(pos))
            level.setBlock(pos, state.setValue(LIT, false).setValue(POWER, 0), 2);
    }
}
