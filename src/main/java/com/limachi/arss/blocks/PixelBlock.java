package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInit;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@SuppressWarnings("unused")
@StaticInit
@ParametersAreNonnullByDefault
public class PixelBlock extends RedstoneLampBlock {

    @RegisterBlock
    public static RegistryObject<Block> R_BLOCK;

    @RegisterBlockItem
    public static RegistryObject<Item> R_ITEM;

    public static int getTint(BlockState state) {
        return switch (state.getValue(POWER)) {
            case 0 -> 0;
            case 1 -> 0xAAAAAA;
            case 2 -> 0x555555;
            case 3 -> 0x553322;
            case 4 -> 0xFF0000;
            case 5 -> 0xFF9922;
            case 6 -> 0xFFFF00;
            case 7 -> 0x00FF00;
            case 8 -> 0x226622;
            case 9 -> 0x1177AA;
            case 10 -> 0x3399FF;
            case 11 -> 0x0022FF;
            case 12 -> 0x5511BB;
            case 13 -> 0xAA44CC;
            case 14 -> 0xEEAABB;
            case 15 -> 0xFFFFFF;
            default -> -1;
        };
    }

    @OnlyIn(Dist.CLIENT)
    public static int getTint(BlockState state, BlockAndTintGetter getter, BlockPos pos, int index) {
        return getTint(state);
    }

    @OnlyIn(Dist.CLIENT)
    @StaticInitClient
    public static void registerTint() { ClientRegistries.setColor(R_BLOCK, PixelBlock::getTint); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, level, components, flags);
        Arss.commonHoverText("pixel_block", components);
    }

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public PixelBlock() { super(Properties.copy(Blocks.REDSTONE_LAMP).isRedstoneConductor((s, l, p)->false).lightLevel(s->10)); }

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
                if (s == 0)
                    level.setBlock(pos, state.setValue(LIT, false).setValue(POWER, 0), 2);
                else
                    level.setBlock(pos, state.setValue(LIT, true).setValue(POWER, s), 2);
            }

        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {}
}
