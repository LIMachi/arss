package com.limachi.arss.vanillaInterface;

import com.limachi.arss.mixin.RedStoneWireBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public interface IRedStoneWire {
    <W extends RedStoneWireBlock> W self();
    default RedStoneWireBlockAccessor mixin() { return self(); }
    default Map<BlockState, VoxelShape> shapesCache() { return mixin().getShapesCache(); }

    default boolean shouldSignal() { return mixin().getShouldSignal(); }

    default void setShouldSignal(boolean value) { mixin().setShouldSignal(value); }

    default BlockState invGetConnectionState(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
        return mixin().invGetConnectionState(blockGetter, blockState, blockPos);
    }

    default RedstoneSide invGetConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return mixin().invGetConnectingSide(blockGetter, blockPos, direction);
    }
}
