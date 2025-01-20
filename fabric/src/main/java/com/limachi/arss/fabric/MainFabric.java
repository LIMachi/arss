package com.limachi.arss.fabric;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.reflect.AnnotationExtractor;
import net.fabricmc.api.ModInitializer;

public final class MainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBase.init(new AnnotationExtractor(ModBase.class/*, MainFabric::skipInvalidEnv*/));
    }

    /*
    public static boolean skipInvalidEnv(ClassReader cr) {
        return false;
    }
     */
}
