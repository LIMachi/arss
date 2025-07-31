package com.limachi.arss.common.blocks.redstone_wires;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * works like vanilla/modded redstone but instead of being in dust form and only going up slopes,
 * this redstone can go in any cardinal direction including up and down (but not diagonal)
 * each of the 6 faces can be toggled on/off by using a (analog) redstone torch
 */
public class BaseFramedRedstoneWire extends Block {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = new EnumMap<>(ImmutableMap.of(Direction.NORTH, NORTH, Direction.SOUTH, SOUTH, Direction.EAST, EAST, Direction.WEST, WEST, Direction.DOWN, DOWN, Direction.UP, UP));

    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();

    protected IntegerProperty range;
    protected int maxRange;
    protected int rangeFalloff;

    protected final RedStoneWireBlock vanilla = (RedStoneWireBlock) Blocks.REDSTONE_WIRE;

    protected BaseFramedRedstoneWire(Properties bProps, IntegerProperty fRange, int fMaxRange, int fRangeFalloff) {
        super(bProps);
        range = fRange;
        maxRange = fMaxRange;
        rangeFalloff = fRangeFalloff;
        var defaultState = stateDefinition.any();
        defaultState.setValue(NORTH, false).setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false).setValue(POWER, 0);
        if (!vanilla())
            defaultState.setValue(range, 0);
        registerDefaultState(defaultState);
        for (var state : getStateDefinition().getPossibleStates())
            if (state.getValue(POWER) == 0 && getRange(state) == 0)
                SHAPES_CACHE.put(state, calculateShape(state));
    }

    private VoxelShape calculateShape(BlockState blockState) {
        VoxelShape voxelShape = /*SHAPE_DOT*/Block.box(0., 0., 0., 16., 16., 16.);

//        for(Direction direction : Direction.Plane.HORIZONTAL) {
//            RedstoneSide redstoneSide = (RedstoneSide)blockState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction));
//            if (redstoneSide == RedstoneSide.SIDE) {
//                voxelShape = Shapes.or(voxelShape, (VoxelShape)SHAPES_FLOOR.get(direction));
//            } else if (redstoneSide == RedstoneSide.UP) {
//                voxelShape = Shapes.or(voxelShape, (VoxelShape)SHAPES_UP.get(direction));
//            }
//        }

        return voxelShape;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER, NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES_CACHE.get(setRange(state.setValue(POWER, 0), 0));
    }
}
