package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.StaticInit;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;

public class CommonEvents {
    @StaticInit
    public static void registerAcceptCrouchInteractWithItemBypass() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face)->{
            if (player.isCrouching() && player.level().getBlockState(pos).getBlock() instanceof IAcceptCrouchInteractWithItem)
                return EventResult.interruptFalse();
            return EventResult.pass();
        });
    }
}
