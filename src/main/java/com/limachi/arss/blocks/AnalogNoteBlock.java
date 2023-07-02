package com.limachi.arss.blocks;

import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.lim_lib.SoundUtils;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent; //VERSION 1.18.2
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class AnalogNoteBlock extends NoteBlock {

    public static final Properties PROPS = Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(0.8F);

    @RegisterBlock(name = "analog_note_block")
    public static RegistryObject<Block> R_BLOCK;

    @RegisterBlockItem(name = "analog_note_block", block = "analog_note_block", jeiInfoKey = "jei.info.analog_note_block")
    public static RegistryObject<Item> R_ITEM;

    public static final BooleanProperty HIGH = ArssBlockStateProperties.HIGH;

    public AnalogNoteBlock() {
        super(PROPS);
        registerDefaultState(stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, 0).setValue(POWERED, false).setValue(HIGH, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INSTRUMENT, POWERED, NOTE, HIGH);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55045_, boolean p_55046_) {
        int power = level.getBestNeighborSignal(pos);
        int _new = getNote(state, level, pos);
        if (!((_new == -1 && power == 0) || _new == state.getValue(NOTE))) {
            _new = net.minecraftforge.common.ForgeHooks.onNoteChange(level, pos, state, state.getValue(NOTE), _new);
            if (_new == -1) return;
        }
        if (_new != state.getValue(NOTE))
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)).setValue(POWERED, power > 0), 3);
        if (power > 0)
            playNote(level, pos);
    }

    private void playNote(Level level, BlockPos pos) {
        if (level.getBlockState(pos.above()).isAir())
            level.blockEvent(pos, this, 0, 0);
    }

    @Override
    public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        else {
            int power = level.getBestNeighborSignal(pos);
            int _new = getNote(state, level, pos);
            if (!((_new == -1 && power == 0) || _new == state.getValue(NOTE))) {
                _new = net.minecraftforge.common.ForgeHooks.onNoteChange(level, pos, state, state.getValue(NOTE), _new);
                if (_new == -1) return InteractionResult.FAIL;
            }
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)).setValue(HIGH, !state.getValue(HIGH)), 3);
            player.displayClientMessage(
//                    Component.translatable( //VERSION 1.19.2
                    new TranslatableComponent( //VERSION 1.18.2
                            "display.arss.analog_note_block.high_pitch." + level.getBlockState(pos).getValue(HIGH)), true);
            playNote(level, pos);
            player.awardStat(Stats.TUNE_NOTEBLOCK);
            return InteractionResult.CONSUME;
        }
    }

    protected int getNote(BlockState state, Level level, BlockPos pos) {
        int power = level.getBestNeighborSignal(pos);
        return power > 0 ? power + (state.getValue(HIGH) ? 9 : -1) : -1;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {}

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int p_55026_, int p_55027_) {
        int note = getNote(state, level, pos);
        if (note != -1) {
            SoundUtils.playNoteWithEvent(level, pos, state.getValue(INSTRUMENT), note);
            level.addParticle(ParticleTypes.NOTE, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.2D, (double)pos.getZ() + 0.5D, (double)note / 24.0D, 0.0D, 0.0D);
            return true;
        }
        return false;
    }
}
