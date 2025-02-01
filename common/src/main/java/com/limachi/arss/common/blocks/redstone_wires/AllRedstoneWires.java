package com.limachi.arss.common.blocks.redstone_wires;

import com.limachi.arss.common.ArssBlockStateProperties;
import com.limachi.arss.utils.Stage;
import com.limachi.arss.utils.annotations.StaticInit;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

@SuppressWarnings("unused")
public class AllRedstoneWires {
    public static final BlockBehaviour.Properties B_PROPS = BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE);
    public static final Item.Properties I_PROPS = new Item.Properties();

    @StaticInit(Stage.BLOCK)
    public static void registerWires() {
        RedstoneWireFactory.create("enriched_redstone", B_PROPS, I_PROPS, ArssBlockStateProperties.ENRICHED_RS_RANGE, 4, 1);
        RedstoneWireFactory.create("perfected_redstone", B_PROPS, I_PROPS, ArssBlockStateProperties.PERFECTED_RS_RANGE, 32, 15);
    }
}
