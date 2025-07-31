package com.limachi.arss;

import com.limachi.lim_lib.InstancedMod;
import com.limachi.lim_lib.common.annotations.ModInstance;
import com.limachi.lim_lib.common.annotations.RegisterTab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class Arss {
    @ModInstance
    public static InstancedMod INSTANCE;

    public static TagKey<Item> WRENCH = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("arss", "arss_wrench"));

    public static boolean isWrench(ItemStack stack) { return stack.is(Arss.WRENCH); }

    @RegisterTab(defaultTab = true)
    public static void tab(CreativeModeTab.Builder builder) {
        builder.title(Component.translatable("arss.tab.title"));
        builder.icon(()->new ItemStack(Items.COMPARATOR));
    }
}
