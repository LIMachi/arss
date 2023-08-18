package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.items.BlockItemWithCustomRenderer;
import com.limachi.lim_lib.registries.StaticInit;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@SuppressWarnings("unused")
@StaticInit
@ParametersAreNonnullByDefault
public class AnalogRedstoneLampBlock extends RedstoneLampBlock {

    @RegisterBlock(name = "analog_redstone_lamp")
    public static RegistryObject<Block> R_BLOCK;

    public static class AnalogRedstoneLamp extends BlockItemWithCustomRenderer {

        @RegisterItem
        public static RegistryObject<BlockItem> R_ITEM;

        public AnalogRedstoneLamp() { super(R_BLOCK.get(), new Item.Properties(), Blocks.REDSTONE_LAMP); }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, level, components, flags);
        Arss.commonHoverText("analog_redstone_lamp", components);
    }

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    static int litBlockEmission(BlockState state) {
        return state.getValue(BlockStateProperties.LIT) ? state.getValue(POWER) : 0;
    }

    public AnalogRedstoneLampBlock() { super(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP).lightLevel(AnalogRedstoneLampBlock::litBlockEmission)); }

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
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        if (state.getValue(LIT) && !level.hasNeighborSignal(pos))
            level.setBlock(pos, state.setValue(LIT, false).setValue(POWER, 0), 2);
    }
}
