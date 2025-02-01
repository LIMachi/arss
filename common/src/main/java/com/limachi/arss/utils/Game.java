package com.limachi.arss.utils;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;
import java.util.function.Supplier;

public class Game {
    public static MinecraftServer getServer() {
        return GameInstance.getServer();
    }

    @Environment(EnvType.CLIENT)
    public static Minecraft getClient() {
        return GameInstance.getClient();
    }

    public static boolean isPhysicalServer() {
        return Platform.getEnvironment() == Env.SERVER;
    }

    public static boolean isPhysicalClient() {
        return Platform.getEnvironment() == Env.CLIENT;
    }

    public static boolean isLogicalServer() {
        MinecraftServer server = getServer();
        return server != null && server.isSameThread();
    }

    public static boolean isLogicalClient() {
        return EnvExecutor.getInEnv(Env.CLIENT, ()->()->{
            Minecraft client = getClient();
            return client != null && client.isSameThread();
        }).orElse(false);
    }

    public static void runPhysical(Env side, Supplier<Runnable> run) {
        if (Platform.getEnvironment() == side)
            run.get().run();
    }

    public static void runPhysical(Supplier<Runnable> client, Supplier<Runnable> server) {
        if (Platform.getEnvironment() == Env.CLIENT)
            client.get().run();
        else
            server.get().run();
    }

    public static <T> Optional<T> getPhysical(Env side, Supplier<Supplier<T>> run) {
        if (Platform.getEnvironment() == side)
            return Optional.ofNullable(run.get().get());
        return Optional.empty();
    }

    public static <T> T getPhysical(Supplier<Supplier<T>> client, Supplier<Supplier<T>> server) {
        if (Platform.getEnvironment() == Env.CLIENT)
            return client.get().get();
        else
            return server.get().get();
    }

    public static void runLogical(Supplier<Runnable> client, Supplier<Runnable> server) {
        if (isLogicalServer())
            server.get().run();
        EnvExecutor.runInEnv(Env.CLIENT, ()->()->{
            if (getClient() instanceof Minecraft mc && mc.isSameThread())
                client.get().run();
        });
    }

    public static <T> T getLogical(Supplier<Supplier<T>> client, Supplier<Supplier<T>> server, Supplier<T> def) {
        if (isLogicalServer())
            return server.get().get();
        return EnvExecutor.getInEnv(Env.CLIENT, ()->()->{
            if (getClient() instanceof Minecraft mc && mc.isSameThread())
                return client.get().get();
            return null;
        }).orElseGet(def);
    }
}
