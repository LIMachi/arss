package com.limachi.arss.utils.client;

import com.limachi.arss.common.ArssItemStackComponents;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.Registries;
import com.limachi.arss.utils.StaticInitializer;
import com.limachi.arss.utils.client.annotations.*;
import com.limachi.arss.utils.reflect.ReflectUtils;

import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.RegistrySupplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ClientRegistries {
    protected HashMap<ResourceLocation, BlockColor> blockTints = new HashMap<>();
    protected HashMap<ResourceLocation, ItemColor> itemTints = new HashMap<>();

    public void registerBlockTint(BlockColor blockTint, ResourceLocation ... ids) {
        for (var id : ids)
            blockTints.put(id, blockTint);
    }

    public void registerItemTint(ItemColor itemTint, ResourceLocation ... ids) {
        for (var id : ids)
            itemTints.put(id, itemTint);
    }

    protected void extractBlockTinters() {
        ModBase.extractor.runOnMethods(BlockTinter.class, (m, a)->{
            var id = ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, Registries.defaultToClass(a.value(), m.clazz()));
            ColorHandlerRegistry.registerBlockColors((s, g, p, i)->(int)m.get(null, false, s, g, p, i), ()->ModBase.registries.blocks.getRegistrar().get(id));
        });
        ModBase.extractor.runAnnotations(HasRedstoneTint.class, (c, a)->{
            if (Block.class.isAssignableFrom(c)) {
                var id = ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, Registries.defaultToClass(a.value(), c));
                Supplier<Block> t = ()->ModBase.registries.blocks.getRegistrar().get(id);
                ColorHandlerRegistry.registerBlockColors((s, g, p, i) -> 0xFF000000 | RedStoneWireBlock.getColorForPower(s.getValue(BlockStateProperties.POWER)), t);
            }
        }, (f, a)->{
            if (f.get() instanceof RegistrySupplier<?> rs && rs.getRegistrar().key().location().getPath().equals("block"))
                ColorHandlerRegistry.registerBlockColors((s, g, p, i) -> 0xFF000000 | RedStoneWireBlock.getColorForPower(s.getValue(BlockStateProperties.POWER)), (RegistrySupplier<Block>)rs);
        }, null);
    }

    protected void extractItemTinters() {
        ModBase.extractor.runOnMethods(ItemTinter.class, (m ,a)->{
            var id = ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, Registries.defaultToClass(a.value(), m.clazz()));
            Supplier<Item> t = ()->ModBase.registries.items.getRegistrar().get(id);
            ColorHandlerRegistry.registerItemColors((s, i)->(int)m.get(null, false, s, i), t);
        });
        ModBase.extractor.runAnnotations(HasRedstoneTint.class, (c, a)->{
            if (Item.class.isAssignableFrom(c)) {
                var id = ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, Registries.defaultToClass(a.value(), c));
                Supplier<Item> t = ()->ModBase.registries.items.getRegistrar().get(id);
                ColorHandlerRegistry.registerItemColors((s, i) -> 0xFF000000 | RedStoneWireBlock.getColorForPower(s.getOrDefault(ArssItemStackComponents.OUTPUT.get(), 9)), t);
            }
        }, (f, a)->{
            if (f.get() instanceof RegistrySupplier<?> rs && rs.getRegistrar().key().location().getPath().equals("item"))
                ColorHandlerRegistry.registerItemColors((s, i) -> 0xFF000000 | RedStoneWireBlock.getColorForPower(s.getOrDefault(ArssItemStackComponents.OUTPUT.get(), 9)), (RegistrySupplier<Item>)rs);
        }, null);
    }

    protected void extractKeyBindings() {
        ModBase.extractor.runOnFields(RegisterKeyBinding.class, (f, a)->{
            if (f.get() instanceof KeyMapping key)
                KeyMappingRegistry.register(key);
            else
                ModBase.logger.error("RegisterKeyBinding used on invalid type: " + f.get());
        });
    }

    @Environment(EnvType.CLIENT)
    protected record ErasedMenuScreen<M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>>(RegistrySupplier<MenuType<M>> menu, Class<S> screen) {
        @Environment(EnvType.CLIENT)
        private class Factory implements MenuRegistry.ScreenFactory<M, S> {
            @Override
            public S create(M containerMenu, Inventory inventory, Component component) {
                return ReflectUtils.nullableInstance(screen, containerMenu, inventory, component);
            }
        }
        void register() { MenuRegistry.registerScreenFactory(menu.get(), new Factory()); }
    }

    protected <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void extractMenuScreens() {
        ModBase.extractor.runOnClasses(RegisterMenuScreen.class, (c, a)->{
            String name = Registries.defaultToClass(a.value(), c);
            new ErasedMenuScreen<>(Registries.searchRegistry(ModBase.registries.menus, ModBase.registries.mod_id + ":" + name), (Class<S>)c).register();
        });
    }

    protected static void stage(ClientStage stage, Runnable run) {
        StaticInitializer.initialize(stage, true);
        run.run();
        StaticInitializer.initialize(stage, false);
    }

    public void extractInStages() {
        synchronized (this) {
            stage(ClientStage.SCREEN, this::extractMenuScreens);
            stage(ClientStage.KEY_BINDING, this::extractKeyBindings);
            stage(ClientStage.BLOCK_TINTER, this::extractBlockTinters);
            stage(ClientStage.ITEM_TINTER, this::extractItemTinters);
        }
    }

    public void register() {
        for (var e : blockTints.entrySet())
            ColorHandlerRegistry.registerBlockColors(e.getValue(), () -> ModBase.registries.blocks.getRegistrar().get(e.getKey()));
        for (var e : itemTints.entrySet()) {
            Supplier<Item> t = ()->ModBase.registries.items.getRegistrar().get(e.getKey());
            ColorHandlerRegistry.registerItemColors(e.getValue(), t);
        }
    }
}
