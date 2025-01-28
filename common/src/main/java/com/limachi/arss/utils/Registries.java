package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.*;
import com.limachi.arss.utils.reflect.FieldAccess;
import com.limachi.arss.utils.reflect.MethodAccess;

import com.mojang.serialization.Codec;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Registries {
    public final Class<? extends ModBase> mod;
    public final String mod_id;
    public final DeferredRegister<DataComponentType<?>> component_types;
    public final DeferredRegister<Block> blocks;
    public final DeferredRegister<BlockEntityType<?>> block_entities;
    public final DeferredRegister<Item> items;
    public final DeferredRegister<CreativeModeTab> tabs;
    public RegistrySupplier<CreativeModeTab> default_tab = null;
    public final HashMap<Class<?>, CustomPacketPayload.Type<?>> messages = new HashMap<>();

    public <T extends IMsg<T>> void message(Class<T> clazz, Supplier<T> builder, ResourceLocation id, boolean s2c, boolean c2s) {
        if (!s2c && !c2s) {
            ModBase.logger.error("message registration without receiver declared: " + id);
            return;
        }
        var type = new CustomPacketPayload.Type<T>(id);
        messages.put(clazz, type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = CustomPacketPayload.codec(T::write, i->builder.get().read(i));
        if (s2c)
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, type, codec, T::clientWork);
        if (c2s)
            NetworkManager.registerReceiver(NetworkManager.Side.C2S, type, codec, T::serverWork);
        logRegistration("message", id);
    }

    protected String logRegistration(String kind, String id) {
        ModBase.logger.info("registered " + kind + ": " + id);
        return id;
    }

    protected ResourceLocation logRegistration(String kind, ResourceLocation id) {
        if (id.getNamespace().equals(mod_id))
            logRegistration(kind, id.getPath());
        else
            logRegistration(kind, id.toString());
        return id;
    }

    protected <T> RegistrySupplier<T> logRegistration(String kind, RegistrySupplier<T> sup) {
        logRegistration(kind, ResourceLocation.parse(sup.getRegisteredName()));
        return sup;
    }

    public Registries(String mod_id, Class<? extends ModBase> mod) {
        this.mod = mod;
        this.mod_id = mod_id;
        ModBase.logger.info("Started registration for mod: " + mod_id);
        component_types = DeferredRegister.create(mod_id, net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE);
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
        component_types.register();
        blocks.register();
        block_entities.register();
        items.register();
        tabs.register();
        ModBase.logger.info("finished common registration");
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

    public static <T, S, P> Function<P, S> defaultInstanceSupplier(Class<T> clazz, Class<S> sup, Class<P> param) {
        Constructor<T> c;
        if (!sup.isAssignableFrom(clazz)) {
            System.err.println("invalid cast from " + clazz + " to " + sup);
            System.exit(-1);
            return null;
        }
        try {
            c = clazz.getConstructor(param);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
        return p->{
            try {
                return (S)c.newInstance(p);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
                return null;
            }
        };
    }

    public static <T, S, P0, P1> BiFunction<P0, P1, S> defaultInstanceSupplier(Class<T> clazz, Class<S> sup, Class<P0> param0, Class<P1> param1) {
        Constructor<T> c;
        if (!sup.isAssignableFrom(clazz)) {
            System.err.println("invalid cast from " + clazz + " to " + sup);
            System.exit(-1);
            return null;
        }
        try {
            c = clazz.getConstructor(param0, param1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
        return (p0, p1)->{
            try {
                return (S)c.newInstance(p0, p1);
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
        var out = items.register(reg_key, n);
//        if (out != null && infoKey != null && !infoKey.isBlank())
//            JEIInfo.registerInfo(out, infoKey);
        if (tab != null && out != null)
            for (String t : tab)
                if (t != null && !t.isBlank()) {
                    RegistrySupplier<CreativeModeTab> ts;
                    if (t.equals("automatic"))
                        ts = default_tab;
                    else
                        ts = tabs.getRegistrar().delegate(ResourceLocation.parse(t));
                    if (ts != null)
                        CreativeTabRegistry.append(ts, out);
                }
        return logRegistration("item", out);
    }

    public <T> RegistrySupplier<DataComponentType<T>> component(String reg_key, Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return logRegistration("component type", component_types.register(reg_key, ()->{
            DataComponentType.Builder<T> builder = DataComponentType.builder();
            return builder.persistent(codec).networkSynchronized(streamCodec).build();
        }));
    }

    public <T extends Block> RegistrySupplier<T> block(String reg_key, Supplier<T> n) {
        return logRegistration("block", blocks.register(reg_key, n));
    }

    public <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> blockEntity(String reg_key, BlockEntityType.BlockEntitySupplier<T> n, Supplier<Block> ... blocks) {
        return logRegistration("block entity", block_entities.register(reg_key, ()->{
            Block[] b = new Block[blocks.length];
            for (int i = 0; i < blocks.length; ++i)
                b[i] = blocks[i].get();
            return BlockEntityType.Builder.of(n, b).build(null);
        }));
    }

    protected void extractItems() {
        ModBase.extractor.runOnFields(RegisterItem.class, (f, a)->{
            String name = defaultToClass(a.value(), f.clazz());
            ((FieldAccess<?, RegistrySupplier<Item>>) f).set(null, false, item(name, defaultInstanceSupplier(f.clazz(), Item.class), a.jeiInfoKey(), a.tab()));
        });
    }

    protected void extractBlocks() {
        ModBase.extractor.runOnFields(RegisterBlock.class, (f, a)->{
            String name = defaultToClass(a.value(), f.clazz());
            ((FieldAccess<?, RegistrySupplier<Block>>) f).set(null, false, block(name, defaultInstanceSupplier(f.clazz(), Block.class)));
        });
    }

    protected void extractBlockItems() {
        ModBase.extractor.runOnFields(RegisterBlockItem.class, (f, a)-> {
            String name = a.value();
            if (name.isBlank()) {
                name = defaultToClass(a.value(), f.clazz());
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
            String name = defaultToClass(a.value(), f.clazz());
            Supplier<Block>[] sba;
            if (a.blocks().length == 0) {
                var t1 = ResourceLocation.fromNamespaceAndPath(mod_id, name.replace("_block_entity", "_block"));
                var t2 = ResourceLocation.fromNamespaceAndPath(mod_id, name.replace("_block_entity", ""));
                var t3 = ResourceLocation.fromNamespaceAndPath(mod_id, name);
                sba = new Supplier[1];
                for (var r : blocks)
                    if (r.is(t1) || r.is(t2) || r.is(t3)) {
                        sba[0] = r;
                        break;
                    }
                if (sba[0] == null) {
                    ModBase.logger.error("could not find block for block entity: " + name);
                    return;
                }
            } else {
                sba = new Supplier[a.blocks().length];
                for (int i = 0; i < a.blocks().length; ++i) {
                    var n = ResourceLocation.parse(a.blocks()[i]);
                    if (n.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                        n = ResourceLocation.fromNamespaceAndPath(mod_id, a.blocks()[i]);
                    for (var r : blocks)
                        if (r.is(n)) {
                            sba[i] = r;
                            break;
                        }
                    if (sba[i] == null) {
                        ModBase.logger.error("could not find block \"" + n +  "\" for block entity: " + name);
                        return;
                    }
                }
            }
            BiFunction<BlockPos, BlockState, BlockEntity> sup = defaultInstanceSupplier(f.clazz(), BlockEntity.class, BlockPos.class, BlockState.class);
            ((FieldAccess<?, RegistrySupplier<BlockEntityType<BlockEntity>>>) f).set(null, false, blockEntity(name, sup::apply, sba));
        });
    }

    protected void extractTabs() {
        ModBase.extractor.runOnMethods(RegisterTab.class, (m, a)->{
            var t = logRegistration("tab", tabs.register(defaultToMethod(a.value(), m), ()->CreativeTabRegistry.create(c->m.get(null, false, c))));
            if (a.defaultTab())
                default_tab = t;
        });
    }

    protected void extractMsgs() {
        ModBase.extractor.runOnClasses(RegisterMsg.class, (c, a)->{
            Supplier<IMsg> builder = defaultInstanceSupplier(c, IMsg.class);
            String name = a.value();
            if (name.isBlank()) {
                name = defaultToClass(a.value(), c);
                if (name.endsWith("_msg"))
                    name = name.substring(0, name.length() - 4);
                else if (name.endsWith("_message"))
                    name = name.substring(0, name.length() - 8);
            }
            message((Class<IMsg>)c, builder, ResourceLocation.fromNamespaceAndPath(mod_id, name), a.s2c(), a.c2s());
        });
    }

    public void extractInStages() {
        synchronized (this) {
            StaticInitializer.initialize(Stage.MSG);
            extractMsgs();
            StaticInitializer.initialize(Stage.TAB);
            extractTabs();
            StaticInitializer.initialize(Stage.BLOCK);
            extractBlocks();
            StaticInitializer.initialize(Stage.ITEM);
            extractItems();
            StaticInitializer.initialize(Stage.BLOCK_ITEM);
            extractBlockItems();
            StaticInitializer.initialize(Stage.BLOCK_ENTITY);
            extractBlockEntities();
        }
    }
}
