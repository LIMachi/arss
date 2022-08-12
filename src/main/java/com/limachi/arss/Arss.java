package com.limachi.arss;

import com.limachi.arss.utils.StaticInitializer;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@SuppressWarnings("unused")
@Mod(Arss.MOD_ID)
public class Arss
{
    public static final String MOD_ID = "arss";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab("tab_" + MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon() { return new ItemStack(Items.COMPARATOR); }
    };

    public Arss()
    {
        StaticInitializer.initialize();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.unsafeRunForDist(()->()->{
            StaticInitializer.initializeClient();
            bus.addListener(ClientRegistries::clientSetup);
            return 0;}, ()->()->0);
        MinecraftForge.EVENT_BUS.register(this);
        Registries.registerAll(bus);
        Configs.register(MOD_ID, "Analog_Redstone_Suite");
    }
}
