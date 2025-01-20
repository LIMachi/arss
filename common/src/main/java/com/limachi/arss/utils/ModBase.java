package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.*;
import com.limachi.arss.utils.commands.CommandManager;
import com.limachi.arss.utils.config.ConfigManager;
import com.limachi.arss.utils.reflect.AnnotationExtractor;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        registries.extractInStages();
        instance = registries.initMod();
        registries.register();
        CommandManager.register();
    }

    @Environment(EnvType.CLIENT)
    public static abstract class ClientModBase {

        public static ClientRegistries registries = new ClientRegistries();

        public static void init() {
            registries.extractInStages();

            registries.register();
        }
    }

    @Environment(EnvType.SERVER)
    public static abstract class ServerModBase {

    }
}
