package com.limachi.arss;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Style;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class Registries {
    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Arss.MOD_ID);
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Arss.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Arss.MOD_ID);

    private static final HashMap<RegistryObject<?>, RenderType> renderLayers = new HashMap<>();
    private static final HashMap<RegistryObject<Block>, BlockColor> blockColors= new HashMap<>();

    public static Supplier<BlockItem> blockItem(RegistryObject<Block> blockReg) { return blockItem(blockReg, new Item.Properties().tab(Arss.ITEM_GROUP), ""); }
    public static Supplier<BlockItem> blockItem(RegistryObject<Block> blockReg, Item.Properties props) { return blockItem(blockReg, props, ""); }
    public static Supplier<BlockItem> blockItem(RegistryObject<Block> blockReg, String tooltip) { return blockItem(blockReg, new Item.Properties().tab(Arss.ITEM_GROUP), tooltip); }
    public static Supplier<BlockItem> blockItem(RegistryObject<Block> blockReg, Item.Properties props, String tooltip) {
        return ()->new BlockItem(blockReg.get(), props) {
            @Override
            public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flags) {
                super.appendHoverText(stack, level, components, flags);
                if (!tooltip.equals("")) {
                    if (Screen.hasShiftDown())
                        components.add(new TranslatableComponent(tooltip).setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
                    else
                        components.add(new TranslatableComponent("tooltip.help.press_shift_for_help").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
                }
            }
        };
    }

    public static Pair<RegistryObject<Item>, RegistryObject<Block>> registerBlockAndItem(String regKey, Supplier<Block> blockSup) {
        RegistryObject<Block> r_block = BLOCK_REGISTER.register(regKey, blockSup);
        return Pair.of(ITEM_REGISTER.register(regKey, blockItem(r_block, "tooltip.help." + regKey)), r_block);
    }

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
