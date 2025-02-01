package com.limachi.arss.mixin.client;

import com.limachi.arss.utils.IItemMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    private ItemStack offHandItem;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (minecraft.player instanceof LocalPlayer player) {
            ItemStack newStack = player.getMainHandItem();
            if (mainHandItem.getItem() instanceof IItemMixin m && !m.shouldCauseReequipAnimation(mainHandItem, newStack, player.getInventory().selected))
                mainHandItem = newStack;
            newStack = player.getOffhandItem();
            if (offHandItem.getItem() instanceof IItemMixin m && !m.shouldCauseReequipAnimation(offHandItem, newStack, -1))
                offHandItem = newStack;
        }
    }
}
