package com.limachi.arss.items;

import com.limachi.arss.Arss;
import com.limachi.arss.Static;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import static com.limachi.arss.Registries.ITEM_REGISTER;

@SuppressWarnings("unused")
@Static
public class EpuratedRedstoneBlend extends Item {

    public static final RegistryObject<Item> R_ITEM = ITEM_REGISTER.register("epurated_redstone_blend", EpuratedRedstoneBlend::new);

    public EpuratedRedstoneBlend() {
        super(new Item.Properties().tab(Arss.ITEM_GROUP));
    }
}
