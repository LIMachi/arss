package com.limachi.arss;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Client1_18_2 {
    private static final HashMap<RegistryObject<?>, RenderType> renderLayers = new HashMap<>();

    public static void setRenderLayer(RegistryObject<Block> rb, RenderType type) { renderLayers.put(rb, type); }

    public static void isTranslucent(RegistryObject<Block> rb) {
        setRenderLayer(rb, RenderType.translucent());
    }

    public static void isCutout(RegistryObject<Block> rb) {
        setRenderLayer(rb, RenderType.cutout());
    }

    @SubscribeEvent
    static void clientSetup(final FMLClientSetupEvent event)
    {
        for (Map.Entry<RegistryObject<?>, RenderType> entry : renderLayers.entrySet()) {
            Object o = entry.getKey().get();
            if (o instanceof Block)
                ItemBlockRenderTypes.setRenderLayer((Block)o, entry.getValue());
            if (o instanceof Fluid)
                ItemBlockRenderTypes.setRenderLayer((Fluid)o, entry.getValue());
        }
    }
}
