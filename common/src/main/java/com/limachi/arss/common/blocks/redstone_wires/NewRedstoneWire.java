package com.limachi.arss.common.blocks.redstone_wires;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NewRedstoneWire extends RedStoneWireBlock {

    protected IntegerProperty range;
    protected int maxRange;
    protected int rangeFalloff;

    protected final RedStoneWireBlock vanilla = (RedStoneWireBlock)Blocks.REDSTONE_WIRE;

    protected NewRedstoneWire(BlockBehaviour.Properties bProps, IntegerProperty fRange, int fMaxRange, int fRangeFalloff) {
        super(bProps);
        range = fRange;
        maxRange = fMaxRange;
        rangeFalloff = fRangeFalloff;
        registerDefaultState(stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, 0).setValue(fRange, 0));
        for (BlockState state : getStateDefinition().getPossibleStates())
            if (state.getValue(POWER) == 0 && state.getValue(fRange) != 0)
                SHAPES_CACHE.remove(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES_CACHE.get(state.setValue(POWER, 0).setValue(range, 0));
    }

    @Override
    protected void updatePowerStrength(Level level, BlockPos pos, BlockState state) {
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
        vanilla.shouldSignal = false;
        int source = level.getBestNeighborSignal(pos);
        vanilla.shouldSignal = true;
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
        if (!(state.getBlock() instanceof RedStoneWireBlock)) { return new RedstoneWireFactory.PR(); }
        int p = state.getValue(POWER);
        int r = maxRange;
        if (state.getBlock() == this) {
            r = state.getValue(range) - 1;
            if (r <= 0) {
                p = Math.max(0, p - rangeFalloff);
                r = p > 0 ? maxRange : 0;
            }
        } else
            --p;
        return new RedstoneWireFactory.PR(p, r);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        if (!vanilla.shouldSignal) return 0;
        int s = super.getDirectSignal(state, level, pos, dir);
        return state.is(Blocks.REDSTONE_WIRE) ? s - 1 : s;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        if (vanilla.shouldSignal && dir != Direction.DOWN) {
            int i = state.getValue(POWER);
            if (i == 0) {
                return 0;
            } else {
                return dir != Direction.UP && getConnectionState(level, state, pos).getValue(PROPERTY_BY_DIRECTION.get(dir.getOpposite())).isConnected() ? i : 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return vanilla.shouldSignal;
    }
}
