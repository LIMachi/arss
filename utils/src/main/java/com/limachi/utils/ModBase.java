package com.limachi.utils;

import com.limachi.utils.annotations.RegisterBlock;
import com.limachi.utils.annotations.RegisterBlockItem;
import com.limachi.utils.annotations.RegisterItem;
import com.limachi.utils.clientAnnotations.BlockTinter;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public abstract class ModBase {
    public static final String mod_id = "$MOD_ID";
    public static final Logger logger = LogManager.getLogger(mod_id);

    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(mod_id, net.minecraft.core.registries.Registries.BLOCK);
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(mod_id, net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE);
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(mod_id, net.minecraft.core.registries.Registries.ITEM);

    public ModBase() {}

    public static void extractClasses() {
        try {
            ClassExtractor.extractClasses(Class.forName("$ENTRY_POINT"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Item> RegistrySupplier<T> item(String reg_key, Supplier<T> n, String infoKey, String[] tab) {
        RegistrySupplier<T> out = ITEMS.register(reg_key, n);
//        if (out != null && infoKey != null && !infoKey.isBlank())
//            JEIInfo.registerInfo(out, infoKey);
//        if (tab != null && out != null)
//            for (String t : tab)
//                if (t != null && !t.isBlank()) {
//                    if (t.equals("automatic"))
//                        t = LimLib.INSTANCES.get(modId).tab().getKey().location().toString();
//                    CREATIVE_TABS.compute(t, (k, v) -> {
//                        if (v == null)
//                            v = new LinkedList<>();
//                        v.add((RegistryObject<Item>)out);
//                        return v;
//                    });
//                }
        return out;
    }

    public static String defaultToClass(String nullable, Class<?> clazz) {
        if (nullable == null || nullable.isBlank())
            return StringUtils.camelToSnake(StringUtils.getSimplifiedClassName(clazz.getName()));
        return nullable;
    }

    public static void init() {
        if (ClassExtractor.CLASSES.isEmpty())
            extractClasses();
        ClassExtractor.runFieldAnnotations(RegisterBlock.class, (f, a)->{
            String name = defaultToClass(a.name(), f.clazz());
            f.setStatic(BLOCKS.register(name, ()->{
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
            f.setStatic(item(name, ()->{
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
                        item(name,
                                ()->{
                            try {
                                return new BlockItem(BLOCKS.getRegistrar().get(ResourceLocation.fromNamespaceAndPath(mod_id, block)), new Item.Properties());
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
        BLOCKS.register();
        BLOCK_ENTITIES.register();
        ITEMS.register();
    }

    @Environment(EnvType.CLIENT)
    public static abstract class ClientModBase {
        public static void init() {
            if (ClassExtractor.CLASSES.isEmpty())
                extractClasses();
            ClassExtractor.runMethodAnnotations(BlockTinter.class, (m, a)->{
                String name = a.name();
                if (name.isBlank())
                    name = StringUtils.camelToSnake(StringUtils.getSimplifiedClassName(m.clazz().getName()));
                String finalName = name;
                ColorHandlerRegistry.registerBlockColors(m::invokeStatic, ()->BLOCKS.getRegistrar().get(ResourceLocation.fromNamespaceAndPath(mod_id, finalName)));
            });
        }
    }
}
