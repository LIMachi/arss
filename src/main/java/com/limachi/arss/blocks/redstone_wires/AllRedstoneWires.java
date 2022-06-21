package com.limachi.arss.blocks.redstone_wires;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.ArssBlockStateProperties;
import com.limachi.arss.Static;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@Static
public class AllRedstoneWires {
    public static final BlockBehaviour.Properties B_PROPS = BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak();
    public static final Item.Properties I_PROPS = new Item.Properties().tab(Arss.ITEM_GROUP);

    static  {
        RedstoneWireFactory.create("enriched_redstone", B_PROPS, I_PROPS, ArssBlockStateProperties.ENRICHED_RS_RANGE, 4, 1);
        RedstoneWireFactory.create("perfected_redstone", B_PROPS, I_PROPS, ArssBlockStateProperties.PERFECTED_RS_RANGE, 32, 15);
    }
}
