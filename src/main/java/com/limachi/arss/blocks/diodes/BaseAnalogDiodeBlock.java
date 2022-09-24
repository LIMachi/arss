package com.limachi.arss.blocks.diodes;

import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.integration.theOneProbePlugin.IProbeInfoGiver;
import com.limachi.lim_lib.registries.StaticInit;
import com.mojang.datafixers.util.Pair;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
public abstract class BaseAnalogDiodeBlock extends DiodeBlock implements IProbeInfoGiver {

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
        builder.add(FACING, POWERED, POWER);
        if (modeProp != null)
            builder.add(modeProp);
    }

    @Override
    protected int getDelay(@NotNull BlockState state) { return 2 * delay; }

    @Override
    protected int getOutputSignal(@NotNull BlockGetter level, @NotNull BlockPos pos, BlockState state) { return state.getValue(POWER); }

    abstract protected BlockState calculateOutputSignal(boolean test, Level level, BlockPos pos, BlockState state);

    @Override
    protected boolean shouldTurnOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return calculateOutputSignal(true, level, pos, state).getValue(POWER) > 0;
    }

    public static int sGetInputSignal(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseAnalogDiodeBlock)
            return ((BaseAnalogDiodeBlock)state.getBlock()).getInputSignal(level, pos, state);
        return 0;
    }

    public static int sGetAlternateSignal(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseAnalogDiodeBlock)
            return ((BaseAnalogDiodeBlock)state.getBlock()).getAlternateSignal(level, pos, state);
        return 0;
    }

    public static Pair<Integer, Integer> sGetAlternateSignals(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseAnalogDiodeBlock instance) {
            Direction direction = state.getValue(FACING);
            Direction clock = direction.getClockWise();
            Direction counter = direction.getCounterClockWise();
            return Pair.of(instance.getAlternateSignalAt(level, pos.relative(clock), clock), instance.getAlternateSignalAt(level, pos.relative(counter), counter));
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

    protected int commonSignalGetter(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Direction back) {
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
        return i;
    }

    @Override
    protected int getInputSignal(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
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
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (modeProp != null && player.getAbilities().mayBuild) {
            state = player.isShiftKeyDown() ? cycleBack(state, modeProp) : state.cycle(modeProp);
            Enum<?> s = state.getValue(modeProp);
            float f = s.ordinal() * 0.05F + 0.5F;
            level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
            player.displayClientMessage(Component.translatable("display.arss." + name + ".mode." + s), true);
            level.setBlock(pos, state, 2);
            refreshOutputState(level, pos, state);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void checkTickOnNeighbor(Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (!level.getBlockTicks().willTickThisTick(pos, this)) {
            BlockState newState = calculateOutputSignal(true, level, pos, state);
            if (newState != state) {
                    boolean turn_on = shouldTurnOn(level, pos, state);
                    TickPriority tickpriority = TickPriority.HIGH;
                    if (shouldPrioritize(level, pos, state))
                        tickpriority = TickPriority.VERY_HIGH;
                    else if (turn_on)
                        tickpriority = TickPriority.EXTREMELY_HIGH;
                    level.scheduleTick(pos, this, getDelay(state), tickpriority);

            }
        }
    }

    static BlockState setPower(BlockState state, int power) {
        return state.getValue(POWER) != power ? state.setValue(POWER, power) : state;
    }

    @Override
    protected int getAlternateSignalAt(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull Direction dir) {
        if (ALL_POWERS_ON_SIDES) {
            BlockPos start = pos.relative(dir.getOpposite());
            return commonSignalGetter((Level) level, start, level.getBlockState(start), dir);
        }
        return super.getAlternateSignalAt(level, pos, dir);
    }

    private void refreshOutputState(Level level, BlockPos pos, BlockState state) {
        BlockState newState = calculateOutputSignal(false, level, pos, state);

        if (state != newState) {
            boolean flag1 = newState.getValue(POWER) > 0;
            boolean flag = state.getValue(POWERED);

            if (flag && !flag1)
                level.setBlock(pos, newState.setValue(POWERED, false), 2);
            else if (!flag && flag1)
                level.setBlock(pos, newState.setValue(POWERED, true), 2);
            else
                level.setBlock(pos, newState, 2);

            if (tickOnceAfterUpdate)
                level.scheduleTick(pos, this, getDelay(state));

            updateNeighborsInFront(level, pos, state);
        }
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource rng) {
        refreshOutputState(level, pos, state);
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
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull LivingEntity player, @NotNull ItemStack stack) {
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

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level, BlockState blockState, IProbeHitData iProbeHitData) {
        IProbeInfo v = iProbeInfo.vertical(iProbeInfo.defaultLayoutStyle().spacing(2));
        IProbeInfo h = v.horizontal(iProbeInfo.defaultLayoutStyle().spacing(2).alignment(ElementAlignment.ALIGN_TOPLEFT));
        h.item(new ItemStack(Items.REDSTONE), new ItemStyle().height(14).width(14)).text(Component.translatable("top.info.power", blockState.getValue(POWER).toString()));
        if (modeProp != null)
            v.text(Component.translatable("top.info.mode").append(Component.translatable("display.arss." + name + ".mode." + blockState.getValue(modeProp))));
    }
}
