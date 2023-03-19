package com.limachi.arss.blocks.redstone_wires;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class BaseRedstoneWire extends RedStoneWireBlock {

    protected IntegerProperty range;
    protected int maxRange;
    protected int rangeFalloff;

    protected BaseRedstoneWire(BlockBehaviour.Properties bProps, IntegerProperty fRange, int fMaxRange, int fRangeFalloff) {
        super(bProps);
        range = fRange;
        maxRange = fMaxRange;
        rangeFalloff = fRangeFalloff;
        registerDefaultState(stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, 0).setValue(fRange, 0));
        crossState = defaultBlockState().setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE).setValue(fRange, 0);
        for(BlockState state : getStateDefinition().getPossibleStates()) {
            if (state.getValue(POWER) == 0 && state.getValue(fRange) != 0) {
                SHAPES_CACHE.remove(state);
            }
        }
    }

    @Override
    public @Nonnull VoxelShape getShape(BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPES_CACHE.get(state.setValue(POWER, 0).setValue(range, 0));
    }

    @Override
    protected @Nonnull BlockState getMissingConnections(BlockGetter level, BlockState state, BlockPos pos) {
        boolean flag = !level.getBlockState(pos.above()).isRedstoneConductor(level, pos);

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (!state.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected()) {
                RedstoneSide redstoneside = this.getConnectingSide(level, pos, direction, flag);
                state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside);
            }
        }

        return state;
    }

    @Override
    protected @Nonnull BlockState getConnectionState(BlockGetter level, BlockState state, BlockPos pos) {
        boolean flag = isDot(state);
        state = getMissingConnections(level, defaultBlockState().setValue(POWER, state.getValue(POWER)).setValue(range, state.getValue(range)), pos);
        if (!flag || !isDot(state)) {
            boolean flag1 = state.getValue(NORTH).isConnected();
            boolean flag2 = state.getValue(SOUTH).isConnected();
            boolean flag3 = state.getValue(EAST).isConnected();
            boolean flag4 = state.getValue(WEST).isConnected();
            boolean flag5 = !flag1 && !flag2;
            boolean flag6 = !flag3 && !flag4;
            if (!flag4 && flag5) {
                state = state.setValue(WEST, RedstoneSide.SIDE);
            }

            if (!flag3 && flag5) {
                state = state.setValue(EAST, RedstoneSide.SIDE);
            }

            if (!flag1 && flag6) {
                state = state.setValue(NORTH, RedstoneSide.SIDE);
            }

            if (!flag2 && flag6) {
                state = state.setValue(SOUTH, RedstoneSide.SIDE);
            }

        }
        return state;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return false;
    }

    protected boolean canRedstoneConnectTo(BlockGetter level, BlockPos pos, @Nullable Direction dir) {
        BlockState state = level.getBlockState(pos);
        return !state.getBlock().equals(Blocks.REDSTONE_WIRE) && (state.is(this) || state.getBlock().canConnectRedstone(state, level, pos, dir));
    }

    private RedstoneSide getConnectingSide(BlockGetter level, BlockPos pos, Direction dir, boolean topOpen) {
        BlockPos blockpos = pos.relative(dir);
        BlockState blockstate = level.getBlockState(blockpos);
        if (topOpen) {
            boolean flag = canSurviveOn(level, blockpos, blockstate);
            if (flag && canRedstoneConnectTo(level, blockpos.above(), null)) {
                if (blockstate.isFaceSturdy(level, blockpos, dir.getOpposite())) {
                    return RedstoneSide.UP;
                }

                return RedstoneSide.SIDE;
            }
        }

        if (canRedstoneConnectTo(level, blockpos, dir)) {
            return RedstoneSide.SIDE;
        } else if (blockstate.isRedstoneConductor(level, blockpos)) {
            return RedstoneSide.NONE;
        } else {
            BlockPos blockPosBelow = blockpos.below();
            return canRedstoneConnectTo(level, blockPosBelow, null) ? RedstoneSide.SIDE : RedstoneSide.NONE;
        }
    }

    @Override
    protected @Nonnull RedstoneSide getConnectingSide(BlockGetter level, BlockPos pos, Direction dir) {
        return this.getConnectingSide(level, pos, dir, !level.getBlockState(pos.above()).isRedstoneConductor(level, pos));
    }

    @Override
    public @Nonnull BlockState updateShape(@Nonnull BlockState state1, @Nonnull Direction dir, @Nonnull BlockState state2, @Nonnull LevelAccessor level, @Nonnull BlockPos pos1, @Nonnull BlockPos pos2) {
        if (dir == Direction.DOWN) {
            return state1;
        } else if (dir == Direction.UP) {
            return getConnectionState(level, state1, pos1);
        } else {
            RedstoneSide redstoneside = getConnectingSide(level, pos1, dir);
            return redstoneside.isConnected() == state1.getValue(PROPERTY_BY_DIRECTION.get(dir)).isConnected() && !isCross(state1) ? state1.setValue(PROPERTY_BY_DIRECTION.get(dir), redstoneside) : getConnectionState(level, crossState.setValue(POWER, state1.getValue(POWER)).setValue(range, state1.getValue(range)).setValue(PROPERTY_BY_DIRECTION.get(dir), redstoneside), pos1);
        }
    }

    @Override
    protected void updatePowerStrength(@Nonnull Level level, @Nonnull BlockPos pos, BlockState state) {
        RedstoneWireFactory.PR i = calculate(level, pos);
        if (!(state.getValue(range).equals(i.range) && state.getValue(POWER).equals(i.power))) {
            if (level.getBlockState(pos) == state)
                level.setBlock(pos, state.setValue(range, i.range).setValue(POWER, i.power), 2);
            level.updateNeighborsAt(pos, this);
            for(Direction direction : Direction.values())
                level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    private RedstoneWireFactory.PR calculate(Level level, BlockPos pos) {
        ((RedStoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal = false;
        int source = level.getBestNeighborSignal(pos);
        ((RedStoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal = true;
        if (source == 15) return new RedstoneWireFactory.PR(15, maxRange);
        RedstoneWireFactory.PR j = new RedstoneWireFactory.PR();
        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos relative = pos.relative(direction);
            BlockState state = level.getBlockState(relative);
            j = j.max(getWireSignal(state));
            BlockPos above = relative.above();
            if (state.isRedstoneConductor(level, relative) && !level.getBlockState(above).isRedstoneConductor(level, above))
                j = j.max(getWireSignal(level.getBlockState(relative.above())));
            else if (!state.isRedstoneConductor(level, relative))
                j = j.max(getWireSignal(level.getBlockState(relative.below())));
        }
        return new RedstoneWireFactory.PR(source, source > 0 ? maxRange : 0).max(j);
    }

    private RedstoneWireFactory.PR getWireSignal(BlockState state) {
        if (!state.is(this)) { return new RedstoneWireFactory.PR(); }
        int p = state.getValue(POWER);
        int r = state.getValue(range) - 1;
        if (r <= 0) {
            p = Math.max(0, p - rangeFalloff);
            r = p > 0 ? maxRange : 0;
        }
        return new RedstoneWireFactory.PR(p, r);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        if (!((RedStoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal) return 0;
        int s = super.getDirectSignal(state, level, pos, dir);
        return state.is(Blocks.REDSTONE_WIRE) ? s - 1 : s;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        if (((RedStoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal && dir != Direction.DOWN) {
            int i = state.getValue(POWER);
            if (i == 0) {
                return 0;
            } else {
                return dir != Direction.UP && !this.getConnectionState(level, state, pos).getValue(PROPERTY_BY_DIRECTION.get(dir.getOpposite())).isConnected() ? 0 : i;
            }
        } else {
            return 0;
        }
    }

    @Override
    public @Nonnull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getAbilities().mayBuild) {
            if (isCross(state) || isDot(state)) {
                BlockState blockstate = isCross(state) ? defaultBlockState() : crossState;
                blockstate = blockstate.setValue(POWER, state.getValue(POWER)).setValue(range, state.getValue(range));
                blockstate = getConnectionState(level, blockstate, pos);
                if (blockstate != state) {
                    level.setBlock(pos, blockstate, 3);
                    updatesOnShapeChange(level, pos, state, blockstate);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
