package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.*;
import com.limachi.arss.utils.reflect.AnnotationExtractor;
import com.limachi.arss.utils.reflect.ClassExtractor;
import com.limachi.arss.utils.reflect.FieldAccess;
import com.limachi.arss.utils.reflect.MethodAccess;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class Registries {
    public final Class<? extends ModBase> mod;
    public final String mod_id;
    public final DeferredRegister<Block> blocks;
    public final DeferredRegister<BlockEntityType<?>> block_entities;
    public final DeferredRegister<Item> items;
    public final DeferredRegister<CreativeModeTab> tabs;
    public RegistrySupplier<CreativeModeTab> default_tab;

    public Registries(String mod_id, Class<? extends ModBase> mod) {
        this.mod = mod;
        this.mod_id = mod_id;
        blocks = DeferredRegister.create(mod_id, net.minecraft.core.registries.Registries.BLOCK);
        block_entities = DeferredRegister.create(mod_id, net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE);
        items = DeferredRegister.create(mod_id, net.minecraft.core.registries.Registries.ITEM);
        tabs = DeferredRegister.create(mod_id, net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB);
    }

    public <T extends ModBase> T initMod() {
        try {
            return (T)mod.newInstance();
        } catch (Exception e) {
            return null; //FIXME
        }
    }

    public void register() {
        blocks.register();
        block_entities.register();
        items.register();
        tabs.register();
    }

    public static String defaultToClass(String nullable, Class<?> clazz) {
        if (nullable == null || nullable.isBlank())
            return StringUtils.camelToSnake(StringUtils.getSimplifiedClassName(clazz.getName()));
        return nullable;
    }

    public static String defaultToMethod(String nullable, MethodAccess<?, ?> m) {
        if (nullable == null || nullable.isBlank())
            return StringUtils.camelToSnake(m.name());
        return nullable;
    }

    public static String defaultToField(String nullable, FieldAccess<?, ?> f) {
        if (nullable == null || nullable.isBlank())
            return StringUtils.camelToSnake(f.name());
        return nullable;
    }

    public static <T, S> Supplier<S> defaultInstanceSupplier(Class<T> clazz, Class<S> sup) {
        Constructor<T> c;
        if (!sup.isAssignableFrom(clazz)) {
            System.err.println("invalid cast from " + clazz + " to " + sup);
            System.exit(-1);
            return null;
        }
        try {
            c = clazz.getConstructor();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
        return ()->{
            try {
                return (S)c.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
                return null;
            }
        };
    }

    public static final String[] DEFAULT_TABS = new String[]{"automatic"};

    public <T extends Item> RegistrySupplier<T> item(String reg_key, Supplier<T> n, String infoKey) {
        return item(reg_key, n, infoKey, DEFAULT_TABS);
    }

    public <T extends Item> RegistrySupplier<T> item(String reg_key, Supplier<T> n) {
        return item(reg_key, n, null, DEFAULT_TABS);
    }

    public <T extends Item> RegistrySupplier<T> item(String reg_key, Supplier<T> n, String[] tab) {
        return item(reg_key, n, null, tab);
    }

    public <T extends Item> RegistrySupplier<T> item(String reg_key, Supplier<T> n, String infoKey, String[] tab) {
        RegistrySupplier<T> out = items.register(reg_key, n);
//        if (out != null && infoKey != null && !infoKey.isBlank())
//            JEIInfo.registerInfo(out, infoKey);
        if (tab != null && out != null)
            for (String t : tab)
                if (t != null && !t.isBlank()) {
                    RegistrySupplier<CreativeModeTab> ts;
                    if (t.equals("automatic"))
                        ts = getDefaultTab();
                    else
                        ts = tabs.getRegistrar().delegate(ResourceLocation.parse(t));
                    if (ts != null)
                        CreativeTabRegistry.append(ts, out);
                }
        return out;
    }

    public <T extends Block> RegistrySupplier<T> block(String reg_key, Supplier<T> n) {
        return blocks.register(reg_key, n);
    }

    public <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> blockEntity(String reg_key, BlockEntityType.BlockEntitySupplier<T> n, Supplier<? extends Block> ... blocks) {
        return block_entities.register(reg_key, ()->{
            Block[] b = new Block[blocks.length];
            for (int i = 0; i < blocks.length; ++i)
                b[i] = blocks[i].get();
            return BlockEntityType.Builder.of(n, b).build(null);
        });
    }

    protected void extractBlocks() {
        ModBase.extractor.runOnFields(RegisterBlock.class, (f, a)->{
            String name = defaultToClass(a.name(), f.clazz());
            ((FieldAccess<?, RegistrySupplier<Block>>) f).set(null, false, block(name, defaultInstanceSupplier(f.clazz(), Block.class)));
        });
    }

    protected void extractItems() {
        ModBase.extractor.runOnFields(RegisterItem.class, (f, a)->{
            String name = defaultToClass(a.name(), f.clazz());
            ((FieldAccess<?, RegistrySupplier<Item>>) f).set(null, false, item(name, defaultInstanceSupplier(f.clazz(), Item.class), a.jeiInfoKey(), a.tab()));
        });
    }

    protected void extractBlockItems() {
        ModBase.extractor.runOnFields(RegisterBlockItem.class, (f, a)-> {
            String name = a.name();
            if (name.isBlank()) {
                name = defaultToClass(a.name(), f.clazz());
                if (name.endsWith("_block"))
                    name = name.substring(0, name.length() - 6) + "_item";
            }
            String block = defaultToClass(a.block(), f.clazz());
            ((FieldAccess<?, RegistrySupplier<BlockItem>>) f).set(null, false, item(name,
                    ()-> new BlockItem(blocks.getRegistrar().get(ResourceLocation.fromNamespaceAndPath(mod_id, block)), new Item.Properties()),
                    a.jeiInfoKey(), a.tab()));
        });
    }

    protected void extractBlockEntities() {
        ModBase.extractor.runOnFields(RegisterBlockEntity.class, (f, a)->{

        });
    }

    protected void extractTabs() {
        ModBase.extractor.runOnMethods(RegisterTab.class, (m, a)->{
            var t = tabs.register(defaultToMethod(a.name(), m), ()->CreativeTabRegistry.create(c->m.get(null, false, c)));
            if (a.defaultTab())
                default_tab = t;
        });
    }

    public RegistrySupplier<CreativeModeTab> getDefaultTab() {
        if (default_tab == null)
            default_tab = tabs.register("tab", CreativeTabRegistry.ofBuiltin(CreativeModeTabs.getDefaultTab()));
        return default_tab;
    }

    public void extractInStages() {
        StaticInitializer.initialize(Stage.BLOCK);
        extractBlocks();
        StaticInitializer.initialize(Stage.ITEM);
        extractItems();
        StaticInitializer.initialize(Stage.BLOCK_ITEM);
        extractBlockItems();

        //...
        StaticInitializer.initialize(Stage.BLOCK_ENTITY);
        //...

        extractTabs();
    }
}
