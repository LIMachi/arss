package com.limachi.arss;

import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.ModBase;
import net.minecraftforge.fml.common.Mod;

@Mod(Arss.MOD_ID)
public class Arss extends ModBase
{
    public static final KeyMapController.GlobalKeyBinding SCROLL_KEY = KeyMapController.registerKeyBind("key.hold_to_scroll", 340, "key.categories.arss");

    public static final String MOD_ID = "arss";

    public Arss() { super(MOD_ID, "Analog_Redstone_Suite", null); }

    public static Arss getInstance() { return (Arss)INSTANCES.get(MOD_ID); }
}
