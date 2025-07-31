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

public class BaseRedstoneWire extends RedStoneWireBlock {
    protected IntegerProperty range;
    protected int maxRange;
    protected int rangeFalloff;

    protected final RedStoneWireBlock vanilla = (RedStoneWireBlock)Blocks.REDSTONE_WIRE;

    protected BaseRedstoneWire() {
        super(Properties.ofFullCopy(Blocks.REDSTONE_WIRE));
    }

    protected BaseRedstoneWire(BlockBehaviour.Properties bProps, IntegerProperty fRange, int fMaxRange, int fRangeFalloff) {
        super(bProps);
        range = fRange;
        maxRange = fMaxRange;
        rangeFalloff = fRangeFalloff;
        if (!vanilla()) {
            registerDefaultState(stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, 0).setValue(fRange, 0));
            for (BlockState state : getStateDefinition().getPossibleStates())
                if (state.getValue(POWER) == 0 && state.getValue(fRange) != 0)
                    SHAPES_CACHE.remove(state);
        }
    }

    public boolean vanilla() {
        return (maxRange == 1 && rangeFalloff == 1) || range == null;
    }

    public int getRange(BlockState state) {
        if (vanilla())
            return 0;
        return state.getValue(range);
    }

    public BlockState setRange(BlockState state, int value) {
        if (vanilla())
            return state;
        return state.setValue(range, value);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES_CACHE.get(setRange(state.setValue(POWER, 0), 0));
    }

    @Override
    protected void updatePowerStrength(Level level, BlockPos pos, BlockState state) {
        RedstoneWireFactory.PR i = calculate(level, pos);
        if (!(getRange(state) == i.range && state.getValue(POWER).equals(i.power))) {
            if (level.getBlockState(pos) == state)
                level.setBlock(pos, setRange(state.setValue(POWER, i.power), i.range), 2);
            level.updateNeighborsAt(pos, this);
            for(Direction direction : Direction.values())
                level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    private RedstoneWireFactory.PR calculate(Level level, BlockPos pos) {
        vanilla.shouldSignal = false;
        int source = level.getBestNeighborSignal(pos);
        vanilla.shouldSignal = true;
        if (source == 15) return new RedstoneWireFactory.PR(15, maxRange - 1);
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
        return new RedstoneWireFactory.PR(source, source > 0 ? maxRange - 1 : 0).max(j);
    }

    private RedstoneWireFactory.PR getWireSignal(BlockState state) {
        if (!(state.getBlock() instanceof RedStoneWireBlock)) { return new RedstoneWireFactory.PR(); }
        int p = state.getValue(POWER);
        int r = maxRange - 1;
        if (state.getBlock() == this) {
            r = getRange(state) - 1;
            if (r < 0) {
                p = Math.max(0, p - rangeFalloff);
                r = p > 0 ? maxRange - 1 : 0;
            }
        } else
            --p;
        return new RedstoneWireFactory.PR(p, r);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        if (!vanilla.shouldSignal) return 0;
        int s = super.getDirectSignal(state, level, pos, dir);
        return state.is(Blocks.REDSTONE_WIRE) || (state.getBlock() instanceof BaseRedstoneWire w && w.vanilla()) ? s - 1 : s;
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
