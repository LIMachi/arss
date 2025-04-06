package com.limachi.arss.neoforge;

import com.limachi.arss.neoforge.utils.CheckDistVisitor;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.reflect.AnnotationExtractor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@Mod("arss")
public final class MainNeo {
    public MainNeo() { ModBase.init(new AnnotationExtractor(ModBase.class, CheckDistVisitor::skipInvalidEnv)); }

    @EventBusSubscriber(value = Dist.CLIENT, modid = "arss", bus = EventBusSubscriber.Bus.MOD)
    public static class Client {
        @SubscribeEvent
        public static void init(RenderLevelStageEvent.RegisterStageEvent event) { ModBase.ClientModBase.init(); }
    }


}
