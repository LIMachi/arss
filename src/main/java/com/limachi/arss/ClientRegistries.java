package com.limachi.arss;

import com.limachi.arss.blocks.AnalogRedstoneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ClientRegistries {
    private static final HashMap<RegistryObject<?>, RenderType> renderLayers = new HashMap<>();
    private static final HashMap<RegistryObject<Block>, BlockColor> blockColors= new HashMap<>();

    public static void setRenderLayer(RegistryObject<Block> rb, RenderType type) { renderLayers.put(rb, type); }

    public static void setColor(RegistryObject<Block> rb, BlockColor color) { blockColors.put(rb, color); }

    public static void hasRedstoneTint(RegistryObject<Block> rb) {
        setColor(rb, AnalogRedstoneBlock::getColor);
    }

    public static void isTranslucent(RegistryObject<Block> rb) {
        setRenderLayer(rb, RenderType.translucent());
    }

    public static void isCutout(RegistryObject<Block> rb) {
        setRenderLayer(rb, RenderType.cutout());
    }

    static void clientSetup(final FMLClientSetupEvent event)
    {
        for (Map.Entry<RegistryObject<?>, RenderType> entry : renderLayers.entrySet()) {
            Object o = entry.getKey().get();
            if (o instanceof Block)
                ItemBlockRenderTypes.setRenderLayer((Block)o, entry.getValue());
            if (o instanceof Fluid)
                ItemBlockRenderTypes.setRenderLayer((Fluid)o, entry.getValue());
        }

        BlockColors blockcolors = Minecraft.getInstance().getBlockColors();

        for (Map.Entry<RegistryObject<Block>, BlockColor> entry : blockColors.entrySet()) {
            blockcolors.register(entry.getValue(), entry.getKey().get());
        }
    }
}
