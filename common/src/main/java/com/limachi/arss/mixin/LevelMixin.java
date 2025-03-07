package com.limachi.arss.mixin;

import com.limachi.arss.common.blocks.diodes.BaseAnalogDiodeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Unique final private Level mixin$self = (Level)(Object)this;

    @Inject(method = "updateNeighbourForOutputSignal", at = @At(value = "RETURN"))
    public void updateNeighbourForOutputSignalMixin(BlockPos blockPos, Block block, CallbackInfo ci) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos testedPos = blockPos.relative(dir);
            if (mixin$self.hasChunkAt(testedPos)) {
                BlockState tested = mixin$self.getBlockState(testedPos);
                if (tested.getBlock() instanceof BaseAnalogDiodeBlock)
                    mixin$self.neighborChanged(tested, testedPos, block, blockPos, false);
                else if (tested.isRedstoneConductor(mixin$self, testedPos)) {
                    testedPos = testedPos.relative(dir);
                    tested = mixin$self.getBlockState(testedPos);
                    if (tested.getBlock() instanceof BaseAnalogDiodeBlock)
                        mixin$self.neighborChanged(tested, testedPos, block, blockPos, false);
                }
            }
        }
    }
}
