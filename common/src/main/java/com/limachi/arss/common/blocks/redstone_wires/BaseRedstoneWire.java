package com.limachi.arss.common.blocks.redstone_wires;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.limachi.arss.vanillaInterface.IRedStoneWire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.limachi.arss.common.blocks.redstone_wires.RedstoneWireFactory.COLORS;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;
import static net.minecraft.world.level.block.state.properties.RedstoneSide.*;

public abstract class BaseRedstoneWire extends RedStoneWireBlock implements IRedStoneWire {

    @Override
    public <W extends RedStoneWireBlock> W self() { return (W)this; }

    public class PowerAndRange {
        public int power;
        public int range;
        public int fallOff;
        public int maxRange;

        public PowerAndRange(BlockState state) {
            if (state.getBlock() instanceof BaseRedstoneWire b) {
                if (shouldSignal())
                    power = state.getValue(POWER);
                else
                    power = 0;
                range = b.range(state);
                fallOff = b.rangeFallOff(state);
                maxRange = b.maxRange(state);
            } else {
                power = 0;
                range = 0;
                fallOff = 1;
                maxRange = 1;
            }
        }

        public PowerAndRange(int power, int range, int fallOff, int maxRange) {
            this.power = power;
            this.range = range;
            this.fallOff = fallOff;
            this.maxRange = maxRange;
        }

        public PowerAndRange(int power, int range, int fallOff) {
            this(power, range, fallOff, 1);
        }

        public PowerAndRange(int power, int range) {
            this(power, range, 1);
        }

        public PowerAndRange(int power) {
            this(power, 0);
        }

        public PowerAndRange() {
            this(0);
        }

        public PowerAndRange bestOf(PowerAndRange other) {
            if (power == other.power)
                return range < other.range ? this : other;
            return power > other.power ? this : other;
        }

        public PowerAndRange setPower(int power) {
            this.power = power;
            return this;
        }

        public PowerAndRange setRange(int range) {
            this.range = range;
            return this;
        }

        public PowerAndRange setFallOff(int fallOff) {
            this.fallOff = fallOff;
            return this;
        }

        public PowerAndRange decay() {
            if (--range <= 0) {
                range = maxRange;
                power -= fallOff;
                if (power < 0)
                    power = 0;
            }
            return this;
        }

