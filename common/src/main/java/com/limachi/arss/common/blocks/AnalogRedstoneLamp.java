package com.limachi.arss.common.blocks;

import com.limachi.arss.client.ClientDef;
import com.limachi.arss.common.ArssBlockBehaviors;
import com.limachi.arss.utils.annotations.RegisterBlock;
import com.limachi.arss.utils.annotations.RegisterBlockItem;
import com.limachi.arss.utils.client.annotations.FabricLayer;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

import static com.limachi.arss.common.ArssBlockStateProperties.HIDE_DOT;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER;

@SuppressWarnings({"unused", "deprecation"})
public class AnalogRedstoneLamp extends RedstoneLampBlock {

    @FabricLayer("cutout")
    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

    @RegisterBlockItem
    public static RegistrySupplier<BlockItem> R_ITEM;

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, components, flags);
        ClientDef.commonHoverText("analog_redstone_lamp", components);
    }

    static int litBlockEmission(BlockState state) {
        return state.getValue(BlockStateProperties.LIT) ? state.getValue(POWER) : 0;
    }

    public AnalogRedstoneLamp() {
        super(Properties.ofFullCopy(Blocks.REDSTONE_LAMP).lightLevel(AnalogRedstoneLamp::litBlockEmission));
        registerDefaultState(stateDefinition.any().setValue(HIDE_DOT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER, HIDE_DOT);
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

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ArssBlockBehaviors.useItemOn(stack, state, level, pos, player, hand, hit, ArssBlockBehaviors::useItemOnRedstoneDotBlock, super::useItemOn);
    }
}
