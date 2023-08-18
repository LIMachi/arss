package com.limachi.arss.mixin;

import com.limachi.arss.blockEntities.InstrumentSwapperBlockEntity;
import com.limachi.arss.blocks.InstrumentSwapperBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {

    @Inject(method = "setInstrument(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"), cancellable = true)
    private void setInstrument(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir) {
        BlockState below = level.getBlockState(pos.below());
        if (cir.getReturnValue().is(state.getBlock()) && below.is(InstrumentSwapperBlock.R_BLOCK.get()))
            cir.setReturnValue(state.setValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT, below.getValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT)));
    }

    @Inject(method = "getCustomSoundId(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/resources/ResourceLocation;", at = @At("RETURN"), cancellable = true)
    private void getCustomSoundId(Level level, BlockPos pos, CallbackInfoReturnable<ResourceLocation> cir) {
        if (cir.getReturnValue() == null && level.getBlockEntity(pos.below()) instanceof InstrumentSwapperBlockEntity be)
            cir.setReturnValue(be.customSkullSound());
    }
}
