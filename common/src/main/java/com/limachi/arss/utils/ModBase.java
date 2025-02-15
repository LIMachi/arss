package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.*;
import com.limachi.arss.utils.client.ClientRegistries;
import com.limachi.arss.utils.client.ClientStage;
import com.limachi.arss.utils.commands.CommandManager;
import com.limachi.arss.utils.config.ConfigManager;
import com.limachi.arss.utils.config.ConfigScreen;
import com.limachi.arss.utils.reflect.AnnotationExtractor;
import com.limachi.arss.utils.scrollSystem.ScrollHandler;

import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public abstract class ModBase {
    public static Logger logger;
    public static ModBase instance;
    public static Registries registries;
    public static AnnotationExtractor extractor;
    public static ConfigManager configs = null;

    public ModBase() {}

    private static void extractMod(AnnotationExtractor extractor) {
        extractor.runOnClasses(Mod.class, (c, a)->{
            if (!ModBase.class.isAssignableFrom(c)) {
                System.err.println("@Mod should be used on a class that extends ModBase: " + c);
                System.exit(-1);
            }
            if (registries == null) {
                logger = LogManager.getLogger(a.value());
                configs = new ConfigManager(Platform.getConfigFolder().resolve(a.value() + ".cfg"));
                configs.extract(extractor);
                configs.load();
                registries = new Registries(a.value(), (Class<ModBase>)c);
            } else {
                System.err.println("@Mod is used multiple times: " + registries.mod + " & " + c);
                System.exit(-1);
            }
        });
        if (registries == null) {
            System.err.println("missing @Mod annotation");
            System.exit(-1);
        }
    }

    public static void init(AnnotationExtractor extractor) {
        ModBase.extractor = extractor;
        extractMod(extractor);
        StaticInitializer.initialize(Stage.FIRST, true);
        StaticInitializer.initialize(Stage.FIRST, false);
        registries.extractInStages();
        instance = registries.initMod();
        registries.register();
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new SingleRunnableReloadListener(()->configs.load()), ResourceLocation.fromNamespaceAndPath(registries.mod_id, "config"));
        extractor.runOnMethods(ReloadListener.class, (m, a)->ReloadListenerRegistry.register(a.type(), (preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2) -> (CompletableFuture<Void>)m.get(null, true, preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2), ResourceLocation.fromNamespaceAndPath(registries.mod_id, Registries.defaultToMethod(a.value(), m))));
        CommandManager.register();
        StaticInitializer.initialize(Stage.LAST, true);
        StaticInitializer.initialize(Stage.LAST, false);
    }

    @Environment(EnvType.CLIENT)
    public static abstract class ClientModBase {
        public static ClientRegistries registries = new ClientRegistries();

        public static void init() {
            StaticInitializer.initialize(ClientStage.FIRST, true);
            StaticInitializer.initialize(ClientStage.FIRST, false);
            registries.extractInStages();
            registries.register();
            ScrollHandler.register();
            StaticInitializer.initialize(ClientStage.LAST, true);
            Platform.getMod(ModBase.registries.mod_id).registerConfigurationScreen(parent->new ConfigScreen(parent, configs));
            StaticInitializer.initialize(ClientStage.LAST, false);
        }
    }

    @Environment(EnvType.SERVER)
    public static abstract class ServerModBase {
        public static void init() {}
    }
}
