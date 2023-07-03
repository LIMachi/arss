package com.limachi.arss.integration;

import com.limachi.arss.Arss;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Arss.MOD_ID)
public class TOP_IMC {
    public static final String MODID = "theoneprobe";

    @SubscribeEvent
    public static void imcQueue(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded(MODID))
            InterModComms.sendTo(MODID, "getTheOneProbe", TOPplugin::new);
    }
}
