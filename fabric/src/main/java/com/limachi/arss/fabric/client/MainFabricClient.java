package com.limachi.arss.fabric.client;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.client.annotations.FabricLayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.world.level.block.Block;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.function.Supplier;

public final class MainFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModBase.ClientModBase.init();
        ModBase.extractor.runAnnotations(FabricLayer.class, null, (f, a)->{
            FabricLayer.Layers layer = FabricLayer.Layers.fromString(a.value());
            if (layer == null)
                throw new InvalidParameterException("Invalid layer name for FabricLayer: " + a.value());
            Object t = f.get();
            if (t instanceof Supplier sup) {
                if (sup.get() instanceof Block b)
                    BlockRenderLayerMap.INSTANCE.putBlock(b, layer.renderType());
                else
                    throw new InvalidParameterException("FabricLayer supplier returned unexpected type: " + t);
            } else
                throw new InvalidParameterException("Invalid field type for FabricLayer: " + f.type());
        }, (m, a)->{
            FabricLayer.Layers layer = FabricLayer.Layers.fromString(a.value());
            if (layer == null)
                throw new InvalidParameterException("Invalid layer name for FabricLayer: " + a.value());
            Object t = m.get();
            if (t instanceof Collection col) {
                for (Object o : col)
                    if (o instanceof Block b)
                        BlockRenderLayerMap.INSTANCE.putBlock(b, layer.renderType());
                    else
                        throw new InvalidParameterException("FabricLayer method returned unexpected type in collection: " + o);
            } else
                throw new InvalidParameterException("Invalid method return type for FabricLayer: " + m.returnType());
        });
    }
}
