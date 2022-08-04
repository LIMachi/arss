package com.limachi.arss;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Style;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class Registries {
    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Arss.MOD_ID);
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Arss.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Arss.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, Arss.MOD_ID);

    public static RegistryObject<Block> block(String regKey, Supplier<Block> blockNew) { return BLOCK_REGISTER.register(regKey, blockNew); }
    public static RegistryObject<Item> item(String regKey, Supplier<Item> itemNew) { return ITEM_REGISTER.register(regKey, itemNew); }
    public static RegistryObject<BlockEntityType<?>> blockEntity(String regKey, BlockEntityType.BlockEntitySupplier<?> beNew, RegistryObject<Block> block) { return blockEntity(regKey, beNew, block, null); }
    public static RegistryObject<BlockEntityType<?>> blockEntity(String regKey, BlockEntityType.BlockEntitySupplier<?> beNew, RegistryObject<Block> block, com.mojang.datafixers.types.Type<?> fixer) { return BLOCK_ENTITY_REGISTER.register(regKey, ()->BlockEntityType.Builder.of(beNew, block.get()).build(fixer)); }

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

    public static RegistryObject<Block> hasRedstoneTint(RegistryObject<Block> rb) {
        return DistExecutor.unsafeRunForDist(()->()->{ClientRegistries.hasRedstoneTint(rb); return rb;}, ()->()->rb);
    }

    public static RegistryObject<Block> isTranslucent(RegistryObject<Block> rb) {
        return DistExecutor.unsafeRunForDist(()->()->{ClientRegistries.isTranslucent(rb); return rb;}, ()->()->rb);
    }

    public static RegistryObject<Block> isCutout(RegistryObject<Block> rb) {
        return DistExecutor.unsafeRunForDist(()->()->{ClientRegistries.isCutout(rb); return rb;}, ()->()->rb);
    }

    public static Pair<RegistryObject<Item>, RegistryObject<Block>> registerBlockAndItem(String regKey, Supplier<Block> blockSup) {
        RegistryObject<Block> r_block = BLOCK_REGISTER.register(regKey, blockSup);
        return Pair.of(ITEM_REGISTER.register(regKey, blockItem(r_block, "tooltip.help." + regKey)), r_block);
    }

    static void registerAll(IEventBus bus) {
        BLOCK_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        BLOCK_ENTITY_REGISTER.register(bus);
        MENU_REGISTER.register(bus);
    }
}
