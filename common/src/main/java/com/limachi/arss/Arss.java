package com.limachi.arss;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.annotations.Mod;
import com.limachi.arss.utils.annotations.RegisterTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mod("arss")
public final class Arss extends ModBase {

    @RegisterTab(defaultTab = true)
    public static void tab(CreativeModeTab.Builder builder) {
        builder.title(Component.translatable("arss.tab.title"));
        builder.icon(()->new ItemStack(Items.COMPARATOR));
    }

    public Arss() {
    }
}