        public boolean isBest() {
            return power == 15 && range >= maxRange;
        }
    }
    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH_REDSTONE, Direction.EAST, EAST_REDSTONE, Direction.SOUTH, SOUTH_REDSTONE, Direction.WEST, WEST_REDSTONE));
    protected static final VoxelShape SHAPE_DOT = Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);
    protected static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 13.0), Direction.SOUTH, Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 16.0), Direction.EAST, Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 13.0), Direction.WEST, Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 13.0)));
    protected static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0, 0.0, 0.0, 13.0, 16.0, 1.0)), Direction.SOUTH, Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0, 0.0, 15.0, 13.0, 16.0, 16.0)), Direction.EAST, Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0, 0.0, 3.0, 16.0, 16.0, 13.0)), Direction.WEST, Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0, 0.0, 3.0, 1.0, 16.0, 13.0))));
    protected static final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
    protected final BlockState crossState;

    protected int range(BlockState blockState) { return 0; }
    protected BlockState setRange(BlockState blockState, int range) { return blockState; }
    protected int maxRange(BlockState blockState) { return 1; }
    protected int rangeFallOff(BlockState blockState) { return 1; }

    public BaseRedstoneWire(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH_REDSTONE, NONE).setValue(EAST_REDSTONE, NONE).setValue(SOUTH_REDSTONE, NONE).setValue(WEST_REDSTONE, NONE).setValue(POWER, 0));
        crossState = defaultBlockState().setValue(NORTH_REDSTONE, SIDE).setValue(EAST_REDSTONE, SIDE).setValue(SOUTH_REDSTONE, SIDE).setValue(WEST_REDSTONE, SIDE);
        for (var state : getStateDefinition().getPossibleStates())
            if (state.getValue(POWER) == 0)
                SHAPES_CACHE.put(state, calculateShape(state));
    }

    protected VoxelShape calculateShape(BlockState blockState) {
        VoxelShape shape = SHAPE_DOT;

        for (Direction direction : Direction.Plane.HORIZONTAL)
            switch (blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                case SIDE -> shape = Shapes.or(shape, SHAPES_FLOOR.get(direction));
                case UP -> shape = Shapes.or(shape, SHAPES_UP.get(direction));
            }

        return shape;
    }

    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES_CACHE.get(blockState.setValue(POWER, 0));
    }

    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return getConnectionState(blockPlaceContext.getLevel(), crossState, blockPlaceContext.getClickedPos());
    }

    protected BlockState getConnectionState(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
        boolean wasDot = isDot(blockState);
        blockState = getMissingConnections(blockGetter, defaultBlockState().setValue(POWER, blockState.getValue(POWER)), blockPos);
        if (!wasDot || !isDot(blockState)) {
            boolean northConnected = blockState.getValue(NORTH_REDSTONE).isConnected();
            boolean southConnected = blockState.getValue(SOUTH_REDSTONE).isConnected();
            boolean eastConnected = blockState.getValue(EAST_REDSTONE).isConnected();
            boolean westConnected = blockState.getValue(WEST_REDSTONE).isConnected();
            if (!northConnected && !southConnected) {
                if (!westConnected)
                    blockState = blockState.setValue(WEST_REDSTONE, RedstoneSide.SIDE);
                if (!eastConnected)
                    blockState = blockState.setValue(EAST_REDSTONE, RedstoneSide.SIDE);
            }
            if (!eastConnected && !westConnected) {
                if (!northConnected)
                    blockState = blockState.setValue(NORTH_REDSTONE, RedstoneSide.SIDE);
                if (!southConnected)
                    blockState = blockState.setValue(SOUTH_REDSTONE, RedstoneSide.SIDE);
            }
        }
        return blockState;
    }

    protected BlockState getMissingConnections(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
        boolean bl = !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos);

        for (Direction direction : Direction.Plane.HORIZONTAL)
            if (!blockState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected())
                blockState = blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), getConnectingSide(blockGetter, blockPos, direction, bl));

        return blockState;
    }

    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return switch (direction) {
            case DOWN -> !canSurviveOn(levelAccessor, blockPos2, blockState2) ? Blocks.AIR.defaultBlockState() : blockState;
            case UP -> getConnectionState(levelAccessor, blockState, blockPos);
            default -> {
                RedstoneSide side = getConnectingSide(levelAccessor, blockPos, direction);
                if (side.isConnected() == blockState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && !isCross(blockState))
                    yield blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), side);
                else
                    yield getConnectionState(levelAccessor, crossState.setValue(POWER, blockState.getValue(POWER)).setValue(PROPERTY_BY_DIRECTION.get(direction), side), blockPos);
            }
        };
    }

    protected static boolean isCross(BlockState blockState) {
        return blockState.getValue(NORTH_REDSTONE).isConnected() && blockState.getValue(SOUTH_REDSTONE).isConnected() && blockState.getValue(EAST_REDSTONE).isConnected() && blockState.getValue(WEST_REDSTONE).isConnected();
    }

    protected static boolean isDot(BlockState blockState) {
        return !blockState.getValue(NORTH_REDSTONE).isConnected() && !blockState.getValue(SOUTH_REDSTONE).isConnected() && !blockState.getValue(EAST_REDSTONE).isConnected() && !blockState.getValue(WEST_REDSTONE).isConnected();
    }

    //TODO: check in detail the workings of this function to fix the shapes not updating properly for this or the vanilla redstone wire
    protected void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.Plane.HORIZONTAL)
            if (blockState.getValue(PROPERTY_BY_DIRECTION.get(direction)) != RedstoneSide.NONE && !levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, direction)).is(this)) {
                mutableBlockPos.move(Direction.DOWN);
                BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
                if (blockState2.is(this)) {
                    BlockPos blockPos2 = mutableBlockPos.relative(direction.getOpposite());
                    levelAccessor.neighborShapeChanged(direction.getOpposite(), levelAccessor.getBlockState(blockPos2), mutableBlockPos, blockPos2, i, j);
                }

                mutableBlockPos.setWithOffset(blockPos, direction).move(Direction.UP);
                BlockState blockState3 = levelAccessor.getBlockState(mutableBlockPos);
                if (blockState3.is(this)) {
                    BlockPos blockPos3 = mutableBlockPos.relative(direction.getOpposite());
                    levelAccessor.neighborShapeChanged(direction.getOpposite(), levelAccessor.getBlockState(blockPos3), mutableBlockPos, blockPos3, i, j);
                }
            }
    }

    protected RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return getConnectingSide(blockGetter, blockPos, direction, !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos));
    }

    protected RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction, boolean bl) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        if (bl) {
            boolean bl2 = blockState.getBlock() instanceof TrapDoorBlock || canSurviveOn(blockGetter, blockPos2, blockState);
            if (bl2 && shouldConnectTo(blockGetter.getBlockState(blockPos2.above()))) {
                if (blockState.isFaceSturdy(blockGetter, blockPos2, direction.getOpposite()))
                    return RedstoneSide.UP;
                return RedstoneSide.SIDE;
            }
        }

        return !shouldConnectTo(blockState, direction) && (blockState.isRedstoneConductor(blockGetter, blockPos2) || !shouldConnectTo(blockGetter.getBlockState(blockPos2.below()))) ? RedstoneSide.NONE : RedstoneSide.SIDE;
    }

    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        return canSurviveOn(levelReader, blockPos2, blockState2);
    }

    protected boolean canSurviveOn(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP) || blockState.is(Blocks.HOPPER);
    }

    //TODO: implement the configurable power strength (power is still between 0-15, but range should be used there)
    protected void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState) {
        PowerAndRange p = calculateTargetStrength(level, blockPos);
        if (blockState.getValue(POWER) != p.power || range(blockState) != p.range) {
            if (level.getBlockState(blockPos) == blockState)
                level.setBlock(blockPos, setRange(blockState.setValue(POWER, p.power), p.range), 2);

            //TODO: wtf is this set usage? is this the fix to make redstone "locally random" instead of "sided"?
            Set<BlockPos> set = Sets.newHashSet();
            set.add(blockPos);
            Direction[] var6 = Direction.values();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                Direction direction = var6[var8];
                set.add(blockPos.relative(direction));
            }

            Iterator var10 = set.iterator();

            while(var10.hasNext()) {
                BlockPos blockPos2 = (BlockPos)var10.next();
                level.updateNeighborsAt(blockPos2, this);
            }
        }
    }

    protected PowerAndRange getSignalAndRange(SignalGetter getter, BlockPos blockPos, Direction direction) {
        BlockState blockState = getter.getBlockState(blockPos);
        int power = blockState.getSignal(getter, blockPos, direction);
        if (blockState.getBlock() instanceof BaseRedstoneWire)
            return new PowerAndRange(blockState);
        return new PowerAndRange(blockState.isRedstoneConductor(getter, blockPos) ? Math.max(power, getter.getDirectSignalTo(blockPos)) : power);
    }
    //best is calculated by getting the best power, and in case of equal power, the best range
    protected PowerAndRange getBestDecayedSignal(Level level, BlockPos blockPos) {
        PowerAndRange best = new PowerAndRange();
        for (Direction direction : Direction.values())
            best = best.bestOf(getSignalAndRange(level, blockPos.relative(direction), direction));
        return best;
    }

    //FIXME/TODO: our wire should carfully use this when interacting with vanilla wires
    protected PowerAndRange calculateTargetStrength(Level level, BlockPos blockPos) {
        setShouldSignal(false);
        PowerAndRange best = getBestDecayedSignal(level, blockPos);
        setShouldSignal(true);

        if (best.isBest())
            return best;

        PowerAndRange test = new PowerAndRange();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockState blockState = level.getBlockState(blockPos2);
            test = test.bestOf(getWireSignal(blockState));
            BlockPos blockPos3 = blockPos.above();
            if (blockState.isRedstoneConductor(level, blockPos2) && !level.getBlockState(blockPos3).isRedstoneConductor(level, blockPos3)) {
                test = test.bestOf(getWireSignal(level.getBlockState(blockPos2.above())));
            } else if (!blockState.isRedstoneConductor(level, blockPos2)) {
                test = test.bestOf(getWireSignal(level.getBlockState(blockPos2.below())));
            }
        }
        return best.bestOf(test.decay());
    }

    //TODO: check if this check against "this" should be replaced for a check against any wire kind
    protected PowerAndRange getWireSignal(BlockState blockState) {
        return new PowerAndRange(blockState);
    }

    protected void checkCornerChangeAt(Level level, BlockPos blockPos) {
        if (level.getBlockState(blockPos).is(this)) {
            level.updateNeighborsAt(blockPos, this);
            for (Direction direction : Direction.values())
                level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState2.is(blockState.getBlock()) && !level.isClientSide) {
            updatePowerStrength(level, blockPos, blockState);

            for (Direction direction : Direction.Plane.VERTICAL)
                level.updateNeighborsAt(blockPos.relative(direction), this);

            updateNeighborsOfNeighboringWires(level, blockPos);
        }
    }

    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!bl && !blockState.is(blockState2.getBlock())) {
            super.onRemove(blockState, level, blockPos, blockState2, bl);
            if (!level.isClientSide) {
                for (Direction direction : Direction.values())
                    level.updateNeighborsAt(blockPos.relative(direction), this);

                updatePowerStrength(level, blockPos, blockState);
                updateNeighborsOfNeighboringWires(level, blockPos);
            }
        }
    }

    protected void updateNeighborsOfNeighboringWires(Level level, BlockPos blockPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL)
            checkCornerChangeAt(level, blockPos.relative(direction));

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2))
                checkCornerChangeAt(level, blockPos2.above());
            else
                checkCornerChangeAt(level, blockPos2.below());
        }
    }

    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (!level.isClientSide)
            if (blockState.canSurvive(level, blockPos))
                updatePowerStrength(level, blockPos, blockState);
            else {
                dropResources(blockState, level, blockPos);
                level.removeBlock(blockPos, false);
            }
    }

    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return !shouldSignal() ? 0 : blockState.getSignal(blockGetter, blockPos, direction);
    }

    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (shouldSignal() && direction != Direction.DOWN) {
            int i = blockState.getValue(POWER);
            if (range(blockState) > 0)
                i -= 1;
            if (i <= 0)
                return 0;
            else
                return direction != Direction.UP && !getConnectionState(blockGetter, blockState, blockPos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite())).isConnected() ? 0 : i;
        } else
            return 0;
    }

    protected static boolean shouldConnectTo(BlockState blockState) {
        return shouldConnectTo(blockState, null);
    }

    //FIXME: we just found one of the many culprits of redstone wire not connecting to some things as expected
    protected static boolean shouldConnectTo(BlockState blockState, Direction direction) {
        if (blockState.is(Blocks.REDSTONE_WIRE))
            return true;
        else if (blockState.is(Blocks.REPEATER)) {
            Direction direction2 = blockState.getValue(RepeaterBlock.FACING);
            return direction2 == direction || direction2.getOpposite() == direction;
        }
        else if (blockState.is(Blocks.OBSERVER))
            return direction == blockState.getValue(ObserverBlock.FACING);
        else
            return blockState.isSignalSource() && direction != null;
    }

    protected boolean isSignalSource(BlockState blockState) {
        return shouldSignal();
    }

    protected void spawnParticlesAlongLine(Level level, RandomSource randomSource, BlockPos blockPos, Vec3 vec3, Direction direction, Direction direction2, float f, float g) {
        float h = g - f;
        if (!(randomSource.nextFloat() >= 0.2F * h)) {
            float j = f + h * randomSource.nextFloat();
            double d = 0.5 + (double)(0.4375F * (float)direction.getStepX()) + (double)(j * (float)direction2.getStepX());
            double e = 0.5 + (double)(0.4375F * (float)direction.getStepY()) + (double)(j * (float)direction2.getStepY());
            double k = 0.5 + (double)(0.4375F * (float)direction.getStepZ()) + (double)(j * (float)direction2.getStepZ());
            level.addParticle(new DustParticleOptions(vec3.toVector3f(), 1.0F), (double)blockPos.getX() + d, (double)blockPos.getY() + e, (double)blockPos.getZ() + k, 0.0, 0.0, 0.0);
        }
    }

    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        int i = blockState.getValue(POWER);
        if (i != 0) {
            for (Direction direction : Direction.Plane.HORIZONTAL)
                switch (blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                    case UP:
                        spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[i], direction, Direction.UP, -0.5F, 0.5F);
                    case SIDE:
                        spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.5F);
                        break;
                    case NONE:
                    default:
                        spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.3F);
                }
        }
    }

    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> blockState.setValue(NORTH_REDSTONE, blockState.getValue(SOUTH_REDSTONE)).setValue(EAST_REDSTONE, blockState.getValue(WEST_REDSTONE)).setValue(SOUTH_REDSTONE, blockState.getValue(NORTH_REDSTONE)).setValue(WEST_REDSTONE, blockState.getValue(EAST_REDSTONE));
            case COUNTERCLOCKWISE_90 -> blockState.setValue(NORTH_REDSTONE, blockState.getValue(EAST_REDSTONE)).setValue(EAST_REDSTONE, blockState.getValue(SOUTH_REDSTONE)).setValue(SOUTH_REDSTONE, blockState.getValue(WEST_REDSTONE)).setValue(WEST_REDSTONE, blockState.getValue(NORTH_REDSTONE));
            case CLOCKWISE_90 -> blockState.setValue(NORTH_REDSTONE, blockState.getValue(WEST_REDSTONE)).setValue(EAST_REDSTONE, blockState.getValue(NORTH_REDSTONE)).setValue(SOUTH_REDSTONE, blockState.getValue(EAST_REDSTONE)).setValue(WEST_REDSTONE, blockState.getValue(SOUTH_REDSTONE));
            default -> blockState;
        };
    }

    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> blockState.setValue(NORTH_REDSTONE, blockState.getValue(SOUTH_REDSTONE)).setValue(SOUTH_REDSTONE, blockState.getValue(NORTH_REDSTONE));
            case FRONT_BACK -> blockState.setValue(EAST_REDSTONE, blockState.getValue(WEST_REDSTONE)).setValue(WEST_REDSTONE, blockState.getValue(EAST_REDSTONE));
            default -> super.mirror(blockState, mirror);
        };
    }

    //FIXME: should add RANGE property
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH_REDSTONE, EAST_REDSTONE, SOUTH_REDSTONE, WEST_REDSTONE, POWER);
    }

    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!player.getAbilities().mayBuild)
            return InteractionResult.PASS;
        else {
            if (isCross(blockState) || isDot(blockState)) {
                BlockState blockState2 = isCross(blockState) ? defaultBlockState() : crossState;
                blockState2 = blockState2.setValue(POWER, blockState.getValue(POWER));
                blockState2 = getConnectionState(level, blockState2, blockPos);
                if (blockState2 != blockState) {
                    level.setBlock(blockPos, blockState2, 3);
                    updatesOnShapeChange(level, blockPos, blockState, blockState2);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }
    }

    protected void updatesOnShapeChange(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (blockState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() != blockState2.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2))
                level.updateNeighborsAtExceptFromFacing(blockPos2, blockState2.getBlock(), direction.getOpposite());
        }
    }
}