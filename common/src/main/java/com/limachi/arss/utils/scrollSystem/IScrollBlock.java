package com.limachi.arss.utils.scrollSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IScrollBlock {
    BooleanProperty CAN_SCROLL = BooleanProperty.create("can_scroll");
    void scroll(Level var1, BlockPos var2, int var3, Player var4);
    @Environment(EnvType.CLIENT)
    void scrollFeedBack(Level var1, BlockPos var2, int var3, Player var4);
    @Environment(EnvType.CLIENT)
    boolean canScroll(Player var1, BlockPos var2);
}
