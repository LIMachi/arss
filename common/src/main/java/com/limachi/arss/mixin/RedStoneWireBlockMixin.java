package com.limachi.arss.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(RedStoneWireBlock.class)
public interface RedStoneWireBlockMixin {

    @Accessor("SHAPES_CACHE")
    Map<BlockState, VoxelShape> getShapesCache();

    @Accessor("shouldSignal")
    boolean getShouldSignal();

    @Accessor("shouldSignal")
    void setShouldSignal(boolean value);

    @Invoker("getConnectionState")
    BlockState invGetConnectionState(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos);

    @Invoker("getConnectingSide")
    RedstoneSide invGetConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction);
}
