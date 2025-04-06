package com.limachi.arss.common.blocks.diodes;

import com.limachi.arss.client.screen.NewKeyboardScreen;
import com.limachi.arss.client.screen.ResonantGateScreen;
import com.limachi.arss.common.ArssBlockStateProperties;
import com.limachi.arss.common.block_entities.ResonantGateBlockEntity;
import com.limachi.arss.utils.Game;
import com.limachi.arss.utils.annotations.RegisterBlock;
import com.limachi.arss.utils.annotations.RegisterBlockItem;
import com.limachi.arss.utils.client.annotations.FabricLayer;

import com.mojang.serialization.MapCodec;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

public class ResonantGateBlock extends DiodeBlock implements EntityBlock {
    @FabricLayer("cutout")
    @RegisterBlock("resonant_gate")
    public static RegistrySupplier<Block> R_BLOCK;

    @RegisterBlockItem(value = "resonant_gate", block = "resonant_gate")
    public static RegistrySupplier<BlockItem> R_ITEM;

    public static final MapCodec<ResonantGateBlock> CODEC = simpleCodec(ResonantGateBlock::new);

    public ResonantGateBlock(Properties unused) {
        super(Properties.ofFullCopy(Blocks.COMPARATOR));
    }

    public ResonantGateBlock() {
        this(null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, BlockStateProperties.POWER, ArssBlockStateProperties.SIDES);
    }

    @Override
    protected MapCodec<ResonantGateBlock> codec() {
        return CODEC;
    }

    @Override
    protected int getDelay(BlockState blockState) {
        return 0;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ResonantGateBlockEntity(blockPos, blockState);
    }

    public static int getStaticInputSignal(Level level, BlockPos pos, BlockState state) {
        return Math.clamp(Integer.max(BaseAnalogDiodeBlock.sGetInputSignal(level, pos, state, true), BaseAnalogDiodeBlock.sGetAlternateSignal(level, pos, state)), 0, 15);
    }

    @Override
    protected void checkTickOnNeighbor(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide)
            return;
        if (level.getBlockEntity(blockPos) instanceof ResonantGateBlockEntity be)
            be.updatePowerInput(getStaticInputSignal(level, blockPos, blockState));
    }

    @Override
    protected int getOutputSignal(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return blockState.getValue(BlockStateProperties.POWER);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        Game.runLogical(()->()-> ResonantGateScreen.client_open(blockPos), null);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
