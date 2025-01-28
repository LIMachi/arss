package com.limachi.arss.utils.scrollSystem;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class ScrollHandler {
    public static int COUNTDOWN_LENGTH_BLOCK = 20;
    public static int COUNTDOWN_LENGTH_ITEM = 5;

    private static int DELTA = 0;
    private static int COUNTDOWN = -1;
    private static BlockPos POS = null;
    private static int SLOT = -1;

    public static void register() {
        ClientRawInputEvent.MOUSE_SCROLLED.register(ScrollHandler::mouseScrolled);
        ClientTickEvent.CLIENT_LEVEL_POST.register(ScrollHandler::flush);
    }

    private static void flush(ClientLevel clientLevel) {
        if (COUNTDOWN < 0) return;
        if (COUNTDOWN-- == 0) {
            if (DELTA != 0) {
                if (POS != null)
                    new ScrolledBlockMsg(POS, DELTA).sendToServer();
                if (SLOT != -1)
                    new ScrolledItemMsg(SLOT, DELTA).sendToServer();
            }
            DELTA = 0;
            POS = null;
            SLOT = -1;
        }
    }

    private static EventResult mouseScrolled(Minecraft minecraft, double x, double y) {
        Player player = minecraft.player;
        if (player != null) {
            if (player.getMainHandItem().getItem() instanceof IScrollItem item && item.canScroll(player, player.getInventory().selected)) {
                DELTA += y;
                COUNTDOWN = COUNTDOWN_LENGTH_ITEM;
                SLOT = player.getInventory().selected;
                item.scrollFeedBack(player, SLOT, DELTA);
                return EventResult.interruptFalse();
            } else if (player.getOffhandItem().getItem() instanceof IScrollItem item && item.canScroll(player, Inventory.SLOT_OFFHAND)) {
                DELTA += y;
                COUNTDOWN = COUNTDOWN_LENGTH_ITEM;
                SLOT = Inventory.SLOT_OFFHAND;
                item.scrollFeedBack(player, SLOT, DELTA);
                return EventResult.interruptFalse();
            } else {
                HitResult target = Minecraft.getInstance().hitResult;
                if (target != null && target.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = ((BlockHitResult) target).getBlockPos();
                    BlockEntity be = player.level().getBlockEntity(pos);
                    Block block = player.level().getBlockState(pos).getBlock();
                    if ((block instanceof IScrollBlock sb && sb.canScroll(player, pos)) || (be instanceof IScrollBlock sbe && sbe.canScroll(player, pos))) {
                        POS = pos;
                        DELTA += y;
                        COUNTDOWN = COUNTDOWN_LENGTH_BLOCK;
                        if (block instanceof IScrollBlock sb)
                            sb.scrollFeedBack(player.level(), pos, DELTA, player);
                        if (be instanceof IScrollBlock sbe)
                            sbe.scrollFeedBack(player.level(), pos, DELTA, player);
                        return EventResult.interruptFalse();
                    }
                }
            }
        }
        return EventResult.pass();
    }
}
