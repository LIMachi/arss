package com.limachi.arss.utils.client;

import com.limachi.arss.common.ArssItemStackComponents;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.Registries;
import com.limachi.arss.utils.client.annotations.BlockTinter;
import com.limachi.arss.utils.client.annotations.HasRedstoneTint;
import com.limachi.arss.utils.client.annotations.ItemTinter;
import com.limachi.arss.utils.client.annotations.RegisterKeyBinding;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceLocation;
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
        ModBase.extractor.runOnClasses(HasRedstoneTint.class, (c, a)->{
            if (Block.class.isAssignableFrom(c)) {
                var id = ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, Registries.defaultToClass(a.value(), c));
                ColorHandlerRegistry.registerBlockColors((s, g, p, i) -> 0xFF000000 | RedStoneWireBlock.getColorForPower(s.getValue(BlockStateProperties.POWER)), () -> ModBase.registries.blocks.getRegistrar().get(id));
            }
        });
    }

    protected void extractItemTinters() {
        ModBase.extractor.runOnMethods(ItemTinter.class, (m ,a)->{
            var id = ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, Registries.defaultToClass(a.value(), m.clazz()));
            Supplier<Item> t = ()->ModBase.registries.items.getRegistrar().get(id);
            ColorHandlerRegistry.registerItemColors((s, i)->(int)m.get(null, false, s, i), t);
        });
        ModBase.extractor.runOnClasses(HasRedstoneTint.class, (c, a)->{
            if (Item.class.isAssignableFrom(c)) {
                var id = ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, Registries.defaultToClass(a.value(), c));
                Supplier<Item> t = ()->ModBase.registries.items.getRegistrar().get(id);
                ColorHandlerRegistry.registerItemColors((s, i) -> 0xFF000000 | RedStoneWireBlock.getColorForPower(s.getOrDefault(ArssItemStackComponents.OUTPUT.get(), 0)), t);
            }
        });
    }

    protected void extractKeyBindings() {
        ModBase.extractor.runOnFields(RegisterKeyBinding.class, (f, a)->{
            if (f.get() instanceof KeyMapping key)
                KeyMappingRegistry.register(key);
            else
                ModBase.logger.error("RegisterKeyBinding used on invalid type: " + f.get());
        });
    }

    public void extractInStages() {
        extractKeyBindings();
        extractBlockTinters();
        extractItemTinters();
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
