package com.limachi.arss.fabric;

import com.limachi.arss.fabric.utils.CheckEnvironmentVisitor;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.reflect.AnnotationExtractor;
import net.fabricmc.api.ModInitializer;

public final class MainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBase.init(new AnnotationExtractor(ModBase.class, CheckEnvironmentVisitor::skipInvalidEnv));
    }
}
