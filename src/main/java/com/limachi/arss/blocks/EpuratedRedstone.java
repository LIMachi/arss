package com.limachi.arss.blocks;

import com.google.common.collect.Sets;
import com.limachi.arss.Arss;
import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.arss.Registries;
import com.limachi.arss.Static;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

import static com.limachi.arss.Registries.BLOCK_REGISTER;
import static com.limachi.arss.Registries.ITEM_REGISTER;
import static net.minecraft.client.renderer.LevelRenderer.DIRECTIONS;

@SuppressWarnings("unused")
@Static
@ParametersAreNonnullByDefault
public class EpuratedRedstone extends RedStoneWireBlock {

    public static final int TOTAL_RANGE = 32;
    public static final int MAX_RANGE = TOTAL_RANGE - 1;

    public static final IntegerProperty RANGE = ArssBlockStateProperties.RANGE;

    public static final Properties PROPS = BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak();
    public static final RegistryObject<Block> R_BLOCK = BLOCK_REGISTER.register("epurated_redstone", EpuratedRedstone::new);
    public static final RegistryObject<Item> R_ITEM = ITEM_REGISTER.register("epurated_redstone", ()->new BlockItem(R_BLOCK.get(), new Item.Properties().tab(Arss.ITEM_GROUP)));

    static {
        Registries.setColor(R_BLOCK, EpuratedRedstone::getColor);
        Registries.setRenderLayer(R_BLOCK, RenderType.cutout());
    }

    private static final Vec3[] COLORS = Util.make(new Vec3[16], vec -> {
        for(int i = 0; i <= 15; ++i) {
            double f = (double)i / 15.;
            double r = f * 0.6 + (f > 0. ? 0.4 : 0.3);
            double g = Mth.clamp(f * f * 0.7 - 0.5, 0., 1.);
            double b = Mth.clamp(f * f * 0.6 - 0.7, 0., 1.);
            vec[i] = new Vec3(r, g, b);
        }

    });

    public static int getColor(BlockState state, BlockAndTintGetter getter, BlockPos pos, int index) {
        Vec3 vec3 = COLORS[state.getValue(POWER)];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    public EpuratedRedstone() {
        super(PROPS);
        registerDefaultState(stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, 0).setValue(RANGE, 0));
        crossState = defaultBlockState().setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE).setValue(RANGE, 0);
        for(BlockState state : getStateDefinition().getPossibleStates()) {
            if (state.getValue(POWER) == 0 && state.getValue(RANGE) != 0) {
                SHAPES_CACHE.remove(state);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RANGE);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPES_CACHE.get(state.setValue(POWER, 0).setValue(RANGE, 0));
    }

    @Override
    protected @NotNull BlockState getConnectionState(BlockGetter level, BlockState state, BlockPos pos) {
        boolean flag = isDot(state);
        state = getMissingConnections(level, defaultBlockState().setValue(POWER, state.getValue(POWER)).setValue(RANGE, state.getValue(RANGE)), pos);
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

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state1, @NotNull Direction dir, @NotNull BlockState state2, @NotNull LevelAccessor level, @NotNull BlockPos pos1, @NotNull BlockPos pos2) {
        if (dir == Direction.DOWN) {
            return state1;
        } else if (dir == Direction.UP) {
            return this.getConnectionState(level, state1, pos1);
        } else {
            RedstoneSide redstoneside = this.getConnectingSide(level, pos1, dir);
            return redstoneside.isConnected() == state1.getValue(PROPERTY_BY_DIRECTION.get(dir)).isConnected() && !isCross(state1) ? state1.setValue(PROPERTY_BY_DIRECTION.get(dir), redstoneside) : this.getConnectionState(level, this.crossState.setValue(POWER, state1.getValue(POWER)).setValue(RANGE, state1.getValue(RANGE)).setValue(PROPERTY_BY_DIRECTION.get(dir), redstoneside), pos1);
        }
    }

