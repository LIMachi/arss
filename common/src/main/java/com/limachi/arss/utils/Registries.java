package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.*;
import com.limachi.arss.utils.codec.CodecUtils;
import com.limachi.arss.utils.network.ClassMsg;
import com.limachi.arss.utils.network.IC2SMsg;
import com.limachi.arss.utils.network.IMsg;
import com.limachi.arss.utils.network.IS2CMsg;
import com.limachi.arss.utils.reflect.FieldAccess;
import com.limachi.arss.utils.reflect.MethodAccess;
import com.limachi.arss.utils.reflect.Utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Registries {
    public final Class<? extends ModBase> mod;
    public final String mod_id;
    public final DeferredRegister<DataComponentType<?>> component_types;
    public final DeferredRegister<Block> blocks;
    public final DeferredRegister<BlockEntityType<?>> block_entities;
    public final DeferredRegister<Item> items;
    public final DeferredRegister<MenuType<?>> menus;
    public final DeferredRegister<CreativeModeTab> tabs;
    public RegistrySupplier<CreativeModeTab> default_tab = null;
    public final HashMap<Class<?>, Pair<CustomPacketPayload.Type<?>, CustomPacketPayload.Type<?>>> messages = new HashMap<>();

    public <T extends IMsg<T>> CustomPacketPayload.Type<T> getMessageType(IMsg<T> msg) {
        return (CustomPacketPayload.Type<T>) Optional.ofNullable(messages.get(msg.getClass())).map(p->msg.upstream() ? p.getFirst() : p.getSecond()).orElse(null);
    }

    protected <T extends IMsg<T>> boolean messageHasReceiver(Class<T> msg) {
        return IS2CMsg.class.isAssignableFrom(msg) || IC2SMsg.class.isAssignableFrom(msg);
    }

    protected <T extends IMsg<T>> void registerMessageReceivers(Class<T> msg, ResourceLocation id, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        boolean s2c = IS2CMsg.class.isAssignableFrom(msg);
        boolean c2s = IC2SMsg.class.isAssignableFrom(msg);
        CustomPacketPayload.Type<T> s2cType = s2c ? new CustomPacketPayload.Type<>(c2s ? ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_s2c") : id) : null;
        CustomPacketPayload.Type<T> c2sType = c2s ? new CustomPacketPayload.Type<>(s2c ? ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_c2s") : id) : null;
        if (s2c)
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, s2cType, codec, T::run);
        if (c2s)
            NetworkManager.registerReceiver(NetworkManager.Side.C2S, c2sType, codec, T::run);
        messages.put(msg, new Pair<>(c2sType, s2cType));
    }

    public <T extends IMsg<T>> void recordMessage(Class<T> clazz, ResourceLocation id) {
        if (!messageHasReceiver(clazz)) {
            ModBase.logger.error("message registration without receiver declared (should extend one or more of IS2CMsg/IC2SMsg): " + id);
            return;
        }
        if (!Record.class.isAssignableFrom(clazz)) {
            ModBase.logger.error("recordMessage registration called for non record object: " + clazz);
            return;
        }
        registerMessageReceivers(clazz, id, CodecUtils.autoStreamCodec(clazz));
        logRegistration("message", id);
    }

    public <T extends ClassMsg<T>> void dynamicMessage(Class<T> clazz, ResourceLocation id) {
        if (!messageHasReceiver(clazz)) {
            ModBase.logger.error("message registration without receiver declared (should extend one or more of IS2CMsg/IC2SMsg): " + id);
            return;
        }
        registerMessageReceivers(clazz, id, CustomPacketPayload.codec(ClassMsg::write, i -> Utils.unsafeInstance(clazz).read(i)));
        logRegistration("message", id);
    }

    protected void error(String error) {
        ModBase.logger.error(error);
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
        menus = DeferredRegister.create(mod_id, net.minecraft.core.registries.Registries.MENU);
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
        menus.register();
        tabs.register();
        ModBase.logger.info("finished common registration");
    }

    public static Comparator<String> longestString = Comparator.comparing(String::length).reversed().thenComparing(Comparator.naturalOrder());
    public static HashMap<Class<?>, TreeSet<String>> DISCARD_SUFFIXES = new HashMap<>();

    public static void addDiscardSuffixes(Class<?> clazz, String ... suffixes) {
        DISCARD_SUFFIXES.compute(clazz, (k, v)->{
            if (v == null)
                v = new TreeSet<>(longestString);
            v.addAll(List.of(suffixes));
            return v;
        });
    }

    static {
//        addDiscardSuffixes(Item.class, "_item", "_block_item", "_block", "_block_entity", "_be", "_b_e", "_i", "_b");
//        addDiscardSuffixes(Block.class, "_item", "_block_item", "_block", "_block_entity", "_be", "_b_e", "_i", "_b");
        addDiscardSuffixes(BlockEntity.class, "_block_entity", "_be", "_b_e");
        addDiscardSuffixes(IMsg.class, "_msg", "_message");
        addDiscardSuffixes(AbstractContainerMenu.class, "_menu", "_screen", "_menu_screen");
        addDiscardSuffixes(Screen.class, "_menu", "_screen", "_menu_screen");
    }

    public static String discardSuffixes(Class<?> clazz, String input) {
        for (var e : DISCARD_SUFFIXES.entrySet())
            if (e.getKey().isAssignableFrom(clazz)) {
                boolean discard = true;
                while (discard) {
                    discard = false;
                    for (String suffix : e.getValue())
                        if (input.endsWith(suffix)) {
                            discard = true;
                            input = input.substring(0, input.length() - suffix.length());
                        }
                }
            }
        return input;
    }

    public static String defaultToClass(String nullable, Class<?> clazz) {
        if (nullable == null || nullable.isBlank())
            nullable = discardSuffixes(clazz, StringUtils.camelToSnake(StringUtils.getSimplifiedClassName(clazz.getName())));
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
            String name = defaultToClass(a.value(), f.clazz());
            String block = defaultToClass(a.block(), f.clazz());
            ((FieldAccess<?, RegistrySupplier<BlockItem>>) f).set(null, false, item(name,
                    ()-> new BlockItem(blocks.getRegistrar().get(ResourceLocation.fromNamespaceAndPath(mod_id, block)), new Item.Properties()),
                    a.jeiInfoKey(), a.tab()));
            f.type();
        });
    }

    public static <R, T extends R> RegistrySupplier<T> searchRegistry(DeferredRegister<R> register, String regex) {
        var pattern = Pattern.compile(regex);
        for (var r : register)
            if (pattern.matcher(r.getId().toString()).matches())
                return (RegistrySupplier<T>) r;
        return null;
    }

    protected void extractBlockEntities() {
        ModBase.extractor.runOnFields(RegisterBlockEntity.class, (f, a)->{
            String name = defaultToClass(a.value(), f.clazz());
            Supplier<Block>[] sba;
            if (a.blocks().length == 0) {
                sba = new Supplier[1];
                sba[0] = searchRegistry(blocks, mod_id + ":" + name);
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
            if (!IMsg.class.isAssignableFrom(c)) {
                error("@RegisterMsg not on a class/record that extend/implement ClassMsg/IMsg" + c);
                return;
            }
            String name = defaultToClass(a.value(), c);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(mod_id, name);
            if (Record.class.isAssignableFrom(c))
                recordMessage((Class<IMsg>)c, id);
            else if (ClassMsg.class.isAssignableFrom(c))
                dynamicMessage((Class<ClassMsg>)c, id);
            else
                error("@RegisterMsg not on a class/record that extend/implement ClassMsg/IMsg" + c);
        });
    }

    public <T extends AbstractContainerMenu> RegistrySupplier<MenuType<T>> menu(String reg_key, MenuRegistry.ExtendedMenuTypeFactory<T> builder) {
        return logRegistration("menu", menus.register(reg_key, ()->MenuRegistry.ofExtended(builder)));
    }

    protected void extractMenus() {
        ModBase.extractor.runOnFields(RegisterMenu.class, (f, a)->{
            final Constructor<AbstractContainerMenu> ctr = (Constructor<AbstractContainerMenu>) Utils.getMatchingConstructor(f.clazz(), int.class, Inventory.class, RegistryFriendlyByteBuf.class);
            ((FieldAccess<?, RegistrySupplier<MenuType<AbstractContainerMenu>>>)f).set(null, false, menu(defaultToClass(a.value(), f.clazz()), (id, inventory, buf) -> Utils.nullableInstance(ctr, id, inventory, buf)));
        });
    }

    protected static void stage(Stage stage, Runnable run) {
        StaticInitializer.initialize(stage, true);
        run.run();
        StaticInitializer.initialize(stage, false);
    }

    public void extractInStages() {
        synchronized (this) {
            stage(Stage.MSG, this::extractMsgs);
            stage(Stage.TAB, this::extractTabs);
            stage(Stage.BLOCK, this::extractBlocks);
            stage(Stage.ITEM, this::extractItems);
            stage(Stage.BLOCK_ITEM, this::extractBlockItems);
            stage(Stage.BLOCK_ENTITY, this::extractBlockEntities);
            stage(Stage.MENU, this::extractMenus);
        }
    }
}
