package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.*;
import com.limachi.arss.utils.clientAnnotations.BlockTinter;
import com.limachi.arss.utils.commands.CommandManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ModBase {
    public static Logger logger;
    public static ModBase instance;
    public static Registries registries;

    public ModBase() {}

    public static String defaultToClass(String nullable, Class<?> clazz) {
        if (nullable == null || nullable.isBlank())
            return StringUtils.camelToSnake(StringUtils.getSimplifiedClassName(clazz.getName()));
        return nullable;
    }

    public static String defaultToMethod(String nullable, ClassExtractor.MethodAccess<?> m) {
        if (nullable == null || nullable.isBlank())
            return StringUtils.camelToSnake(m.name());
        return nullable;
    }

    public static String defaultToField(String nullable, ClassExtractor.FieldAccess<?> f) {
        if (nullable == null || nullable.isBlank())
            return StringUtils.camelToSnake(f.name());
        return nullable;
    }

    static void extractAnnotations() {
        ClassExtractor.runFieldAnnotations(RegisterBlock.class, (f, a)->{
            String name = defaultToClass(a.name(), f.clazz());
            f.setStatic(registries.blocks.register(name, ()->{
                try {
                    return (Block)(f.clazz().getConstructor().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                    return null;
                }
            }));
        });
        ClassExtractor.runFieldAnnotations(RegisterItem.class, (f, a)->{
            String name = defaultToClass(a.name(), f.clazz());
            f.setStatic(registries.item(name, ()->{
                try {
                    return (Item)(f.clazz().getConstructor().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                    return null;
                }
            }, a.jeiInfoKey(), a.tab()));
        });
        ClassExtractor.runFieldAnnotations(RegisterBlockItem.class, (f, a)->{
            String name = a.name();
            if (name.isBlank()) {
                name = defaultToClass(a.name(), f.clazz());
                if (name.endsWith("_block"))
                    name = name.substring(0, name.length() - 6) + "_item";
            }
            String block = defaultToClass(a.block(), f.clazz());
            try {
                f.setStatic(
                        registries.item(name,
                                ()->{
                                    try {
                                        return new BlockItem(registries.blocks.getRegistrar().get(ResourceLocation.fromNamespaceAndPath(registries.mod_id, block)), new Item.Properties());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        System.exit(-1);
                                        return null;
                                    }
                                },
                                a.jeiInfoKey(),
                                a.tab())
                );
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        });
        ClassExtractor.runMethodAnnotations(RegisterTab.class, (m, a)->{
            var t = registries.tabs.register(defaultToMethod(a.name(), m), ()->CreativeTabRegistry.create(m::invokeStatic));
            if (a.defaultTab())
                registries.default_tab = t;
        });
    }

    private static void extractMod() {
        ClassExtractor.runClassAnnotations(Mod.class, (c, a)->{
            if (!ModBase.class.isAssignableFrom(c)) {
                System.err.println("@Mod should be used on a class that extends ModBase: " + c);
                System.exit(-1);
            }
            if (registries == null) {
                registries = new Registries(a.value(), (Class<ModBase>)c);
                logger = LogManager.getLogger(a.value());
            } else {
                System.err.println("@Mod is used multiple times: " + registries.mod + " & " + c);
                System.exit(-1);
            }
        });
    }

    public static void init() {
        ClassExtractor.extractClasses();
        extractMod();
        registries.default_tab = registries.tabs.register("tab", CreativeTabRegistry.ofBuiltin(CreativeModeTabs.getDefaultTab()));
        extractAnnotations();
        instance = registries.initMod();
        registries.register();
        CommandManager.register();
    }

    @Environment(EnvType.CLIENT)
    public static abstract class ClientModBase {
        public static void init() {
            ClassExtractor.runMethodAnnotations(BlockTinter.class, (m, a)->{
                String name = a.name();
                if (name.isBlank())
                    name = StringUtils.camelToSnake(StringUtils.getSimplifiedClassName(m.clazz().getName()));
                String finalName = name;
                ColorHandlerRegistry.registerBlockColors(m::invokeStatic, ()->registries.blocks.getRegistrar().get(ResourceLocation.fromNamespaceAndPath(registries.mod_id, finalName)));
            });
        }
    }
}
