package com.limachi.arss.mixin;

import com.limachi.arss.common.block_entities.InstrumentSwapper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin {

    @Shadow
    protected abstract ResourceLocation getCustomSoundId(Level arg, BlockPos arg2);

    @Inject(method = "setInstrument(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"), cancellable = true)
    private void setInstrumentMixin(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir) {
        BlockState below = level.getBlockState(pos.below());
        if (cir.getReturnValue().is(state.getBlock()) && below.is(com.limachi.arss.common.blocks.InstrumentSwapper.R_BLOCK.get()))
            cir.setReturnValue(state.setValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT, below.getValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT)));
    }

    @Inject(method = "getCustomSoundId(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/resources/ResourceLocation;", at = @At("RETURN"), cancellable = true)
    private void getCustomSoundIdMixin(Level level, BlockPos pos, CallbackInfoReturnable<ResourceLocation> cir) {
        if (cir.getReturnValue() == null && level.getBlockEntity(pos.below()) instanceof InstrumentSwapper be)
            cir.setReturnValue(be.customSkullSound());
    }

    @Inject(method = "triggerEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V"), cancellable = true)
    private void triggerEventMixin(BlockState blockState, Level level, BlockPos blockPos, int i, int j, CallbackInfoReturnable<Boolean> cir) {
        NoteBlockInstrument noteblockinstrument = blockState.getValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT);
        if (noteblockinstrument.hasCustomSound()) {
            ResourceLocation rl = getCustomSoundId(level, blockPos);
            if (rl != null && InstrumentSwapper.isTunable(rl)) {
                level.playSeededSound(null, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, Holder.direct(SoundEvent.createVariableRangeEvent(rl)), SoundSource.RECORDS, 3f, NoteBlock.getPitchFromNote(blockState.getValue(BlockStateProperties.NOTE)), level.random.nextLong());
                cir.setReturnValue(true);
            }
        }
    }
}
