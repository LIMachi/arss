package com.limachi.arss.common.blocks.diodes;

import com.limachi.arss.Arss;
import com.limachi.arss.common.block_entities.BaseAnalogDiodeBlockEntity;
import com.limachi.arss.common.block_entities.IOnUseBlockListener;

import com.limachi.lim_lib.common.annotations.Config;
import com.limachi.lim_lib.common.blocks.IAcceptCrouchInteractWithItem;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import static com.limachi.arss.common.ArssBlockStateProperties.*;

@SuppressWarnings({"deprecation", "unused"})
public abstract class BaseAnalogDiodeBlock extends DiodeBlock implements IAcceptCrouchInteractWithItem {
    @Config(path = "Diodes", name = "SidesAsReaders", reload = true, cmt="Read sides like the back (ex: will use the content of a chest or the orientation of an item frame on the side as a valid redstone signal)")
    static public boolean ALL_POWERS_ON_SIDES = true;

    protected BaseAnalogDiodeBlock(Properties props) { super(props); }

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public enum TickingMode {
        ON_CHANGE, //tick once per change (default behavior of reactint diodes)
        ON_CHANGE_AND_RECHECK, //tick twice (pulse/fast diodes)
        ALWAYS; //always tick (diodes using complex logic or delays)

        public boolean ticking() { return this == ALWAYS; }
        public boolean recheck() { return this == ON_CHANGE_AND_RECHECK; }
    }

//
//     overridable variables
//
    protected String name = null; //string used for localisation
    protected int delay = 1; //delay (in redstone ticks) between updates
    protected TickingMode tickingMode = TickingMode.ON_CHANGE; //if set to true, tick 2 times instead of 1 when there is an update (useful for pulses)
    protected EnumProperty<?> modeProp = null; //most of the overrides of this block will have a mode, so I have standardised the way we handle them

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, POWER, SIDES, BOOSTED);
        if (modeProp != null)
            builder.add(modeProp);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state != null) {
//            CompoundTag tag = ctx.getItemInHand().getTag();
            CustomData tag = ctx.getItemInHand().getComponents().get(DataComponents.BLOCK_ENTITY_DATA);
            if (tag != null/* && tag.contains("BlockEntityTag")*/) {
                CompoundTag bet = /*tag.getCompound("BlockEntityTag")*/tag.getUnsafe();
                if (bet.contains("Mode", Tag.TAG_INT) && modeProp != null)
                    state = setValueByClampedIndex(state, modeProp, bet.getInt("Mode"));
                if (bet.contains("Sides", Tag.TAG_INT))
                    state = setValueByClampedIndex(state, SIDES, bet.getInt("Sides"));
            }
        }
        return state;
    }

    public Pair<String, EnumProperty<?>> instanceType() { return new Pair<>(name, modeProp); }

    @Override
    protected int getDelay(BlockState state) { return state.getValue(BOOSTED) ? 1 : 2 * delay; }

    @Override
    protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) { return state.getValue(POWER); }

    abstract protected int calculateOutputSignal(boolean test, Level level, BlockPos pos, BlockState state);

    abstract protected SideToggling cycleSideStates(SideToggling current, boolean shifting);

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
        return calculateOutputSignal(true, level, pos, state) > 0;
    }

    public static int sGetInputSignal(Level level, BlockPos pos, BlockState state, boolean analog) {
        if (!state.hasProperty(SIDES) || state.getValue(SIDES).acceptBack())
            return commonSignalGetter(level, pos, state, state.getValue(FACING), analog);
        return 0;
    }

    public static int sGetAlternateSignal(LevelReader level, BlockPos pos, BlockState state) {
        Pair<Integer, Integer> s = sGetAlternateSignals(level, pos, state);
        return Math.max(s.getFirst(), s.getSecond());
    }

    public static Pair<Integer, Integer> sGetAlternateSignals(LevelReader level, BlockPos pos, BlockState state) {
        if (state.hasProperty(FACING)) {
            Direction direction = state.getValue(FACING);
            Direction clock = direction.getClockWise();
            boolean acceptLeft = true;
            boolean acceptRight = true;
            if (state.hasProperty(SIDES)) {
                SideToggling sides = state.getValue(SIDES);
                acceptLeft = sides.acceptLeft();
                acceptRight = sides.acceptRight();
            }
            int left = acceptLeft ? getAlternateSignalAt(level, pos.relative(clock), clock) : 0;
            Direction counter = direction.getCounterClockWise();
            int right = acceptRight ? getAlternateSignalAt(level, pos.relative(counter), counter) : 0;
            return Pair.of(left, right);
        }
        return Pair.of(0, 0);
    }

    public static int sGetModeSignal(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseAnalogDiodeBlock) {
            SideToggling sides = state.getValue(SIDES);
            return sides.acceptBelow() ? level.getSignal(pos.relative(Direction.DOWN), Direction.UP) : 0;
        }
        return 0;
    }

    protected static int getDirectionalSignal(Level level, BlockPos pos, BlockState state, Direction dir) {
        BlockPos blockpos = pos.relative(dir);
        int i = level.getSignal(blockpos, dir);
        if (i >= 15) {
            return i;
        } else {
            BlockState blockstate = level.getBlockState(blockpos);
            return Math.max(i, blockstate.is(Blocks.REDSTONE_WIRE) ? blockstate.getValue(RedStoneWireBlock.POWER) : 0);
        }
    }

    protected static int getCrafterSignal(CrafterBlockEntity crafter) {
        int out = 15;
        for (int i = 0; i < 9; ++i) {
            if (crafter.isSlotDisabled(i))
                continue;
            ItemStack stack = crafter.getItem(i);
            if (out >= 9) {
                if (!stack.isEmpty())
                    out = Integer.min(out, 9 + (int)(((double)stack.getCount() / (double)stack.getMaxStackSize()) * 6));
                else
                    out = 8;
            } else if (stack.isEmpty())
                --out;
        }
        return out;
    }

    public static HashMap<Class<?>, BiFunction<Level, BlockPos, Integer>> ANALOG_OVERRIDES = new HashMap<>();
    static {
        ANALOG_OVERRIDES.put(CrafterBlock.class, (level, pos)->level.getBlockEntity(pos) instanceof CrafterBlockEntity crafter ? getCrafterSignal(crafter) : 0);
    }

    protected static int commonSignalGetter(Level level, BlockPos pos, BlockState state, Direction back, boolean analog) {
        int i = getDirectionalSignal(level, pos, state, back);
        BlockPos backPos = pos.relative(back);
        BlockState t_state = level.getBlockState(backPos);
        if (analog) {
            var override = ANALOG_OVERRIDES.get(level.getBlockState(backPos).getBlock().getClass());
            if (override != null)
                i = override.apply(level, backPos);
            else if (t_state.hasAnalogOutputSignal())
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
        }
        return Mth.clamp(i, 0, 15);
    }

    @Override
    protected int getInputSignal(Level level, BlockPos pos, BlockState state) {
        return commonSignalGetter(level, pos, state, state.getValue(FACING), true);
    }

    private static ItemFrame getItemFrame(Level level, Direction dir, BlockPos pos) {
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
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (player.getAbilities().mayBuild) {
            if (Arss.isWrench(itemStack)) {
                SideToggling newSidedness = cycleSideStates(blockState.getValue(SIDES), player.isShiftKeyDown());
                blockState = blockState.setValue(SIDES, newSidedness);
                level.setBlock(blockPos, blockState, 3);
                player.displayClientMessage(Component.translatable("display.arss.diode_block.sidedness." + newSidedness), true);
                refreshOutputState(level, blockPos, blockState, true);
                return ItemInteractionResult.SUCCESS;
            }
            if (modeProp == null && this instanceof EntityBlock && level.getBlockEntity(blockPos) instanceof IOnUseBlockListener useListener) {
                ItemInteractionResult out = useListener.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
                refreshOutputState(level, blockPos, blockState, tickingMode.ticking());
                return out;
            }
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.getAbilities().mayBuild) {
            if (modeProp != null) {
                state = player.isShiftKeyDown() ? cycleBack(state, modeProp) : state.cycle(modeProp);
                Enum<?> s = state.getValue(modeProp);
                level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, 0.5F + 0.05F * (float)s.ordinal());
                player.displayClientMessage(Component.translatable("display.arss." + name + ".mode." + s), true);
                level.setBlockAndUpdate(pos, state);
                refreshOutputState(level, pos, state, true);
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (this instanceof EntityBlock && level.getBlockEntity(pos) instanceof IOnUseBlockListener useListener) {
                InteractionResult out = useListener.useWithoutItem(state, level, pos, player, hit);
                refreshOutputState(level, pos, state, tickingMode.ticking());
                return out;
            }

        }
        return InteractionResult.PASS;
    }

    private static <T extends Enum<T> & StringRepresentable> BlockState setValueByClampedIndex(BlockState state, EnumProperty<T> prop, int index) {
        List<String> val = prop.getPossibleValues().stream().map(StringRepresentable::getSerializedName).toList();
        T f = prop.getValue(val.get(Mth.clamp(index, 0, val.size() - 1))).get();
        return state.setValue(prop, f);
    }

    @Override
    protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            if (modeProp != null) {
                int modeSwitch = sGetModeSignal(level, pos, state);
                if (modeSwitch > 0 && modeProp != null && state.getValue(modeProp).ordinal() != modeSwitch - 1) {
                    state = setValueByClampedIndex(state, modeProp, modeSwitch - 1);
                    level.setBlockAndUpdate(pos, state);
                }
            }
            int newPower = calculateOutputSignal(true, level, pos, state);
            if (newPower != state.getValue(POWER) || tickingMode.ticking())
                level.scheduleTick(pos, this, getDelay(state), shouldPrioritize(level, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL);
        }
    }

    static int setPower(Level level, BlockPos pos, BlockState state, int power) {
        if (level.getBlockEntity(pos) instanceof BaseAnalogDiodeBlockEntity be) {
            be.setOutput(Mth.clamp(power, 0, 15));
            return be.getOutput();
        }
        return 0;
    }

    protected static int getAlternateSignalAt(LevelReader level, BlockPos pos, Direction dir) {
        if (ALL_POWERS_ON_SIDES) {
            BlockPos start = pos.relative(dir.getOpposite());
            return commonSignalGetter((Level) level, start, level.getBlockState(start), dir, true);
        }
        return level.getControlInputSignal(pos.relative(dir), dir, false);
    }

    private void refreshOutputState(Level level, BlockPos pos, BlockState state, boolean recalculate) {
        int nextPower = 0;
        if (modeProp != null) {
            int modeSwitch = sGetModeSignal(level, pos, state);
            if (modeSwitch > 0 && modeProp != null && state.getValue(modeProp).ordinal() != modeSwitch - 1) {
                state = setValueByClampedIndex(state, modeProp, modeSwitch - 1);
                level.setBlockAndUpdate(pos, state);
                recalculate = true;
            }
        }
        if (recalculate) {
            nextPower = calculateOutputSignal(false, level, pos, state);
            if (level.getBlockEntity(pos) instanceof BaseAnalogDiodeBlockEntity be)
                be.setOutput(nextPower);
        } else if (level.getBlockEntity(pos) instanceof BaseAnalogDiodeBlockEntity be)
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

            if (tickingMode.recheck())
                level.scheduleTick(pos, this, getDelay(state));

            updateNeighborsInFront(level, pos, state);
        }
    }

    public static void updateNeighborsInFront(Block block, Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
//        if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(level, pos, level.getBlockState(pos), java.util.EnumSet.of(direction.getOpposite()), false).isCanceled())
//            return;
        level.neighborChanged(blockpos, block, pos);
        level.updateNeighborsAtExceptFromFacing(blockpos, block, direction);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {
        refreshOutputState(level, pos, state, tickingMode.ticking());
        if (tickingMode.ticking()) {
            int delay = getDelay(state);
            level.scheduleTick(pos, this, delay > 0 ? delay : 1);
        }
    }

//    @Override
//    public boolean getWeakChanges(BlockState state, net.minecraft.world.level.LevelReader world, BlockPos pos) {
//        return state.getBlock() instanceof BaseAnalogDiodeBlock;
//    }

//    @Override
//    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighbor, boolean b) {
//        if (pos.getY() == neighbor.getY() && level instanceof Level && !level.isClientSide())
//            state.handleNeighborChanged(level, pos, level.getBlockState(neighbor).getBlock(), neighbor, false); //FIXME!!! stack overflow (something must have changed in 1.21 or forge changed some behavior in 1.20)
//    }


    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack) {
        if (tickingMode.ticking() || shouldTurnOn(level, pos, state))
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
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel sl && player.isCreative() && level.getBlockEntity(pos) instanceof BaseAnalogDiodeBlockEntity diodeEntity)
            for (ItemStack stack : diodeEntity.getDrops(sl, pos, state, player)) {
                ItemEntity itementity = new ItemEntity(level, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, stack);
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof BaseAnalogDiodeBlockEntity diodeEntity) {
            List<ItemStack> out = diodeEntity.getDrops(builder.getLevel(), BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN)), state, player);
            if (!out.isEmpty())
                return out;
        }
        return super.getDrops(state, builder);
    }
}
