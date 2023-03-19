package com.limachi.arss.blocks.diodes;

import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.arss.blockEntities.DelayerBlockEntity;
import com.limachi.arss.blockEntities.ProgrammableAnalogGateBlockEntity;
import com.limachi.arss.blockEntities.SignalGeneratorBlockEntity;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;

import static com.limachi.arss.blocks.diodes.BaseAnalogDiodeBlock.*;
import static com.limachi.arss.ArssBlockStateProperties.*;

@StaticInit
public class AllDiodeBlocks {

    //new gate: programmable analog gate:
    //craft: x5 gates (gate ratio 5/13 < cell gate ratio 1/2)
    //CAC
    //NDP
    //CXC
    //C->analog cell
    //A->analog and
    //N->better_comparator
    //D->demuxer
    //P->adder
    //X->analog xor
    //can program specific output for each of the 4 bits according to combination of 3 entries (similar to rftool's logic gate), each of the 8 combination can be toggled to 6 modes (on/off/keep/copy1/copy2/copy4/copy8)
    static {
        DiodeBlockFactory.create("adder", ADDER_MODE, AllDiodeBlocks::adder);
        DiodeBlockFactory.create("analog_and", AllDiodeBlocks::and);
        DiodeBlockFactory.create("analog_cell", MEMORY_MODE, AllDiodeBlocks::cell, true);
        DiodeBlockFactory.create("analog_nand", AllDiodeBlocks::nand);
        DiodeBlockFactory.create("analog_nor", AllDiodeBlocks::nor);
        DiodeBlockFactory.create("analog_or", AllDiodeBlocks::or);
        DiodeBlockFactory.create("analog_xnor", AllDiodeBlocks::xnor);
        DiodeBlockFactory.create("analog_xor", AllDiodeBlocks::xor);
        DiodeBlockFactory.create("better_comparator", BlockStateProperties.MODE_COMPARATOR, AllDiodeBlocks::comparator);
        DiodeBlockFactory.create("checker", CHECKER_MODE, AllDiodeBlocks::checker);
        DiodeBlockFactory.create("delayer", DELAYER_MODE, AllDiodeBlocks::delayer, DelayerBlockEntity::new);
        DiodeBlockFactory.create("demuxer", DEMUXER_MODE, AllDiodeBlocks::demuxer);
        DiodeBlockFactory.create("edge_detector", EDGE_MODE, AllDiodeBlocks::edge, true, false, PREVIOUS_READ_POWER);
        DiodeBlockFactory.create("shifter", SHIFTER_MODE, AllDiodeBlocks::shifter);
        DiodeBlockFactory.create("signal_generator", GENERATOR_MODE, AllDiodeBlocks::generator, SignalGeneratorBlockEntity::new);
//        DiodeBlockFactory.create("programmable_analog_gate", AllDiodeBlocks::programmable, ProgrammableAnalogGateBlockEntity::new);
    }

    static protected BlockState comparator(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, side <= read ? state.getValue(BlockStateProperties.MODE_COMPARATOR).equals(ComparatorMode.COMPARE) ? read : read - side : 0);
    }

    static protected BlockState adder(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        if (state.getValue(ADDER_MODE).compare())
            return setPower(state, side >= read ? read : 0);
        return setPower(state, Math.min(side + read, 15));
    }

    static protected BlockState checker(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, state.getValue(CHECKER_MODE).equal() == (read == side) ? read : 0);
    }

    static protected BlockState cell(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        boolean side = sGetAlternateSignal(level, pos, state) > 0;
        ArssBlockStateProperties.MemoryMode mode = state.getValue(MEMORY_MODE);
        int stored = state.getValue(POWER);
        if (mode.set() && !side) return setPower(state, stored);
        if (mode.reset()) {
            if (side) return setPower(state, 0);
            if (read == 0) return setPower(state, stored);
        }
        return setPower(state, read);
    }

    static protected BlockState edge(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int prev = state.getValue(PREVIOUS_READ_POWER);
        if (read != prev) {
            EdgeMode mode = state.getValue(EDGE_MODE);
            BlockState newState = state.setValue(PREVIOUS_READ_POWER, read);
            return setPower(newState, (read > prev && mode.rising()) || (read < prev && mode.falling()) ? 15 : 0);
        }
        return setPower(state, 0);
    }

    static protected BlockState demuxer(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int ord = state.getValue(DEMUXER_MODE).ordinal();
        if (ord < 4)
            return setPower(state, (read & (int)Math.pow(2, ord)) != 0 ? 15 : 0);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, side > 0 && (read & (int)Math.pow(2, Math.min(side - 1, 3))) != 0 ? 15 : 0);
    }

    static protected BlockState delayer(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof DelayerBlockEntity delayer) {
            if (test) return setPower(state, delayer.state());
            int read = sGetInputSignal(level, pos, state);
            int ord = state.getValue(DELAYER_MODE).ordinal();
            int delay = ord < 4 ? (int)Math.pow(2, ord) - 1 : sGetAlternateSignal(level, pos, state);
            return setPower(state, delayer.step(read, delay));
        }
        return state;
    }

    static protected BlockState or(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, read | side);
    }

    static protected BlockState nor(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, (~(read | side)) & 15);
    }

    static protected BlockState and(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, read & side);
    }

    static protected BlockState nand(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, (~(read & side)) & 15);
    }

    static protected BlockState xor(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, read ^ side);
    }

    static protected BlockState xnor(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, (~(read ^ side)) & 15);
    }

    static protected BlockState shifter(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(state, state.getValue(SHIFTER_MODE).up() ? (read << side) & 15 : (read >> side) & 15);
    }

    static protected BlockState generator(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof SignalGeneratorBlockEntity generator) {
            String mode = state.getValue(GENERATOR_MODE).toString();
            if (test || sGetInputSignal(level, pos, state) == 0) return setPower(state, generator.state(mode, sGetAlternateSignals(level, pos, state)));
            return setPower(state, generator.step(mode, sGetAlternateSignals(level, pos, state)));
        }
        return state;
    }

    static protected BlockState programmable(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ProgrammableAnalogGateBlockEntity gate)
            return setPower(state, gate.getPower());
        return state;
    }
}
