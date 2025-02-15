package com.limachi.arss.mixin;

import com.limachi.arss.common.blocks.redstone_wires.BaseRedstoneWire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin {
    @Inject(method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;", at = @At("RETURN"), cancellable = true)
    public void getConnectingSideMixin(BlockGetter blockGetter, BlockPos blockPos, Direction direction, boolean diag, CallbackInfoReturnable<RedstoneSide> cir) {
        BlockPos neighborPos = blockPos.relative(direction);
        BlockState neighborState = blockGetter.getBlockState(neighborPos);

        if (neighborState.getBlock() instanceof BaseRedstoneWire)
            cir.setReturnValue(RedstoneSide.SIDE);
        else if (diag && blockGetter.getBlockState(neighborPos.above()).getBlock() instanceof BaseRedstoneWire)
            cir.setReturnValue(RedstoneSide.UP);
        else if (blockGetter.getBlockState(neighborPos.below()).getBlock() instanceof BaseRedstoneWire && !neighborState.isRedstoneConductor(blockGetter, neighborPos))
            cir.setReturnValue(RedstoneSide.SIDE);
    }


    @Redirect(method = "getWireSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    public boolean getWireSignalRedirectIs(BlockState instance, Block block) {
        return instance.getBlock() instanceof RedStoneWireBlock;
    }


    @Redirect(method = "checkCornerChangeAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    public boolean checkCornerChangeAtRedirectIs(BlockState instance, Block block) {
        return instance.getBlock() instanceof RedStoneWireBlock;
    }

    @Redirect(method = "updateIndirectNeighbourShapes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    public boolean updateIndirectNeighbourShapesRedirectIs(BlockState instance, Block block) {
        return instance.getBlock() instanceof RedStoneWireBlock;
    }
}
