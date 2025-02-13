package com.limachi.arss.mixin;

import com.limachi.arss.common.blocks.redstone_wires.NewRedstoneWire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin implements RedStoneWireBlockAccessor {
    @Inject(method = "checkCornerChangeAt", at = @At("HEAD"), cancellable = true)
    public void checkCornerChangeAtMixin(Level level, BlockPos blockPos, CallbackInfo ci) {
        if (level.getBlockState(blockPos).getBlock() instanceof NewRedstoneWire) {
            level.updateNeighborsAt(blockPos, (Block)(Object)this);
            for (Direction direction : Direction.values())
                level.updateNeighborsAt(blockPos.relative(direction), (Block)(Object)this);
            ci.cancel();
        }
    }
}
