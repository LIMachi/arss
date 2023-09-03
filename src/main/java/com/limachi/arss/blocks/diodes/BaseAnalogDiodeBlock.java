package com.limachi.arss.blocks.diodes;

import com.limachi.arss.blockEntities.GenericDiodeBlockEntity;
import com.limachi.arss.blocks.AnalogRedstoneTorchBlock;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.SoundUtils;
import com.limachi.lim_lib.blockEntities.IOnUseBlockListener;
import com.limachi.lim_lib.registries.StaticInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties.*;

/**
 * common class for comparator like blocks (get power at the back by getting analog signal/item frame) and expect potential power on the sides (the highest value)
 * used by:
 *   analog cell
 *   better comparator
 *   adder
 *   checker
 *   edge detector
 *   demuxer
 *   analog delayer
 *   signal generator
 *   analog gates (or, nor, and, nand, xor, xnor)
 */

@StaticInit
@SuppressWarnings({"deprecation", "unused"})
public abstract class BaseAnalogDiodeBlock extends DiodeBlock {

    @Configs.Config(reload = true, cmt="Read sides like the back (ex: will use the content of a chest on the side as a valid redstone signal)")
    static public boolean ALL_POWERS_ON_SIDES = true;

    protected BaseAnalogDiodeBlock(Properties props) { super(props); }

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    /**
     * overridable variables
     */
    protected String name = null; //string used for localisation
    protected int delay = 1; //delay (in redstone ticks) between updates
    protected boolean tickOnceAfterUpdate = false; //if set to true, tick 2 times instead of 1 when there is an update (useful for pulses)
    protected boolean isTicking = false; //make this block tick all the time, incompatible with 'tickOnceAfterUpdate'
    protected EnumProperty<?> modeProp = null; //most of the overrides of this block will have a mode, so I have standardised the way we handle them

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, POWER, SIDES, BOOSTED);
        if (modeProp != null)
            builder.add(modeProp);
    }

    public Pair<String, EnumProperty<?>> instanceType() { return new Pair<>(name, modeProp); }

    @Override
    protected int getDelay(@Nonnull BlockState state) { return state.getValue(BOOSTED) ? 1 : 2 * delay; }

    @Override
    protected int getOutputSignal(@Nonnull BlockGetter level, @Nonnull BlockPos pos, BlockState state) { return state.getValue(POWER); }

    abstract protected int calculateOutputSignal(boolean test, Level level, BlockPos pos, BlockState state);

    abstract protected ArssBlockStateProperties.SideToggling cycleSideStates(ArssBlockStateProperties.SideToggling current, boolean shifting);

    @Override
    protected boolean shouldTurnOn(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        return calculateOutputSignal(true, level, pos, state) > 0;
    }

    public static int sGetInputSignal(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseAnalogDiodeBlock && state.getValue(SIDES) != ArssBlockStateProperties.SideToggling.INPUT_DISABLED)
            return ((BaseAnalogDiodeBlock)state.getBlock()).getInputSignal(level, pos, state);
        return 0;
    }

    public static int sGetAlternateSignal(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseAnalogDiodeBlock) {
            Pair<Integer, Integer> s = sGetAlternateSignals(level, pos, state);
            return Math.max(s.getFirst(), s.getSecond());
        }
        return 0;
    }

    public static Pair<Integer, Integer> sGetAlternateSignals(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseAnalogDiodeBlock instance) {
            Direction direction = state.getValue(FACING);
            Direction clock = direction.getClockWise();
            ArssBlockStateProperties.SideToggling sides = state.getValue(SIDES);
            int left = sides.acceptLeft() ? instance.getAlternateSignalAt(level, pos.relative(clock), clock) : 0;
            Direction counter = direction.getCounterClockWise();
            int right = sides.acceptRight() ? instance.getAlternateSignalAt(level, pos.relative(counter), counter) : 0;
            return Pair.of(left, right);
        }
        return Pair.of(0, 0);
    }

    protected int getDirectionalSignal(Level level, BlockPos pos, BlockState state, Direction dir) {
        BlockPos blockpos = pos.relative(dir);
        int i = level.getSignal(blockpos, dir);
        if (i >= 15) {
            return i;
        } else {
            BlockState blockstate = level.getBlockState(blockpos);
            return Math.max(i, blockstate.is(Blocks.REDSTONE_WIRE) ? blockstate.getValue(RedStoneWireBlock.POWER) : 0);
        }
    }

    protected int commonSignalGetter(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Direction back) {
        int i = getDirectionalSignal(level, pos, state, back);
        BlockPos backPos = pos.relative(back);
        BlockState t_state = level.getBlockState(backPos);
        if (t_state.hasAnalogOutputSignal())
            i = t_state.getAnalogOutputSignal(level, backPos);
        else if (i < 15 && t_state.isRedstoneConductor(level, backPos)) {
            backPos = backPos.relative(back);
            t_state = level.getBlockState(backPos);
            ItemFrame itemframe = getItemFrame(level, back, backPos);
            int j = Math.max(itemframe == null ? Integer.MIN_VALUE : itemframe.getAnalogOutput(), t_state.hasAnalogOutputSignal() ? t_state.getAnalogOutputSignal(level, backPos) : Integer.MIN_VALUE);
            if (j != Integer.MIN_VALUE) {
                i = j;
            }
        }
        return Mth.clamp(i, 0, 15);
    }

    @Override
    protected int getInputSignal(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        return commonSignalGetter(level, pos, state, state.getValue(FACING));
    }

    @Nullable
    private ItemFrame getItemFrame(Level level, Direction dir, BlockPos pos) {
        List<ItemFrame> list = level.getEntitiesOfClass(ItemFrame.class, new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), e -> e != null && e.getDirection() == dir);
        return list.size() == 1 ? list.get(0) : null;
    }

    protected static <T> T findPrevInCollection(Collection<T> col, T find) {
        Iterator<T> iterator = col.iterator();
        T prev = iterator.next();

        while(iterator.hasNext()) {
            T t = iterator.next();
            if (t.equals(find))
                return prev;
            prev = t;
        }

        return prev;
    }

    public <T extends Comparable<T>> BlockState cycleBack(BlockState state, Property<T> prop) {
        return state.setValue(prop, findPrevInCollection(prop.getPossibleValues(), state.getValue(prop)));
    }

    @Override
    public @Nonnull InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        Item held = player.getItemInHand(hand).getItem();
        if (held == Items.REDSTONE_TORCH || held == AnalogRedstoneTorchBlock.AnalogRedstoneTorchItem.R_ITEM.get()) {
            ArssBlockStateProperties.SideToggling newSidedness = cycleSideStates(state.getValue(SIDES), player.isShiftKeyDown());
            state = state.setValue(SIDES, newSidedness);
            level.setBlock(pos, state, 3);
            player.displayClientMessage(Component.translatable("display.arss.diode_block.sidedness." + newSidedness), true);
            refreshOutputState(level, pos, state, true);
            return InteractionResult.SUCCESS;
        }
        if (player.getAbilities().mayBuild) {
            if (modeProp != null) {
                state = player.isShiftKeyDown() ? cycleBack(state, modeProp) : state.cycle(modeProp);
                Enum<?> s = state.getValue(modeProp);
                SoundUtils.playComparatorClick(level, pos, s.ordinal());
                player.displayClientMessage(Component.translatable("display.arss." + name + ".mode." + s), true);
                level.setBlockAndUpdate(pos, state);
                refreshOutputState(level, pos, state, true);
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (this instanceof EntityBlock && level.getBlockEntity(pos) instanceof IOnUseBlockListener useListener) {
                InteractionResult out = useListener.use(state, level, pos, player, hand, hit);
                refreshOutputState(level, pos, state, isTicking);
                return out;
            }

        }
        return InteractionResult.PASS;
    }

    @Override
    protected void checkTickOnNeighbor(@NotNull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (!level.getBlockTicks().willTickThisTick(pos, this)) {
            int newPower = calculateOutputSignal(true, level, pos, state);
            if (newPower != state.getValue(POWER))
                level.scheduleTick(pos, this, getDelay(state), shouldPrioritize(level, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL);
        }
    }

    static int setPower(Level level, BlockPos pos, BlockState state, int power) {
        if (level.getBlockEntity(pos) instanceof GenericDiodeBlockEntity be) {
            be.setOutput(Mth.clamp(power, 0, 15));
            return be.getOutput();
        }
        return 0;
    }

    protected int getAlternateSignalAt(@Nonnull LevelReader level, @Nonnull BlockPos pos, @Nonnull Direction dir) {
        if (ALL_POWERS_ON_SIDES) {
            BlockPos start = pos.relative(dir.getOpposite());
            return commonSignalGetter((Level) level, start, level.getBlockState(start), dir);
        }
        return level.getControlInputSignal(pos.relative(dir), dir, this.sideInputDiodesOnly());
    }

    private void refreshOutputState(Level level, BlockPos pos, BlockState state, boolean recalculate) {
        int nextPower = 0;
        if (recalculate) {
            nextPower = calculateOutputSignal(false, level, pos, state);
            if (level.getBlockEntity(pos) instanceof GenericDiodeBlockEntity be)
                be.setOutput(nextPower);
        } else if (level.getBlockEntity(pos) instanceof GenericDiodeBlockEntity be)
            nextPower = be.getOutput();
        if (state.getValue(POWER) != nextPower) {
            boolean flag1 = nextPower > 0;
            boolean flag = state.getValue(POWERED);

            if (flag && !flag1)
                level.setBlock(pos, state.setValue(POWERED, false).setValue(POWER, 0), 2);
            else if (!flag && flag1)
                level.setBlock(pos, state.setValue(POWERED, true).setValue(POWER, nextPower), 2);
            else
                level.setBlock(pos, state.setValue(POWER, nextPower), 2);

            if (tickOnceAfterUpdate)
                level.scheduleTick(pos, this, getDelay(state));

            updateNeighborsInFront(level, pos, state);
        }
    }

    @Override
    public void tick(@Nonnull BlockState state, @Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull RandomSource rng) {
        refreshOutputState(level, pos, state, isTicking);
        if (isTicking) {
            int delay = getDelay(state);
            level.scheduleTick(pos, this, delay > 0 ? delay : 1);
        }
    }

    @Override
    public boolean getWeakChanges(BlockState state, net.minecraft.world.level.LevelReader world, BlockPos pos) {
        return state.getBlock() instanceof BaseAnalogDiodeBlock;
    }

    @Override
    public void onNeighborChange(BlockState state, net.minecraft.world.level.LevelReader world, BlockPos pos, BlockPos neighbor) {
        if (pos.getY() == neighbor.getY() && world instanceof Level && !world.isClientSide()) {
            state.neighborChanged((Level)world, pos, world.getBlockState(neighbor).getBlock(), neighbor, false);
        }
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull LivingEntity player, @Nonnull ItemStack stack) {
        if (isTicking || shouldTurnOn(level, pos, state))
            level.scheduleTick(pos, this, 1);
    }

    public static int relativeInputSignal(Level level, BlockPos pos, Direction dir) {
        BlockPos target = pos.relative(dir);
        int strength = level.getSignal(target, dir);
        if (strength >= 15)
            return strength;
        BlockState state = level.getBlockState(target);
        return Math.max(strength, state.is(Blocks.REDSTONE_WIRE) ? state.getValue(RedStoneWireBlock.POWER) : 0);
    }
}
