package com.limachi.arss.blocks.redstone_wires;

import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

@StaticInit
public class AllRedstoneWires {
    public static final BlockBehaviour.Properties B_PROPS = BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE);
    public static final Item.Properties I_PROPS = new Item.Properties();

    static  {
        RedstoneWireFactory.create("enriched_redstone", B_PROPS, I_PROPS, ArssBlockStateProperties.ENRICHED_RS_RANGE, 4, 1);
        RedstoneWireFactory.create("perfected_redstone", B_PROPS, I_PROPS, ArssBlockStateProperties.PERFECTED_RS_RANGE, 32, 15);
    }
}
