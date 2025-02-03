package com.limachi.arss.fabric.mixin;

import com.limachi.arss.utils.IAcceptCrouchInteractWithItem;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow @Final protected ServerPlayer player;

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void useItemOn(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir, BlockPos blockPos, BlockState blockState, boolean bl, boolean bl2) {
        if (bl2 && blockState.getBlock() instanceof IAcceptCrouchInteractWithItem a && a.overrideCrouchInteraction(itemStack, player, blockState, blockPos)) {
            ItemStack itemStack2 = itemStack.copy();
            InteractionResult interactionResult;
            ItemInteractionResult itemInteractionResult = blockState.useItemOn(serverPlayer.getItemInHand(interactionHand), level, serverPlayer, interactionHand, blockHitResult);
            if (itemInteractionResult.consumesAction()) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack2);
                cir.setReturnValue(itemInteractionResult.result());
                cir.cancel();
                return;
            }

            if (itemInteractionResult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && interactionHand == InteractionHand.MAIN_HAND) {
                interactionResult = blockState.useWithoutItem(level, serverPlayer, blockHitResult);
                if (interactionResult.consumesAction()) {
                    CriteriaTriggers.DEFAULT_BLOCK_USE.trigger(serverPlayer, blockPos);
                    cir.setReturnValue(interactionResult);
                    cir.cancel();
                }
            }
        }
    }
}
