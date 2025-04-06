package com.limachi.arss.client.screen;

import com.limachi.arss.utils.client.MidiHandler;
import com.limachi.arss.utils.client.screens.SimpleScreen;
import com.limachi.arss.utils.client.widgets.TextEditor;
import com.limachi.arss.utils.client.widgets.TextSuggestions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class KeySettingsScreen extends SimpleScreen implements MidiHandler.ICatchMIDI {
    final int index;

    public KeySettingsScreen(NewKeyboardScreen parent, int key) {
        super(parent);
        index = key;
    }

    @Override
    public NewKeyboardScreen parent() {
        return super.parent();
    }

    protected TextEditor namer;
    protected TextSuggestions suggestions;
    protected int key;
    protected int power;

    //layout:
    //top left key button
    //top right power scroller
    //under target + suggestions

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean keyState(int channel, int key, boolean state) {
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
        boolean selected = false;
        int compactBinding = -1;
        int power;

        public BindingButton(int x, int y, int power) {
            super(x, y, 50, 16, Component.empty(), s->{
                BindingButton b = (BindingButton)s;
                b.selected = !b.selected;
                if (!b.selected) {
                    b.setFocused(false);
                    screen().setFocused(null);
                }
            }, s->Component.empty());
            this.power = power;
        }

        public BindingButton setKeyBind(int compactBinding) {
            this.compactBinding = compactBinding;
            CompoundTag packet = Util.make(new CompoundTag(), t->t.putInt("binding", compactBinding));
            //TODO: update here
            if (compactBinding != -1) {
                if (compactBinding < -1)
                    setMessage(Component.literal(Objects.requireNonNullElseGet(GLFW.glfwGetKeyName(-compactBinding, -compactBinding), () -> "" + compactBinding)));
                else {
                    int note = compactBinding & 0x7F;
                    int channel = (compactBinding >> 8) & 0xF;
                    int octave = note / 12;
                    note = note % 12;
                    setMessage(Component.translatable("screen.button.midi_keyboard_binding", channel, octave, Component.translatable("display.arss.keyboard_item.semitone." + note)));
                }
            } else
                setMessage(Component.empty());
            selected = false;
            screen().setFocused(null);
            setFocused(false);
            return this;
        }

        @Override
        public boolean keyPressed(int key, int scancode, int modifiers) {
            if (isFocused()) {
                if ((key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_ESCAPE) && selected) {
                    keyState(-1, -1, false);
                    return true;
                }
                setKeyBind(-key);
                return true;
            }
            return super.keyPressed(key, scancode, modifiers);
        }
    }
}
