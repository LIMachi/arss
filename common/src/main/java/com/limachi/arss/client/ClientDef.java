package com.limachi.arss.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClientDef {
    public static final KeyMapping SCROLL_KEY = new KeyMapping("key.hold_to_scroll", 340, "key.categories.arss");
    public static final KeyMapping SNEAK_KEY = Minecraft.getInstance().options.keyShift;
    public static final KeyMapping USE_KEY = Minecraft.getInstance().options.keyUse;

    public static void commonHoverText(String name, List<Component> components) {
        Component[] subSentences = new Component[5];
        if (Screen.hasShiftDown() || Screen.hasControlDown()) {
            for (int i = 1; i <= 5; ++i) {
                if (i == 3 && SCROLL_KEY.isUnbound()) {
                    subSentences[i - 1] = Component.translatable("tooltip.help.general.3_alternate", Component.keybind(SCROLL_KEY.getName()), Component.keybind(SNEAK_KEY.getName()), Component.keybind(USE_KEY.getName()));
                    continue;
                }
                subSentences[i - 1] = Component.translatable("tooltip.help.general." + i, Component.keybind(SCROLL_KEY.getName()), Component.keybind(SNEAK_KEY.getName()), Component.keybind(USE_KEY.getName()));
            }
        }
        if (Screen.hasShiftDown()) {
            if (!Screen.hasControlDown())
                components.add(Component.translatable("tooltip.help.press_ctrl_for_help"));
            components.add(Component.translatable("tooltip.help.shift." + name, (Object[])subSentences));
        }
        else
            components.add(Component.translatable("tooltip.help.press_shift_for_help"));
        if (Screen.hasControlDown()) {
            if (Screen.hasShiftDown())
                components.add(Component.empty());
            components.add(Component.translatable("tooltip.help.ctrl." + name, (Object[])subSentences));
        } else if (!Screen.hasShiftDown())
            components.add(Component.translatable("tooltip.help.press_ctrl_for_help"));
    }
}
