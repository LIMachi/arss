package com.limachi.arss.neoforge.client;

import com.limachi.utils.ModBase;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = "$MOD_ID", dist = Dist.CLIENT)
public class MainNeoClient {
    public MainNeoClient() {
        ModBase.ClientModBase.init();
    }
}
