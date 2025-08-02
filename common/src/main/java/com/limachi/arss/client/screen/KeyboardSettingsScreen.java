package com.limachi.arss.client.screen;

import com.limachi.lim_lib.client.screens.SimpleScreen;

import com.limachi.lim_lib.client.utils.MidiHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class KeyboardSettingsScreen extends SimpleScreen {
    public KeyboardSettingsScreen(KeyboardScreen parent) {
        super(parent);
        imageWidth = 170;
        imageHeight = 70;
    }

    @Override
    protected void init() {
        super.init();

        var devices = MidiHandler.getDevices();
        devices.addFirst("");

        var midiSelector = new CycleButton.Builder<>(Component::literal)
                .withInitialValue(MidiHandler.currentDevice())
                .withValues(devices)
                .displayOnlyValue()
                .withTooltip(b->Tooltip.create(Component.translatable("screen.button.bind_midi.tooltip")))
                .create(leftPos + 10, topPos + 22, 150, 16, Component.empty());
        addRenderableWidget(midiSelector);

        addRenderableWidget(Button.builder(Component.translatable("screen.button.bind_midi"), b->{
            MidiHandler.bindDevice(midiSelector.getValue());
            b.setFocused(false);
            screen().setFocused(null);
            ((KeyboardSettingsScreen)screen()).init(); //this cast is not an error, because init is protected in screen, we need to explicitly use ours
        }).bounds(leftPos + 10, topPos + 42, 150, 16).tooltip(Tooltip.create(Component.translatable("screen.button.bind_midi.tooltip"))).build());

        addRenderableOnly(new FittingMultiLineTextWidget(leftPos + 10, topPos + 10, 250, 16, Component.translatable("screen.widget.bound_to", MidiHandler.currentDevice()), font));
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (isOutsideScreen(d, e)) {
            onClose();
            return false;
        }
        return super.mouseClicked(d, e, i);
    }
}
