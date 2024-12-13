package com.limachi.arss;

import com.limachi.arss.blocks.PixelBlock;
import com.limachi.utils.ModBase;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class Arss extends ModBase {

    public Arss() {
        RegistrySupplier<CreativeModeTab> tab = TABS.register("tab", ()->CreativeTabRegistry.create(Component.translatable("arss.tab.title"), ()->new ItemStack(Items.COMPARATOR)));
        CreativeTabRegistry.append(tab, PixelBlock.R_ITEM);
        CreativeTabRegistry.append(tab, Items.COMPARATOR);
//        CreativeTabRegistry.modify(tab, m->{
//
//        });

    }
}
