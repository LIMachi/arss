package com.limachi.arss.blocks.scrollSystem;

import com.limachi.arss.network.PacketHandler;
import com.limachi.arss.network.ScrolledBlock;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
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

    public static final KeyMapping keyScroll = new KeyMapping("key.hold_to_scroll", 340, "key.categories.arss");
    static {
        ClientRegistry.registerKeyBinding(keyScroll);
    }

    @SubscribeEvent
    public static void scrollBlockEvent(InputEvent.MouseScrollEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && (keyScroll.isDown() || keyScroll.isUnbound())) {
            HitResult target = Minecraft.getInstance().hitResult;
            if (target != null && target.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) target).getBlockPos();
                BlockEntity be = player.level.getBlockEntity(pos);
                Block block = player.level.getBlockState(pos).getBlock();
                if ((block instanceof IScrollBlock sb && sb.canScroll(player.level, pos)) || (be instanceof IScrollBlock sbe && sbe.canScroll(player.level, pos))) {
                    POS = pos;
                    DELTA += event.getScrollDelta();
                    COUNTDOWN = COUNTDOWN_LENGTH;
                    if (block instanceof IScrollBlock sb)
                        sb.scrollFeedBack(player.level, pos, DELTA, player);
                    if (be instanceof IScrollBlock sbe)
                        sbe.scrollFeedBack(player.level, pos, DELTA, player);
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
