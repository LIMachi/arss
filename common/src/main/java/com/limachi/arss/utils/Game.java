package com.limachi.arss.utils;

import com.limachi.arss.utils.client.ClientUtils;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * bunch of useful method to query information on the running instances, and to run code based on those information
 */
@SuppressWarnings("unused")
public class Game {

    /**
     * @return the current running server (Integrated, Test or Dedicated) or null if not running
     */
    public static MinecraftServer getServer() { return GameInstance.getServer(); }

    /**
     * @return the running minecraft client (obviously can only be called on the client side, as Minecraft class is not present on Dedicated server)
     */
    @Environment(EnvType.CLIENT)
    public static Minecraft getClient() { return GameInstance.getClient(); }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static boolean isSinglePlayer() {
            return getClient() instanceof Minecraft mc && mc.isSingleplayer();
        }

        public static boolean isLanHost() {
            return getClient() instanceof Minecraft mc && mc.getSingleplayerServer() instanceof IntegratedServer is && is.isPublished();
        }

        public static boolean isRemoteConnected() {
            if (getServer() instanceof MinecraftServer ms && ms.isSameThread())
                return ms.isDedicatedServer();
            return Minecraft.getInstance() instanceof Minecraft mc && mc.getConnection() instanceof ClientPacketListener cpl && cpl.getConnection().isConnected();
        }
    }

    /**
     * @return true if this jar is the Dedicated server (all client code removed)
     */
    public static boolean isPhysicalServer() { return Platform.getEnvironment() == Env.SERVER; }

    /**
     * @return true if this jar contains both the client and Integrated server
     */
    public static boolean isPhysicalClient() { return Platform.getEnvironment() == Env.CLIENT; }

    /**
     * @return true if the current world is a single player (not including lan worlds)
     */
    public static boolean isSinglePlayer() {
        return getPhysical(()->Client::isSinglePlayer, ()->()->false);
    }

    /**
     * @return true if the current world is a single player lan (you are the host, but the game is not expected to pause, player may connect, etc...)
     */
    public static boolean isLanHost() {
        return getPhysical(()->Client::isLanHost, ()->()->false);
    }

    /**
     * @return true if the current world is a remote connection (client connected to a dedicated server or lan world, and for the server, if it is running as dedicated)
     */
    public static boolean isRemoteConnected() {
        return getPhysical(()->Client::isRemoteConnected, ()->()->getServer() instanceof MinecraftServer ms && ms.isDedicatedServer());
    }

    /**
     * @return true if this is a logical server thread (ran by the physical server or as integrated)
     */
    public static boolean isLogicalServer() {
        MinecraftServer server = getServer();
        return server != null && (server.isSameThread() || isPhysicalServer());
    }

    /**
     * @return true if this is a logical client thread (ran by physical client AND on the main client thread)
     */
    public static boolean isLogicalClient() {
        return EnvExecutor.getInEnv(Env.CLIENT, ()->()->{
            Minecraft client = getClient();
            return client != null && client.isSameThread();
        }).orElse(false);
    }

    /**
     * @return true if this jar was made for Forge or NeoForge
     */
    public static boolean isForgeLike() { return Platform.isForgeLike(); }

    /**
     * @return true if this jar was made for Fabric or (TODO)Quilt
     */
    public static boolean isFabricLike() { return Platform.isFabric(); }

    /**
     * safely run the given nested lambda/static method if the predicate return true (the nesting of lambda/static method allow to call potentially unloaded/unavailable classes)
     * @param condition predicate to determine if the runnable should be ran
     * @param run a nested lambda/static method calling potentially unsafe methods
     */
    public static void safeRunner(Supplier<Boolean> condition, Supplier<Runnable> run) {
        if (condition.get())
            run.get().run();
    }

    /**
     * safely run the given nested lambda/static method if the predicate return true (the nesting of lambda/static method allow to call potentially unloaded/unavailable classes)
     * or return the default value otherwise
     * @param condition predicate to determine if the runnable should be ran
     * @param run a nested lambda/static method calling potentially unsafe methods
     * @param def the default value to be generated if the predicate fails
     */
    public static <T> T safeGetter(Supplier<Boolean> condition, Supplier<Supplier<T>> run, Supplier<T> def) {
        if (condition.get())
            return run.get().get();
        return def.get();
    }

    /**
     * safely run the given nested lambda/static method if the side match this jar type (the nesting of lambda/static method allow to call potentially unloaded/unavailable classes)
     * @param side to match against (note: this is a PHYSICAL side, CLIENT will be true even for Integrated server or hosted lan server!)
     * @param run a nested lambda/static method calling potentially unsafe methods (usually methods/classes that are themselves tagged with @Environment)
     */
    public static void runPhysical(Env side, Supplier<Runnable> run) {
        if (Platform.getEnvironment() == side)
            run.get().run();
    }

    /**
     * branching runner that will run 2 different codes depending on if you are on a physical client or server
     * @param client a nested lambda/static method calling potentially unsafe methods (usually methods/classes that are themselves tagged with @Environment(EnvType.CLIENT))
     * @param server a nested lambda/static method calling potentially unsafe methods (usually methods/classes that are themselves tagged with @Environment(EnvType.SERVER))
     */
    public static void runPhysical(Supplier<Runnable> client, Supplier<Runnable> server) {
        if (Platform.getEnvironment() == Env.CLIENT)
            client.get().run();
        else
            server.get().run();
    }

    /**
     * safely run the given nested lambda/static method if the side match this jar type (the nesting of lambda/static method allow to call potentially unloaded/unavailable classes)
     * @param side to match against (note: this is a PHYSICAL side, CLIENT will be true even for Integrated server or hosted lan server!)
     * @param run a nested lambda/static method calling potentially unsafe methods (usually methods/classes that are themselves tagged with @Environment)
     * @return an optional created from the return of the runnable (note: will transform null returns to empty), or empty if the side does not match
     */
    public static <T> Optional<T> getPhysical(Env side, Supplier<Supplier<T>> run) {
        if (Platform.getEnvironment() == side)
            return Optional.ofNullable(run.get().get());
        return Optional.empty();
    }

    /**
     * branching runner that will run 2 different codes depending on if you are on a physical client or server
     * @param client a nested lambda/static method calling potentially unsafe methods (usually methods/classes that are themselves tagged with @Environment(EnvType.CLIENT))
     * @param server a nested lambda/static method calling potentially unsafe methods (usually methods/classes that are themselves tagged with @Environment(EnvType.SERVER))
     * @return the value produced by either the client or server runner (it is considered infallible, contrary to logical)
     */
    public static <T> T getPhysical(Supplier<Supplier<T>> client, Supplier<Supplier<T>> server) {
        if (Platform.getEnvironment() == Env.CLIENT)
            return client.get().get();
        else
            return server.get().get();
    }

    /**
     * branching runner that will run 2 different codes depending on if you are on a logical client or server (note: might run neither branch in the case it is called on a non client thread while the integrated server is not running)
     * @param client a nested lambda/static method calling potentially unsafe methods (note: it is safe to use methods/classes that are tagged with @Environment(EnvType.CLIENT))
     * @param server a nested lambda/static method calling potentially unsafe methods (note: you might be running on a Dedicated or Integrated server, do not call methods/classes that are tagged with @Environment without a physical check)
     */
    public static void runLogical(Supplier<Runnable> client, Supplier<Runnable> server) {
        if (server != null && isLogicalServer())
            server.get().run();
        if (client != null)
            EnvExecutor.runInEnv(Env.CLIENT, ()->()->{
                if (getClient() instanceof Minecraft mc && mc.isSameThread())
                    client.get().run();
            });
    }

    /**
     * branching runner that will run 2 different codes depending on if you are on a logical client or server (note: might run neither branch in the case it is called on a non client thread while the integrated server is not running)
     * @param client a nested lambda/static method calling potentially unsafe methods (note: it is safe to use methods/classes that are tagged with @Environment(EnvType.CLIENT))
     * @param server a nested lambda/static method calling potentially unsafe methods (note: you might be running on a Dedicated or Integrated server, do not call methods/classes that are tagged with @Environment without a physical check)
     * @param def default supplier if neither the client nor the server was ran
     * @return either the result of client, server or def
     */
    public static <T> T getLogical(Supplier<Supplier<T>> client, Supplier<Supplier<T>> server, Supplier<T> def) {
        if (isLogicalServer() && server != null)
            return server.get().get();
        return EnvExecutor.getInEnv(Env.CLIENT, ()->()->{
            if (getClient() instanceof Minecraft mc && mc.isSameThread())
                return client.get().get();
            return null;
        }).orElseGet(def);
    }

    /**
     * branching runner that will run 2 different codes depending on if you are on (Neo)Forge or Fabric/Quilt (note: might run neither branch in the case a new platform is supported by Architectury)
     */
    public static void runModLoader(Supplier<Runnable> forgeLike, Supplier<Runnable> fabricLike) {
        if (isForgeLike())
            forgeLike.get().run();
        if (isFabricLike())
            fabricLike.get().run();
    }

    /**
     * branching runner that will run 2 different codes depending on if you are on (Neo)Forge or Fabric/Quilt (note: might run neither branch in the case a new platform is supported by Architectury)
     */
    public static <T> T getModLoader(Supplier<Supplier<T>> forgeLike, Supplier<Supplier<T>> fabricLike, Supplier<T> def) {
        if (isForgeLike())
            return forgeLike.get().get();
        if (isFabricLike())
            return fabricLike.get().get();
        return def.get();
    }

    /**
     * try to retrieve a level by resource location
     * @param location ResourceLocation of the level (ex: minecraft:overworld)
     * @return a level instance in 2 cases: we are on the logical client and the current world matches the location, or the server contains a level matching the location
     */
    public static Level getLevel(ResourceLocation location) {
        return getLogical(()->()->ClientUtils.getLevel(location), ()->()->{
            var server = GameInstance.getServer();
            if (server == null)
                return null;
            return server.getLevel(ResourceKey.create(Registries.DIMENSION, location));
        }, ()->null);
    }
}
