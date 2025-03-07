package com.limachi.arss.utils.config;

import com.limachi.arss.utils.Game;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.SingleRunnableReloadListener;
import com.limachi.arss.utils.annotations.Config;
import com.limachi.arss.utils.annotations.RegisterMsg;
import com.limachi.arss.utils.network.IS2CMsg;
import com.limachi.arss.utils.reflect.AnnotationExtractor;
import com.limachi.arss.utils.reflect.FieldAccess;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConfigManager {
    private record Default<T>(T def, T min, T max, Function<String, T> parser){}
    private static final HashMap<Class<?>, Default<?>> DEFAULTS = new HashMap<>();

    private static <T, P> void defaultsEntry(Class<T> clazz, Class<P> prim, T def, T min, T max, Function<String, T> conv) {
        Default<?> e = new Default<>(def, min, max, conv);
        if (prim != null)
            DEFAULTS.put(prim, e);
        DEFAULTS.put(clazz, e);
    }

    static {
        defaultsEntry(Boolean.class, boolean.class, false, null, null, Boolean::parseBoolean);
        defaultsEntry(Byte.class, byte.class, (byte)0, Byte.MIN_VALUE, Byte.MAX_VALUE, Byte::parseByte);
        defaultsEntry(Short.class, short.class, (short)0, Short.MIN_VALUE, Short.MAX_VALUE, Short::parseShort);
        defaultsEntry(Integer.class, int.class, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::parseInt);
        defaultsEntry(Long.class, long.class, (long)0, Long.MIN_VALUE, Long.MAX_VALUE, Long::parseLong);
        defaultsEntry(Float.class, float.class, 0f, Float.MIN_VALUE, Float.MAX_VALUE, Float::parseFloat);
        defaultsEntry(Double.class, double.class, 0., Double.MIN_VALUE, Double.MAX_VALUE, Double::parseDouble);
        defaultsEntry(String.class, null, "", null, null, s->s);
    }

    private static <T> T getMin(Class<T> target, Config a, StringBuilder cmt) {
        var e = DEFAULTS.get(target);
        if (!a.min().isBlank()) {
            if (!cmt.isEmpty())
                cmt.append('\n');
            cmt.append("Minimum value: '").append(a.min()).append('\'');
            return (T) e.parser.apply(a.min());
        }
        return (T)e.min;
    }

    private static <T> T getMax(Class<T> target, Config a, StringBuilder cmt) {
        var e = DEFAULTS.get(target);
        if (!a.min().isBlank()) {
            if (!cmt.isEmpty())
                cmt.append('\n');
            cmt.append("Maximum value: '").append(a.max()).append('\'');
            return (T) e.parser.apply(a.max());
        }
        return (T)e.max;
    }

    private static <T> List<T> getList(Class<T> target, Config a, StringBuilder cmt) {
        var e = DEFAULTS.get(target);
        if (a.list().length > 0) {
            String[] v = a.list();
            var out = new ArrayList<T>(v.length);
            if (!cmt.isEmpty())
                cmt.append('\n');
            cmt.append(a.whiteList() ? "Valid" : "Invalid").append(" values: '");
            for (int i = 0; i < v.length; ++i) {
                out.add((T)e.parser.apply(v[i]));
                cmt.append(v[i]);
                if (i < v.length - 1)
                    cmt.append("', '");
            }
            cmt.append('\'');
            return out;
        }
        return null;
    }

    protected ConfigFile client;
    protected ConfigFile server;
    protected Path clientPath;
    protected Path serverPath;

    public ConfigManager(String modId, AnnotationExtractor extractor) {
        client = new ConfigFile();
        server = new ConfigFile();
        extract(extractor);
        serverPath = Platform.getConfigFolder().resolve(modId + "-server.cfg").toAbsolutePath();
        PlayerEvent.PLAYER_JOIN.register(p->new SyncToClient(server.saveAsRaw()).sendToClient(p));
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new SingleRunnableReloadListener(()->{
            server.load(serverPath, true);
            String raw = server.saveAsRaw();
            try {
                Files.writeString(serverPath, raw);
            } catch (IOException ignore) {}
            new SyncToClient(raw).sendToClients();
        }), ResourceLocation.fromNamespaceAndPath(modId, "server-config"));
        if (Game.isPhysicalClient()) {
            clientPath = Platform.getConfigFolder().resolve(modId + "-client.cfg").toAbsolutePath();
            ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new SingleRunnableReloadListener(()->{
                client.load(clientPath, true);
                client.save(clientPath);
            }), ResourceLocation.fromNamespaceAndPath(modId, "client-config"));
            client.load(clientPath, false);
            client.save(clientPath);
        }
        server.load(serverPath, false);
        server.save(serverPath);
    }

    @RegisterMsg
    public record SyncToClient(String raw) implements IS2CMsg<SyncToClient> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            ModBase.configs.server.load(raw, false);
        }
    }

    public <T> void register(String path, String comment, Class<?> type, FieldAccess<?, T> access, Function<Object, Object> validator, boolean reload, boolean isClient) {
        Class<?> innerType = type.isArray() ? type.getComponentType() : type;
        if (!DEFAULTS.containsKey(innerType)) {
            ModBase.logger.error("@Config on invalid/unsupported type: " + innerType);
            return;
        }
        if (isClient)
            client.registerValue(path, comment, access, validator, reload);
        else
            server.registerValue(path, comment, access, validator, reload);
    }

    private static void reloadCmt(Config a, StringBuilder cmt) {
        if (!cmt.isEmpty())
            cmt.append('\n');
        if (a.reload()) {
            cmt.append("Will be reloaded at the same time as ");
            if (a.client())
                cmt.append("client resource (default F3 + T)\n");
            else
                cmt.append("server datapack (by the '/reload' command)");
        } else
            cmt.append("Can only be changed between restart (might be reset to default while the game/server is running)");
    }

    public void extract(AnnotationExtractor extractor) {
        extractor.runOnFields(Config.class, (f, a)->{
            StringBuilder cmt = new StringBuilder(a.cmt());
            reloadCmt(a, cmt);
            Class<?> ft = f.type();
            boolean isArray = ft.isArray();
            Class<?> type = isArray ? ft.getComponentType() : ft;
            List<?> list = getList(type, a, cmt);
            Object min = getMin(type, a, cmt);
            Object max = getMax(type, a, cmt);
            String path = a.path().equals("<auto>") ? f.clazz().getName() : a.path();
            String name = a.name().isBlank() ? f.name() : a.name();
            Predicate<Object> listPred = list == null || list.isEmpty() ? v->!a.whiteList() : type.equals(String.class) ? v->{
                for (String reg : (List<String>)list)
                    if (((String)v).matches(reg))
                        if (a.whiteList())
                            return true;
                return !a.whiteList();
            } : v->list.contains(v) ^ a.whiteList();
            Function<Object, Object> pred = v->{
                if (v == null)
                    return null;
                if (!listPred.test(v))
                    v = f.get();
                if (v instanceof Comparable t) {
                    if (max instanceof Comparable c && c.compareTo(t) < 0)
                        v= max;
                    if (min instanceof Comparable c && c.compareTo(t) > 0)
                        v = min;
                }
                return v;
            };
            register((path.isBlank() ? "" : path + ".") + name, cmt.toString(), ft, f, pred, a.reload(), a.client());
        });
    }

    private final HashSet<Runnable> reloadListeners = new HashSet<>();

    public void addReloadListener(Runnable runnable) {
        reloadListeners.add(runnable);
    }

    public boolean save() { return Game.getLogical(()->()->client.save(clientPath), ()->()->server.save(serverPath), ()->false); }

    public String dump() { return "Client:\n" + client.dump() + "\nServer:\n" + server.dump(); }
}
