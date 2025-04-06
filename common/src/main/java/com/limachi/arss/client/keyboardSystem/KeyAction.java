package com.limachi.arss.client.keyboardSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record KeyAction(int frequency, int power) {
    public void pressed() {}
    public void released() {}
}
