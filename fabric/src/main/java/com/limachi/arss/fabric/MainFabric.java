package com.limachi.arss.fabric;

import com.limachi.lim_lib.common.annotations.Loader;
import com.limachi.lim_lib.common.modCreation.Loaders;

import com.limachi.lim_lib.fabric.FabricEntryPoint;
import com.limachi.lim_lib.fabric.annotations.FabricMod;

@Loader(Loaders.Fabric)
@FabricMod("arss")
public final class MainFabric extends FabricEntryPoint {
    @Override
    protected String commonRootPackage() {
        return "com.limachi.arss";
    }
}
