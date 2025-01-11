package com.limachi.arss.utils;

import com.limachi.arss.utils.client_annotations.BlockTinter;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ClientRegistries {

    protected void extractBlockTinters() {
        ModBase.extractor.runOnMethods(BlockTinter.class, (m, a)->{
            String name = a.name();
            if (name.isBlank())
                name = StringUtils.camelToSnake(StringUtils.getSimplifiedClassName(m.clazz().getName()));
            String finalName = name;
            ColorHandlerRegistry.registerBlockColors(m::invokeStatic, ()->ModBase.registries.blocks.getRegistrar().get(ResourceLocation.fromNamespaceAndPath(ModBase.registries.mod_id, finalName)));
        });
    }

    public void extractInStages() {
        extractBlockTinters();
    }

    public void register() {

    }
}
