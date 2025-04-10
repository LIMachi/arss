package com.limachi.arss.common.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.common.items.RedstoneBooster;

import com.limachi.arss.client.ClientDef;

import com.limachi.lim_lib.client.annotations.FabricLayer;
import com.limachi.lim_lib.common.annotations.RegisterBlock;
import com.limachi.lim_lib.common.annotations.RegisterBlockItem;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

import static com.limachi.arss.common.ArssBlockStateProperties.*;

@SuppressWarnings("unused")
public class AnalogNoteBlock extends NoteBlock implements EntityBlock {

    @FabricLayer("cutout")
    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

    @RegisterBlockItem
    public static RegistrySupplier<BlockItem> R_ITEM;

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new com.limachi.arss.common.block_entities.AnalogNoteBlock(pos, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, components, flags);
        ClientDef.commonHoverText("analog_note_block", components);
    }

    public AnalogNoteBlock() {
        super(Properties.ofFullCopy(Blocks.NOTE_BLOCK));
        registerDefaultState(stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, 0).setValue(POWERED, false).setValue(HIGH, false).setValue(HIDE_DOT, false).setValue(BOOSTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INSTRUMENT, POWERED, NOTE, HIGH, HIDE_DOT, BOOSTED);
    }

    //FIXME: for some reason gets called multiple times in the same tick when a redstone line is depowering (~every 2 power loss)
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55045_, boolean p_55046_) {
        int power = level.getBestNeighborSignal(pos);
        int prevPower = level.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.AnalogNoteBlock be ? be.getPreviousInput() : 0;
        int _new = getNote(state, level, pos);
        if (!((_new == -1 && power == 0) || _new == state.getValue(NOTE))) {
//            _new = net.minecraftforge.common.ForgeHooks.onNoteChange(level, pos, state, state.getValue(NOTE), _new);
//            if (_new == -1) return;
        }
        boolean boosted = state.getValue(BOOSTED);
        if (power != prevPower) {
            if (level.getBlockEntity(pos) instanceof com.limachi.arss.common.block_entities.AnalogNoteBlock be)
                be.setPreviousInput(power);
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)).setValue(POWERED, power > 0), 3);
            if (power > 0 && !boosted)
                playNote(null, state, level, pos);
        } else if (_new != state.getValue(NOTE))
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)), 3);
        if (power > 0 && boosted)
            playNote(null, state, level, pos);
    }

    private void playNote(Entity entity, BlockState state, Level level, BlockPos pos) {
        if (state.getValue(INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(pos.above()).isAir()) {
            level.blockEvent(pos, this, 0, 0);
            level.gameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, pos);
        }
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (Arss.isWrench(player.getItemInHand(hand))) {
            level.setBlock(pos, state.setValue(HIDE_DOT, !state.getValue(HIDE_DOT)), 3);
            return ItemInteractionResult.SUCCESS;
        }
        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;
        else {
            int power = level.getBestNeighborSignal(pos);
            int _new = getNote(state, level, pos);
            if (!((_new == -1 && power == 0) || _new == state.getValue(NOTE))) {
//                _new = net.minecraftforge.common.ForgeHooks.onNoteChange(level, pos, state, state.getValue(NOTE), _new);
//                if (_new == -1) return ItemInteractionResult.FAIL;
            }
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)).setValue(HIGH, !state.getValue(HIGH)), 3);
            player.displayClientMessage(Component.translatable("display.arss.analog_note_block.high_pitch." + level.getBlockState(pos).getValue(HIGH)), true);
            playNote(player, state, level, pos);
            player.awardStat(Stats.TUNE_NOTEBLOCK);
            return ItemInteractionResult.CONSUME;
        }
    }

    protected int getNote(BlockState state, Level level, BlockPos pos) {
        int power = level.getBestNeighborSignal(pos);
        return Math.max(power + (state.getValue(HIGH) ? 10 : 0) - 1, -1);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int p_55026_, int p_55027_) {
        int note = getNote(state, level, pos);
        if (note == -1)
            return false;
        return super.triggerEvent(state.setValue(NOTE, note), level, pos, p_55026_, p_55027_);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!(player.getMainHandItem().getItem() instanceof RedstoneBooster))
            super.attack(state, level, pos, player);
    }
}
