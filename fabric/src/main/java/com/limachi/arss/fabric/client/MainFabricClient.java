package com.limachi.arss.fabric.client;

import com.limachi.utils.ModBase;
import net.fabricmc.api.ClientModInitializer;

public final class MainFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModBase.ClientModBase.init();
    }
}
