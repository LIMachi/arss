package com.limachi.arss.mixin;

import com.limachi.arss.common.blocks.redstone_wires.NewRedstoneWire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;

import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin {
    @Shadow protected abstract boolean canSurviveOn(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState);

    //we need to inject at this point instead of shouldConnectTo because forge replaces shouldConnectTo with it's own method
    @Inject(method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;", at = @At("HEAD"), cancellable = true)
    public void getConnectingSideMixin(BlockGetter blockGetter, BlockPos blockPos, Direction direction, boolean bl, CallbackInfoReturnable<RedstoneSide> cir) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        if (bl) {
            boolean bl2 = blockState.getBlock() instanceof TrapDoorBlock || canSurviveOn(blockGetter, blockPos2, blockState);
            if (bl2 && blockGetter.getBlockState(blockPos2.above()).getBlock() instanceof NewRedstoneWire) {
                if (blockState.isFaceSturdy(blockGetter, blockPos2, direction.getOpposite()))
                    cir.setReturnValue(RedstoneSide.UP);
                else
                    cir.setReturnValue(RedstoneSide.SIDE);
                cir.cancel();
            }
        }
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
