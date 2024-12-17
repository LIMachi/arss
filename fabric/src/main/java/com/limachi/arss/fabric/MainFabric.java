package com.limachi.arss.fabric;

import com.limachi.arss.utils.ModBase;
import net.fabricmc.api.ModInitializer;

public final class MainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBase.init();
    }
}
