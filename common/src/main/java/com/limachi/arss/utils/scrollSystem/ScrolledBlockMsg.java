package com.limachi.arss.utils.scrollSystem;

import com.limachi.arss.utils.network.IC2SMsg;
import com.limachi.arss.utils.annotations.RegisterMsg;

import dev.architectury.networking.NetworkManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

@RegisterMsg
public record ScrolledBlockMsg(BlockPos pos, int delta) implements IC2SMsg<ScrolledBlockMsg> {
    @Override
    public void run(NetworkManager.PacketContext ctx) {
        Player player = ctx.getPlayer();
        Level level = player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IScrollBlock)
            ((IScrollBlock)be).scroll(level, pos, delta, player);
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof IScrollBlock)
            ((IScrollBlock)block).scroll(level, pos, delta, player);
    }
}
