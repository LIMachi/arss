package com.limachi.arss.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Level.class)
public class LevelMixin {
//    @Redirect(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
//    public boolean isBlockCallInUpdateNeighbourForOutputSignal(BlockState self, Block block) {
//        return (self.getBlock() instanceof DiodeBlock && !(self.getBlock() instanceof RepeaterBlock)); //could use a block tag instead
//    }
    //FIXME: redirect is not compatible with neoforge
}
