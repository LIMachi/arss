package com.limachi.arss.utils.scrollSystem;

import com.limachi.arss.utils.IMsg;
import com.limachi.arss.utils.annotations.RegisterMsg;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

@RegisterMsg(s2c = false, c2s = true)
public class ScrolledBlockMsg implements IMsg<ScrolledBlockMsg> {
    BlockPos pos;
    int delta;
    public ScrolledBlockMsg() {}
    public ScrolledBlockMsg(BlockPos pos, int delta) {
        this.pos = pos;
        this.delta = delta;
    }

    @Override
    public void serverWork(NetworkManager.PacketContext ctx) {
        Player player = ctx.getPlayer();
        Level level = player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IScrollBlock)
            ((IScrollBlock)be).scroll(level, pos, delta, player);
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof IScrollBlock)
            ((IScrollBlock)block).scroll(level, pos, delta, player);
    }


    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeInt(delta);
    }

    @Override
    public ScrolledBlockMsg read(RegistryFriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        delta = buf.readInt();
        return this;
    }
}
