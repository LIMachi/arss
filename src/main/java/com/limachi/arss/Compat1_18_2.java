package com.limachi.arss;

import com.limachi.arss.blocks.*;
import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.arss.blocks.redstone_wires.RedstoneWireFactory;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

public class Compat1_18_2 {
    public static RegistryObject<Block> isCutout(RegistryObject<Block> rb) {
        return DistExecutor.unsafeRunForDist(()->()->{Client1_18_2.isCutout(rb); return rb;}, ()->()->rb);
    }

    @StaticInit(Stage.BLOCK)
    public static void registerBlockCutouts() {
        DiodeBlockFactory.iter().forEachRemaining(e->isCutout(e.getValue().getSecond()));
        RedstoneWireFactory.iter().forEachRemaining(e->isCutout(e.getValue().getSecond()));
        isCutout(AnalogJukeboxBlock.R_BLOCK);
        isCutout(AnalogNoteBlock.R_BLOCK);
        isCutout(AnalogRedstoneBlock.R_BLOCK);
        isCutout(AnalogRedstoneLampBlock.R_BLOCK);
        isCutout(AnalogRedstoneTorchBlock.R_BLOCK);
        isCutout(AnalogRedstoneTorchBlock.AnalogRedstoneWallTorchBlock.R_BLOCK);
    }
}
