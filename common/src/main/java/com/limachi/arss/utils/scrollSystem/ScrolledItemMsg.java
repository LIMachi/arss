package com.limachi.arss.utils.scrollSystem;

import com.limachi.arss.utils.IMsg;
import com.limachi.arss.utils.annotations.RegisterMsg;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

@RegisterMsg(s2c = false, c2s = true)
public class ScrolledItemMsg implements IMsg<ScrolledItemMsg> {
    int slot;
    int delta;
    public ScrolledItemMsg() {}
    public ScrolledItemMsg(int slot, int delta) {
        this.slot = slot;
        this.delta = delta;
    }

    @Override
    public void serverWork(NetworkManager.PacketContext ctx) {
        Player player = ctx.getPlayer();
        Item item = player.getInventory().getItem(slot).getItem();
        if (item instanceof IScrollItem i)
            i.scroll(player, slot, delta);
    }


    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(slot).writeInt(delta);
    }

    @Override
    public ScrolledItemMsg read(RegistryFriendlyByteBuf buf) {
        slot = buf.readInt();
        delta = buf.readInt();
        return this;
    }
}