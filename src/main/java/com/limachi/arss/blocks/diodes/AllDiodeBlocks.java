package com.limachi.arss.blocks.diodes;

import com.limachi.arss.Arss;
import com.limachi.arss.blockEntities.*;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.arss.items.SequencerMemoryItem;
import com.limachi.lim_lib.blocks.IGetUseSneakWithItemEvent;
import com.limachi.lim_lib.registries.StaticInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

import java.util.function.BiFunction;

import static com.limachi.arss.blocks.diodes.BaseAnalogDiodeBlock.*;
import static com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties.*;

@StaticInit
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Arss.MOD_ID)
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
        DiodeBlockFactory.builder("edge_detector", AllDiodeBlocks::edge).mode(EDGE_MODE).blockEntityBuilder(EdgeDetectorBlockEntity::new).finish();
        DiodeBlockFactory.builder("shifter", AllDiodeBlocks::shifter).mode(SHIFTER_MODE).canToggleBothSides(true).finish();
        DiodeBlockFactory.builder("sequencer", AllDiodeBlocks::sequencer).mode(SEQUENCER_MODE).ticking(true).catchUse(AllDiodeBlocks::sequencerUse).blockEntityBuilder(SequencerBlockEntity::new).canToggleInput(true).itemBuilder((b, p)->new BlockItem(b, p){
            @Override
            public boolean isFoil(@Nonnull ItemStack stack) {
                if (stack.getTag() != null) {
                    CompoundTag tag = stack.getTag();
                    if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND))
                        return SequencerMemoryItem.validTagData(tag.getCompound("BlockEntityTag"));
                }
                return super.isFoil(stack);
            }
        })
                .finish();
        DiodeBlockFactory.builder("programmable_gate", AllDiodeBlocks::programmable).ticking(true).catchUse(AllDiodeBlocks::programmableUse).blockEntityBuilder(ProgrammableGateBlockEntity::new).itemBuilder((b, p)->new BlockItem(b, p){
                    @Override
                    public boolean isFoil(@Nonnull ItemStack stack) {
                        if (stack.getTag() != null) {
                            CompoundTag tag = stack.getTag();
                            if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND))
                                return true; //could test if the tags are valid (256 bytes in the range 0-16 inclusive)
                        }
                        return super.isFoil(stack);
                    }
                })
                .finish();
    }

    static protected int comparator(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, side <= read ? state.getValue(BlockStateProperties.MODE_COMPARATOR).equals(ComparatorMode.COMPARE) ? read : read - side : 0);
    }

    static protected int adder(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        if (state.getValue(ADDER_MODE).compare())
            return setPower(level, pos, state, side >= read ? read : 0);
        return setPower(level, pos, state, Math.min(side + read, 15));
    }

    static protected int checker(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, state.getValue(CHECKER_MODE).equal() == (read == side) ? read : 0);
    }

    static protected int cell(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        boolean side = sGetAlternateSignal(level, pos, state) > 0;
        MemoryMode mode = state.getValue(MEMORY_MODE);
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
            int read = sGetInputSignal(level, pos, state, true);
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
        int read = sGetInputSignal(level, pos, state, true);
        int ord = state.getValue(DEMUXER_MODE).ordinal();
        if (ord < 4)
            return setPower(level, pos, state, (read & (int)Math.pow(2, ord)) != 0 ? 15 : 0);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, side > 0 && (read & (1 << side - 1)) != 0 ? 15 : 0);
    }

    static protected int delayer(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof DelayerBlockEntity delayer) {
            if (test) return setPower(level, pos, state, delayer.state());
            int read = sGetInputSignal(level, pos, state, true);
            int ord = state.getValue(DELAYER_MODE).ordinal();
            int delay = ord < 4 ? ord : sGetAlternateSignal(level, pos, state);
            return setPower(level, pos, state, delayer.step(read, delay + 1));
        }
        return 0;
    }

    static protected int or(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, read | side);
    }

    static protected int nor(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, (~(read | side)) & 15);
    }

    static protected int and(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, read & side);
    }

    static protected int nand(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, (~(read & side)) & 15);
    }

    static protected int xor(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, read ^ side);
    }

    static protected int xnor(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, (~(read ^ side)) & 15);
    }

    static protected int shifter(boolean test, Level level, BlockPos pos, BlockState state) {
        int read = sGetInputSignal(level, pos, state, true);
        int side = sGetAlternateSignal(level, pos, state);
        return setPower(level, pos, state, state.getValue(SHIFTER_MODE).up() ? (read << side) & 15 : (read >> side) & 15);
    }

    static protected int generator(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof SignalGeneratorBlockEntity generator) {
            String mode = state.getValue(GENERATOR_MODE).toString();
            if (test || sGetInputSignal(level, pos, state, false) == 0) return setPower(level, pos, state, generator.state(mode, sGetAlternateSignals(level, pos, state)));
            return setPower(level, pos, state, generator.step(mode, sGetAlternateSignals(level, pos, state)));
        }
        return 0;
    }

    static protected int sequencer(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof SequencerBlockEntity sequencer) {
            if (test) return setPower(level, pos, state, sequencer.readPower());
            int read = sGetInputSignal(level, pos, state, true);
//            setPower(level, pos, state, 0);
            updateNeighborsInFront(state.getBlock(), level, pos, state);
            Pair<Integer, Integer> side = sGetAlternateSignals(level, pos, state);
            return setPower(level, pos, state, sequencer.update(read, side.getFirst(), side.getSecond()));
        }
        return 0;
    }

    static protected int programmable(boolean test, Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ProgrammableGateBlockEntity gate) {
            gate.update(sGetInputSignal(level, pos, state, true), sGetAlternateSignal(level, pos, state));
            return gate.getOutput();
        }
        return 0;
    }

    static protected InteractionResult sequencerUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof SequencerBlockEntity be) {
            be.startEditing(player);

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    static protected InteractionResult programmableUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ProgrammableGateBlockEntity be) {
            if (!player.isShiftKeyDown()) {
                be.startEditing(player);

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (player.getMainHandItem().getItem() instanceof BlockItem bi && bi.getBlock() instanceof BaseAnalogDiodeBlock diode) {
                BiFunction<Integer, Integer, Integer> t = switch (diode.name) {
                    case "adder" -> (back, side) -> Mth.clamp(back + side, 0, 15);
                    case "better_comparator" -> (back, side) -> Mth.clamp(back - side, 0, 15);
                    case "analog_cell" -> (back, side) -> side > 0 ? back : 16;
                    case "analog_and" -> (back, side) -> back & side;
                    case "analog_or" -> (back, side) -> back | side;
                    case "analog_xor" -> (back, side) -> back ^ side;
                    case "analog_nand" -> (back, side) -> ~(back & side) & 15;
                    case "analog_nor" -> (back, side) -> ~(back | side) & 15;
                    case "analog_xnor" -> (back, side) -> ~(back ^ side) & 15;
                    case "checker" -> (back, side) -> back.equals(side) ? 15 : 0;
                    case "shifter" -> (back, side) -> (back << side) & 15;
                    case "demuxer" -> (back, side) -> side > 0 && (back & (1 << side - 1)) != 0 ? 15 : 0;
                    case "programmable_gate" -> {
                        CompoundTag tag = player.getMainHandItem().getTag();
                        BiFunction<Integer, Integer, Integer> out = null;
                        if (tag != null && tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
                            tag = tag.getCompound("BlockEntityTag");
                            if (tag.contains("layout", Tag.TAG_BYTE_ARRAY)) {
                                final byte[] ar = tag.getByteArray("layout");
                                out = (back, side) -> (int)ar[back + side * 16];
                            }
                        }
                        yield out;
                    }
                    default -> null;
                };
                if (t != null) {
                    for (int back = 0; back < 16; ++back)
                        for (int side = 0; side < 16; ++side)
                            be.layout[back + side * 16] = (byte) (int) t.apply(back, side);
                    be.setChanged();
                    level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 2);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @SubscribeEvent
    public static void acceptSneakUseOfBlockWithItem(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockEntity(event.getHitVec().getBlockPos()) instanceof ProgrammableGateBlockEntity && event.getItemStack().getItem() instanceof BlockItem bi && bi.getBlock() instanceof BaseAnalogDiodeBlock diode) {
            event.setUseBlock(Event.Result.ALLOW);
            event.setUseItem(Event.Result.DENY);
        }
    }
}
