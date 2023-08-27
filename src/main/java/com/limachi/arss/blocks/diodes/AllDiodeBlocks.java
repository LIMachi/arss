package com.limachi.arss.blocks.diodes;

import com.limachi.arss.blockEntities.*;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;

import static com.limachi.arss.blocks.diodes.BaseAnalogDiodeBlock.*;
import static com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties.*;

@StaticInit
@SuppressWarnings("unused")
public class AllDiodeBlocks {

    static {
        DiodeBlockFactory.builder("signal_generator", AllDiodeBlocks::generator).mode(GENERATOR_MODE).ticking(true).blockEntityBuilder(SignalGeneratorBlockEntity::new).canToggleBothSides(true).finish();
        DiodeBlockFactory.builder("analog_cell", AllDiodeBlocks::cell).mode(MEMORY_MODE).hasPowerTint(true).finish();
        DiodeBlockFactory.builder("adder", AllDiodeBlocks::adder).mode(ADDER_MODE).finish();
        DiodeBlockFactory.builder("analog_and", AllDiodeBlocks::and).canToggleInput(true).finish();
        DiodeBlockFactory.builder("analog_nand", AllDiodeBlocks::nand).canToggleInput(true).finish();
        DiodeBlockFactory.builder("analog_or", AllDiodeBlocks::or).canToggleInput(true).finish();
        DiodeBlockFactory.builder("analog_nor", AllDiodeBlocks::nor).canToggleInput(true).finish();
        DiodeBlockFactory.builder("analog_xor", AllDiodeBlocks::xor).canToggleInput(true).finish();
        DiodeBlockFactory.builder("analog_xnor", AllDiodeBlocks::xnor).canToggleInput(true).finish();
        DiodeBlockFactory.builder("better_comparator", AllDiodeBlocks::comparator).mode(BlockStateProperties.MODE_COMPARATOR).finish();
        DiodeBlockFactory.builder("checker", AllDiodeBlocks::checker).mode(CHECKER_MODE).finish();
        DiodeBlockFactory.builder("delayer", AllDiodeBlocks::delayer).mode(DELAYER_MODE).ticking(true).canToggleBothSides(true).blockEntityBuilder(DelayerBlockEntity::new).finish();
        DiodeBlockFactory.builder("demuxer", AllDiodeBlocks::demuxer).mode(DEMUXER_MODE).canToggleBothSides(true).finish();
        DiodeBlockFactory.builder("edge_detector", AllDiodeBlocks::edge).mode(EDGE_MODE).tickOnceAfterUpdate(true).blockEntityBuilder(EdgeDetectorBlockEntity::new).finish();
        DiodeBlockFactory.builder("shifter", AllDiodeBlocks::shifter).mode(SHIFTER_MODE).canToggleBothSides(true).finish();
    }

    static protected int comparator(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, side <= read ? state.getValue(BlockStateProperties.MODE_COMPARATOR).equals(ComparatorMode.COMPARE) ? read : read - side : 0);
    }

    static protected int adder(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        if (state.getValue(ADDER_MODE).compare())
            return setPower(level, pos, state, side >= read ? read : 0);
        return setPower(level, pos, state, Math.min(side + read, 15));
    }

    static protected int checker(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, state.getValue(CHECKER_MODE).equal() == (read == side) ? read : 0);
    }

    static protected int cell(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        boolean side = sGetAlternateSignal(level, pos, state) > 0;
        ArssBlockStateProperties.MemoryMode mode = state.getValue(MEMORY_MODE);
        int stored = 0;
        if (level.getBlockEntity(pos) instanceof GenericDiodeBlockEntity be)
            stored = be.getOutput();
        if (mode.set() && !side) return setPower(level, pos, state, stored);
        if (mode.reset()) {
            if (side) return setPower(level, pos, state, 0);
            if (read == 0) return setPower(level, pos, state, stored);
        }
        return setPower(level, pos, state, read);
    }

    static protected int edge(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof EdgeDetectorBlockEntity be) {
            int read = sGetInputSignal(level, pos, state);
            int prev =  be.getPreviousInput();
            if (read != prev) {
                EdgeMode mode = state.getValue(EDGE_MODE);
                be.setPreviousInput(read);
                return setPower(level, pos, state, (read > prev && mode.rising()) || (read < prev && mode.falling()) ? Math.max(read, prev) : 0);
            }
        }
        return setPower(level, pos, state, 0);
    }

    static protected int demuxer(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int ord = state.getValue(DEMUXER_MODE).ordinal();
        if (ord < 4)
            return setPower(level, pos, state, (read & (int)Math.pow(2, ord)) != 0 ? 15 : 0);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, side > 0 && (read & (int)Math.pow(2, Math.min(side - 1, 3))) != 0 ? 15 : 0);
    }

    static protected int delayer(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof DelayerBlockEntity delayer) {
            if (test) return setPower(level, pos, state, delayer.state());
            int read = sGetInputSignal(level, pos, state);
            int ord = state.getValue(DELAYER_MODE).ordinal();
            int delay = ord < 4 ? ord : sGetAlternateSignal(level, pos, state);
            return setPower(level, pos, state, delayer.step(read, delay + 1));
        }
        return 0;
    }

    static protected int or(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, read | side);
    }

    static protected int nor(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, (~(read | side)) & 15);
    }

    static protected int and(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, read & side);
    }

    static protected int nand(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, (~(read & side)) & 15);
    }

    static protected int xor(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, read ^ side);
    }

    static protected int xnor(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, (~(read ^ side)) & 15);
    }

    static protected int shifter(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, state.getValue(SHIFTER_MODE).up() ? (read << side) & 15 : (read >> side) & 15);
    }

    static protected int generator(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof SignalGeneratorBlockEntity generator) {
            String mode = state.getValue(GENERATOR_MODE).toString();
            if (test || sGetInputSignal(level, pos, state) == 0) return setPower(level, pos, state, generator.state(mode, sGetAlternateSignals(level, pos, state)));
            return setPower(level, pos, state, generator.step(mode, sGetAlternateSignals(level, pos, state)));
        }
        return 0;
    }

    static protected int programmable(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ProgrammableAnalogGateBlockEntity gate)
            return setPower(level, pos, state, gate.getPower());
        return 0;
    }
}
