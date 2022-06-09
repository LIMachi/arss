package com.limachi.arss;

import com.google.common.reflect.Reflection;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UnstableApiUsage"})
@Mod(Arss.MOD_ID)
public class Arss
{
    public static final String MOD_ID = "arss";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab("tab_" + MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon() { return new ItemStack(Items.COMPARATOR); }
    };

    static {
        Type type = Type.getType(Static.class);
        for (ModFileScanData.AnnotationData data : ModList.get().getAllScanData().stream().map(ModFileScanData::getAnnotations).flatMap(Collection::stream).filter(a-> type.equals(a.annotationType())).collect(Collectors.toList())) {
            try {
                Reflection.initialize(Class.forName(data.clazz().getClassName()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public Arss()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Registries::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        Registries.registerAll(bus);
    }
}
