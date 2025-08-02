package com.limachi.arss.client.screen;

import com.limachi.arss.client.widgets.PowerSelector;

import com.limachi.arss.common.block_entities.ResonantGateBlockEntity;

import com.limachi.lim_lib.client.screens.SimpleScreen;
import com.limachi.lim_lib.client.utils.MidiHandler;
import com.limachi.lim_lib.client.widgets.TextEditor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import java.util.HashSet;

@Environment(EnvType.CLIENT)
public class KeySettingsScreen extends SimpleScreen implements MidiHandler.ICatchMIDI {
    final int index;
    final KeyboardScreen.Binding binding;

    protected TextEditor namer;
    protected PowerSelector powerSelector;
    protected BindingButton bindingButton;
    protected Button cancelButton;
    protected Button okButton;

    HashSet<String> previousSuggestions;

    public KeySettingsScreen(KeyboardScreen parent, int key) {
        super(parent);
        index = key;
        imageWidth = 170;
        imageHeight = 105;
        binding = parent.getBinding(index).clone();
        previousSuggestions = (HashSet<String>)ResonantGateBlockEntity.clientNames.clone();
        MidiHandler.KEY_CATCHER = this;
        new ResonantGateBlockEntity.RequestNames().sendToServer();
    }

    @Override
    public KeyboardScreen parent() {
        return super.parent();
    }

    //layout:
    //top left key button
    //top right power scroller
    //under target + suggestions

    @Override
    protected void init() {
        super.init();
        boolean first = namer == null;
        addRenderableWidget(bindingButton = new BindingButton(leftPos + 10, topPos + 10, bindingButton));
        addRenderableWidget(powerSelector = new PowerSelector(leftPos + 150, topPos + 12, Component.translatable("screen.arss.key_setting.power"), powerSelector).setUnknownTooltip(Tooltip.create(Component.translatable("screen.arss.key_setting.unknown_tooltip"))));
        addRenderableWidget(namer = TextEditor.builder(leftPos + 10, topPos + 30, namer)
                .width(150)
                .suggestions(ResonantGateBlockEntity.clientNames)
                .maxSuggestions(3)
                .suggestionsBelow(true)
                .build());
        if (first && binding != null) {
            namer.setValue(binding.freq);
            powerSelector.value = binding.power;
            bindingButton.setMessage(binding.getReadableBinding(false));
            powerSelector.unknown = true;
        }
        addRenderableWidget(okButton = Button.builder(Component.translatable("screen.arss.key_setting.validate"), b->finish(true)).bounds(leftPos + 10, topPos + 85, 70, 15).build());
        addRenderableWidget(cancelButton = Button.builder(Component.translatable("screen.arss.key_setting.cancel"), b->finish(false)).bounds(leftPos + 85, topPos + 85, 70, 15).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        if (!ResonantGateBlockEntity.clientNames.equals(previousSuggestions)) {
            previousSuggestions = (HashSet<String>)ResonantGateBlockEntity.clientNames.clone();
            namer.updateSuggestions(previousSuggestions);
        }
    }

    @Override
    public boolean keyState(int channel, int key, byte state) {
        if (getFocused() instanceof BindingButton button) {
            if (channel < 16 && key >= 0 && key < 128)
                button.setKeyBind((channel << 8) | key);
            else
                button.setKeyBind(-1);
            return true;
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    class BindingButton extends Button {
        boolean selected;

        public BindingButton(int x, int y, BindingButton prev) {
            super(x, y, 50, 16, Component.empty(), s->{
                BindingButton b = (BindingButton)s;
                b.selected = !b.selected;
                if (!b.selected) {
                    b.setFocused(false);
                    screen().setFocused(null);
                }
            }, s->Component.empty());
            selected = prev != null && prev.selected;
        }

        public BindingButton setKeyBind(int compactBinding) {
            if (compactBinding != binding.key) {
                binding.key = compactBinding;
                setMessage(binding.getReadableBinding(false));
            }
            selected = false;
            screen().setFocused(null);
            setFocused(false);
            return this;
        }

        @Override
        public boolean keyPressed(int key, int scancode, int modifiers) {
            if (isFocused()) {
                if ((key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_ESCAPE) && selected) {
                    keyState(-1, -1, (byte)0);
                    return true;
                }
                setKeyBind(-key);
                return true;
            }
            return super.keyPressed(key, scancode, modifiers);
        }
    }

    @Override
    public void closing() {
        if (MidiHandler.KEY_CATCHER == this)
            MidiHandler.KEY_CATCHER = null;
    }

    protected void finish(boolean andSet) {
        if (andSet) {
            binding.freq = namer.getValue();
            binding.power = (byte)powerSelector.value;
            parent().setBinding(index, binding);
        }
        onClose();
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (isOutsideScreen(d, e)) {
            finish(false);
            return false;
        }
        return super.mouseClicked(d, e, i);
    }
}