    @Override
    protected void updatePowerStrength(@NotNull Level level, @NotNull BlockPos pos, BlockState state) {
        Pair<Integer, Integer> i = calculateTargetRange(level, pos);
        if (!(state.getValue(RANGE).equals(i.getFirst()) && state.getValue(POWER).equals(i.getSecond()))) {
            if (level.getBlockState(pos) == state) {
                level.setBlock(pos, state.setValue(RANGE, i.getFirst()).setValue(POWER, i.getSecond()), 2);
            }

            Set<BlockPos> set = Sets.newHashSet();
            set.add(pos);

            for(Direction direction : Direction.values()) {
                set.add(pos.relative(direction));
            }

            for(BlockPos blockpos : set) {
                level.updateNeighborsAt(blockpos, this);
            }
        }
    }

    public int getRange(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        if (this.shouldSignal && dir != Direction.DOWN) {
            int i = state.getValue(RANGE);
            if (i == 0) {
                return 0;
            } else {
                return dir != Direction.UP && !this.getConnectionState(level, state, pos).getValue(PROPERTY_BY_DIRECTION.get(dir.getOpposite())).isConnected() ? 0 : i;
            }
        } else {
            return 0;
        }
    }

    protected Pair<Integer, Integer> getBestNeighborRange(Level level, BlockPos pos) {
        Pair<Integer, Integer> i = new Pair<>(0, 0);

        for(Direction direction : DIRECTIONS) {
            BlockPos tp = pos.relative(direction);
            int s = level.getSignal(tp, direction);
            Pair<Integer, Integer> j = new Pair<>(level.getBlockState(tp).is(this) ? getRange(level.getBlockState(tp), level, tp, direction) : s > 0 ? MAX_RANGE : 0, s);
            if (j.getFirst() >= MAX_RANGE) {
                return j;
            }

            if (j.getFirst() > i.getFirst()) {
                i = j;
            }
        }

        return i;
    }

    protected Pair<Integer, Integer> max(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
        return a.getFirst() >= b.getFirst() ? a : b;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        if (!shouldSignal) return 0;
        int s = super.getDirectSignal(state, level, pos, dir);
        return state.is(Blocks.REDSTONE_WIRE) ? s - 1 : s;
    }

    protected Pair<Integer, Integer> calculateTargetRange(Level level, BlockPos pos) {
        this.shouldSignal = false;
        ((RedStoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal = false;
        Pair<Integer, Integer> i = getBestNeighborRange(level, pos);
        ((RedStoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal = true;
        this.shouldSignal = true;
        Pair<Integer, Integer> j = new Pair<>(0, 0);
        if (i.getFirst() < MAX_RANGE) {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pos.relative(direction);
                BlockState blockstate = level.getBlockState(blockpos);
                j = max(j, getRangeAndPower(blockstate));
                BlockPos blockpos1 = pos.above();
                if (blockstate.isRedstoneConductor(level, blockpos) && !level.getBlockState(blockpos1).isRedstoneConductor(level, blockpos1)) {
                    j = max(j, getRangeAndPower(level.getBlockState(blockpos.above())));
                } else if (!blockstate.isRedstoneConductor(level, blockpos)) {
                    j = max(j, getRangeAndPower(level.getBlockState(blockpos.below())));
                }
            }
        }
        j = new Pair<>(j.getFirst() - 1, j.getSecond());
        return max(i, j);
    }

    protected Pair<Integer, Integer> getRangeAndPower(BlockState state) {
        int s = state.is(this) ? state.getValue(POWER) : state.is(Blocks.REDSTONE_WIRE) ? Math.max(state.getValue(POWER) - 1, 0) : 0;
        int r = state.is(this) ? state.getValue(RANGE) : state.is(Blocks.REDSTONE_WIRE) ? MAX_RANGE : 0;
        return new Pair<>(r, s);
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getAbilities().mayBuild) {
            if (isCross(state) || isDot(state)) {
                BlockState blockstate = isCross(state) ? this.defaultBlockState() : this.crossState;
                blockstate = blockstate.setValue(POWER, state.getValue(POWER)).setValue(RANGE, state.getValue(RANGE));
                blockstate = this.getConnectionState(level, blockstate, pos);
                if (blockstate != state) {
                    level.setBlock(pos, blockstate, 3);
                    this.updatesOnShapeChange(level, pos, state, blockstate);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
