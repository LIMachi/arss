package com.limachi.arss.fabric.client;

import com.limachi.arss.utils.ModBase;
import net.fabricmc.api.ClientModInitializer;

public final class MainFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModBase.ClientModBase.init();
    }
}
