package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.blockEntities.AnalogNoteBlockBlockEntity;
import com.limachi.arss.items.BlockItemWithCustomRenderer;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties.*;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class AnalogNoteBlock extends NoteBlock implements EntityBlock {

    @RegisterBlock(name = "analog_note_block")
    public static RegistryObject<Block> R_BLOCK;

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnalogNoteBlockBlockEntity(pos, state);
    }

    public static class AnalogNoteBlockItem extends BlockItemWithCustomRenderer {

        @RegisterItem(name = "analog_note_block")
        public static RegistryObject<BlockItem> R_ITEM;

        public AnalogNoteBlockItem() { super(R_BLOCK.get(), new Item.Properties(), Blocks.NOTE_BLOCK); }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, level, components, flags);
        Arss.commonHoverText("analog_note_block", components);
    }

    public AnalogNoteBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.NOTE_BLOCK));
        registerDefaultState(stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, 0).setValue(POWERED, false).setValue(HIGH, false).setValue(HIDE_DOT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INSTRUMENT, POWERED, NOTE, HIGH, HIDE_DOT);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55045_, boolean p_55046_) {
        int power = level.getBestNeighborSignal(pos);
        int prevPower = level.getBlockEntity(pos) instanceof AnalogNoteBlockBlockEntity be ? be.getPreviousInput() : 0;
        int _new = getNote(state, level, pos, false);
        if (!((_new == -1 && power == 0) || _new == state.getValue(NOTE))) {
            _new = net.minecraftforge.common.ForgeHooks.onNoteChange(level, pos, state, state.getValue(NOTE), _new);
            if (_new == -1) return;
        }
        if (power != prevPower) {
            if (level.getBlockEntity(pos) instanceof AnalogNoteBlockBlockEntity be)
                be.setPreviousInput(power);
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)).setValue(POWERED, power > 0), 3);
            if (power > 0)
                playNote(state, level, pos);
        } else if (_new != state.getValue(NOTE))
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)), 3);
    }

    private void playNote(BlockState state, Level level, BlockPos pos) {
        if (state.getValue(INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(pos.above()).isAir()) {
            level.blockEvent(pos, this, 0, 0);
        }
    }

    @Override
    public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Item held = player.getItemInHand(hand).getItem();
        if (held == Items.REDSTONE_TORCH || held == AnalogRedstoneTorchBlock.AnalogRedstoneTorchItem.R_ITEM.get()) {
            level.setBlock(pos, state.setValue(HIDE_DOT, !state.getValue(HIDE_DOT)), 3);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        else {
            int power = level.getBestNeighborSignal(pos);
            int _new = getNote(state, level, pos, false);
            if (!((_new == -1 && power == 0) || _new == state.getValue(NOTE))) {
                _new = net.minecraftforge.common.ForgeHooks.onNoteChange(level, pos, state, state.getValue(NOTE), _new);
                if (_new == -1) return InteractionResult.FAIL;
            }
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)).setValue(HIGH, !state.getValue(HIGH)), 3);
            player.displayClientMessage(Component.translatable("display.arss.analog_note_block.high_pitch." + level.getBlockState(pos).getValue(HIGH)), true);
            playNote(state, level, pos);
            player.awardStat(Stats.TUNE_NOTEBLOCK);
            return InteractionResult.CONSUME;
        }
    }

    protected int getNote(BlockState state, Level level, BlockPos pos, boolean clamp) {
        int power = level.getBestNeighborSignal(pos);
        return Math.max(power + (state.getValue(HIGH) ? 10 : 0) - 1, clamp ? 0 : -1);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int p_55026_, int p_55027_) {
        return super.triggerEvent(state.setValue(NOTE, getNote(state, level, pos, true)), level, pos, p_55026_, p_55027_);
    }
}
