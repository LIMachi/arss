package com.limachi.arss.fabric.mixin.client;

import com.limachi.arss.utils.IAcceptCrouchInteractWithItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.apache.commons.lang3.mutable.MutableObject;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow protected abstract void startPrediction(ClientLevel clientLevel, PredictiveAction predictiveAction);

    @Shadow @Final private Minecraft minecraft;

    @Shadow protected abstract InteractionResult performUseItemOn(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult);

    @Shadow @Final private ClientPacketListener connection;

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void useItemOn(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!localPlayer.getItemInHand(interactionHand).isEmpty() && minecraft.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof IAcceptCrouchInteractWithItem) {
            MutableObject<InteractionResult> mutableObject = new MutableObject();
            startPrediction(minecraft.level, i->{
                mutableObject.setValue(performUseItemOn(localPlayer, interactionHand, blockHitResult));
                return new ServerboundUseItemOnPacket(interactionHand, blockHitResult, i);
            });
            cir.setReturnValue(mutableObject.getValue());
            cir.cancel();
        }
    }

    @Inject(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void useItemOn(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        BlockState blockState = this.minecraft.level.getBlockState(blockPos);
        if (bl && localPlayer.isSecondaryUseActive() && blockState.getBlock() instanceof IAcceptCrouchInteractWithItem) {
            if (!connection.isFeatureEnabled(blockState.getBlock().requiredFeatures())) {
                cir.setReturnValue(InteractionResult.FAIL);
                cir.cancel();
                return;
            }

            ItemInteractionResult itemInteractionResult = blockState.useItemOn(localPlayer.getItemInHand(interactionHand), this.minecraft.level, localPlayer, interactionHand, blockHitResult);
            if (itemInteractionResult.consumesAction()) {
                cir.setReturnValue(itemInteractionResult.result());
                cir.cancel();
                return;
            }

            if (itemInteractionResult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && interactionHand == InteractionHand.MAIN_HAND) {
                InteractionResult interactionResult = blockState.useWithoutItem(this.minecraft.level, localPlayer, blockHitResult);
                if (interactionResult.consumesAction()) {
                    cir.setReturnValue(interactionResult);
                    cir.cancel();
                }
            }
        }
    }
}
