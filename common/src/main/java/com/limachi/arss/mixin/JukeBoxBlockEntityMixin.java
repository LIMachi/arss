package com.limachi.arss.mixin;

import com.limachi.arss.common.items.SequencerMemoryDisc;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlockEntity.class)
public class JukeBoxBlockEntityMixin {
    @Shadow private ItemStack item;
    @Shadow @Final private JukeboxSongPlayer jukeboxSongPlayer;

    @Inject(method = "getComparatorOutput", at = @At("HEAD"), cancellable = true)
    public void getComparatorOutputMixin(CallbackInfoReturnable<Integer> cir) {
        JukeboxBlockEntity self = (JukeboxBlockEntity)(Object)this;
        Level level = self.getLevel();
        if (level != null && jukeboxSongPlayer.isPlaying() && item.is(SequencerMemoryDisc.R_ITEM.get())) {
            cir.setReturnValue(level.getRandom().nextInt(1, 15));
            cir.cancel();
        }
    }
}
