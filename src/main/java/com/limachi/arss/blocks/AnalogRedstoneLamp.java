package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.Static;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
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
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.NonnullDefault;

import java.util.Random;

import static com.limachi.arss.Registries.BLOCK_REGISTER;
import static com.limachi.arss.Registries.ITEM_REGISTER;

@SuppressWarnings("unused")
@Static
@NonnullDefault
public class AnalogRedstoneLamp extends RedstoneLampBlock {

    public static final Properties PROPS = BlockBehaviour.Properties.of(Material.BUILDABLE_GLASS).lightLevel(AnalogRedstoneLamp::litBlockEmission).strength(0.3F).sound(SoundType.GLASS);
    public static final RegistryObject<Block> R_BLOCK = BLOCK_REGISTER.register("analog_redstone_lamp", AnalogRedstoneLamp::new);
    public static final RegistryObject<Item> R_ITEM = ITEM_REGISTER.register("analog_redstone_lamp", ()->new BlockItem(R_BLOCK.get(), new Item.Properties().tab(Arss.ITEM_GROUP)));

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    static int litBlockEmission(BlockState state) {
        return state.getValue(BlockStateProperties.LIT) ? state.getValue(POWER) : 0;
    }

    public AnalogRedstoneLamp() { super(PROPS); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
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
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rng) {
        if (state.getValue(LIT) && !level.hasNeighborSignal(pos))
            level.setBlock(pos, state.setValue(LIT, false).setValue(POWER, 0), 2);
    }
}
