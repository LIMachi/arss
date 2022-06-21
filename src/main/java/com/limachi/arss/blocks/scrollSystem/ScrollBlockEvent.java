package com.limachi.arss.blocks.scrollSystem;

import com.limachi.arss.network.PacketHandler;
import com.limachi.arss.network.ScrolledBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ScrollBlockEvent {

    public static int COUNTDOWN_LENGTH = 20;

    private static int DELTA = 0;
    private static int COUNTDOWN = -1;
    private static BlockPos POS = null;

    @SubscribeEvent
    public static void scrollBlockEvent(InputEvent.MouseScrollEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.isShiftKeyDown()) {
            HitResult target = Minecraft.getInstance().hitResult;
            if (target != null && target.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) target).getBlockPos();
                BlockEntity be = player.level.getBlockEntity(pos);
                Block block = player.level.getBlockState(pos).getBlock();
                if (block instanceof IScrollBlock || be instanceof IScrollBlock) {
                    POS = pos;
                    DELTA += event.getScrollDelta();
                    COUNTDOWN = COUNTDOWN_LENGTH;
                    if (be instanceof IScrollBlock)
                        ((IScrollBlock)be).scrollFeedBack(player.level, pos, DELTA, player);
                    if (block instanceof IScrollBlock)
                        ((IScrollBlock)block).scrollFeedBack(player.level, pos, DELTA, player);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void flushOnFinishedCountDown(TickEvent.ClientTickEvent event) {
        if (COUNTDOWN < 0) return;
        if (COUNTDOWN-- == 0) {
            if (DELTA != 0 && POS != null)
                PacketHandler.toServer(new ScrolledBlock(POS, DELTA));
            DELTA = 0;
            POS = null;
        }
    }
}
