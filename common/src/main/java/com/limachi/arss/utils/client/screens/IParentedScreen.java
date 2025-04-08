package com.limachi.arss.utils.client.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BiFunction;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public interface IParentedScreen {
    default <T extends Screen & IParentedScreen> T screen() { return (T)this; }
    <T extends Screen & IParentedScreen> T parent();
    default boolean isActive() { return Minecraft.getInstance().screen == this; }
    default boolean shouldRenderBlur() { return isActive(); }
    default <T1 extends Screen & IParentedScreen, T2 extends Screen & IParentedScreen> void openChildScreen(Function<T1, T2> screenBuilder) {
        Minecraft.getInstance().setScreen(screenBuilder.apply(screen()));
    }
    default <T1 extends Screen & IParentedScreen, T2 extends Screen & IParentedScreen> void openChildScreen(BiFunction<T1, Component, T2> screenBuilder, Component component) {
        Minecraft.getInstance().setScreen(screenBuilder.apply(screen(), component));
    }
}
