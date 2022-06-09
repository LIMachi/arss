package com.limachi.arss;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Registries {
    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Arss.MOD_ID);
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Arss.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Arss.MOD_ID);

    private static final HashMap<RegistryObject<?>, RenderType> renderLayers = new HashMap<>();
    private static final HashMap<RegistryObject<Block>, BlockColor> blockColors= new HashMap<>();

    public static void setRenderLayer(RegistryObject<Block> rb, RenderType type) { renderLayers.put(rb, type); }

    public static void setColor(RegistryObject<Block> rb, BlockColor color) { blockColors.put(rb, color); }

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

    static void registerAll(IEventBus bus) {
        BLOCK_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        BLOCK_ENTITY_REGISTER.register(bus);
    }
}
