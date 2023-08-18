package com.limachi.arss;

import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.ModBase;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod(Arss.MOD_ID)
public class Arss extends ModBase {

    public static final KeyMapController.GlobalKeyBinding SCROLL_KEY = KeyMapController.registerKeyBind("key.hold_to_scroll", 340, "key.categories.arss");
    public static final String MOD_ID = "arss";

    public static void commonHoverText(String name, List<Component> components) {
        if (Screen.hasShiftDown()) {
            Component c3;
            if (SCROLL_KEY.getKeybinding().isUnbound())
                c3 = Component.translatable("tooltip.help.general.3_select_power_key_unbound");
            else
                c3 = Component.translatable("tooltip.help.general.3_select_power_using_key", Component.keybind(SCROLL_KEY.getKeybinding().getName()));
            components.add(Component.translatable("tooltip.help." + name, Component.translatable("tooltip.help.general.1_calculation"), Component.translatable("tooltip.help.general.2_lockable"), c3));
        }
        else
            components.add(Component.translatable("tooltip.help.press_shift_for_help"));
    }

    public Arss() { super(MOD_ID, "Analog_Redstone_Suite", false, createTab(MOD_ID, MOD_ID, ()->()->Items.COMPARATOR)); }
}
