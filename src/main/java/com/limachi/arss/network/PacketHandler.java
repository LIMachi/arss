package com.limachi.arss.network;

import com.limachi.arss.Arss;
import com.limachi.arss.utils.StaticInitializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

@StaticInitializer.Static
public class PacketHandler {
    public static abstract class Message {
        public Message() {}
        public Message(FriendlyByteBuf buffer) {}
        public abstract void toBytes(FriendlyByteBuf buffer);
        public void clientWork() {}
        public void serverWork(Player player) {}
    }

    protected static <T extends Message> void registerMsg(Class<T> clazz) {
        try {
            HANDLER.registerMessage(
                    index++,
                    clazz,
                    (msg, buffer) -> {
                        clazz.cast(msg).toBytes(buffer);
                    },
                    buffer->{
                        try {
                            return clazz.getConstructor(FriendlyByteBuf.class).newInstance(buffer);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                    , (msg, scntx) -> {
                        if (!clazz.isInstance(msg)) {
                            //FIXME: add some kind of error there
                            return;
                        }
                        NetworkEvent.Context ctx = scntx.get();
                        PacketHandler.Target t = PacketHandler.target(ctx);
                        if (t == PacketHandler.Target.CLIENT)
                            ctx.enqueueWork(((Message)msg)::clientWork);
                        if (t == PacketHandler.Target.SERVER)
                            ctx.enqueueWork(()->((Message)msg).serverWork(ctx.getSender()));
                        ctx.setPacketHandled(true);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static enum Target {
        CLIENT,
        SERVER,
        I_M_NOT_SURE
    }

    public static Target target(NetworkEvent.Context ctx) {
        if (ctx.getDirection().getOriginationSide() == LogicalSide.SERVER)
            return ctx.getDirection().getReceptionSide() == LogicalSide.CLIENT ? Target.CLIENT : Target.I_M_NOT_SURE;
        if (ctx.getDirection().getOriginationSide() == LogicalSide.CLIENT)
            return ctx.getDirection().getReceptionSide() == LogicalSide.SERVER ? Target.SERVER : Target.I_M_NOT_SURE;
        return Target.I_M_NOT_SURE;
    }

    public static <T extends Message> void toServer(T msg) { if (msg != null) HANDLER.sendToServer(msg); }
    public static <T extends Message> void toClients(T msg) {
        if (msg != null)
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
                toClient(player, msg);
    }
    public static <T extends Message> void toClient(ServerPlayer player, T msg) { if (msg != null && !(player instanceof FakePlayer)) HANDLER.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT); }

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(Arss.MOD_ID, "network"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int index = 0;

    static {
        registerMsg(ScrolledBlock.class);
    }
}
