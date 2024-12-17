package com.limachi.arss.utils;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

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

    public <T extends Item> RegistrySupplier<T> item(String reg_key, Supplier<T> n, String infoKey, String[] tab) {
        RegistrySupplier<T> out = items.register(reg_key, n);
//        if (out != null && infoKey != null && !infoKey.isBlank())
//            JEIInfo.registerInfo(out, infoKey);
        if (tab != null && out != null)
            for (String t : tab)
                if (t != null && !t.isBlank()) {
                    RegistrySupplier<CreativeModeTab> ts;
                    if (t.equals("automatic") && default_tab !=  null)
                        ts = default_tab;
                    else
                        ts = tabs.getRegistrar().delegate(ResourceLocation.parse(t));
                    if (ts != null)
                        CreativeTabRegistry.append(ts, out);
                }
        return out;
    }
}
