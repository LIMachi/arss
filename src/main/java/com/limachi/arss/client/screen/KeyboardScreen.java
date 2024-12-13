package com.limachi.arss.client.screen;

import com.limachi.arss.Arss;
import com.limachi.arss.client.MidiHandler;
import com.limachi.arss.items.KeyboardItem;
import com.limachi.arss.menu.KeyboardMenu;
import com.limachi.lim_lib.network.messages.ScreenNBTMsg;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.screens.IDontShowJEI;
import com.limachi.lim_lib.utils.Tags;
import com.limachi.lim_lib.widgets.StaticStringWidget;
import com.limachi.lim_lib.widgets.TextEditWithSuggestions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.Objects;

@RegisterMenuScreen
@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class KeyboardScreen extends AbstractContainerScreen<KeyboardMenu> implements IDontShowJEI, MidiHandler.ICatchMIDI {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Arss.MOD_ID, "textures/screen/keyboard_screen.png");
    private final KeyboardScreen SCREEN = this;

    public KeyboardScreen(KeyboardMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 208;
        imageHeight = 166;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 73;
        MidiHandler.KEY_CATCHER = this;
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


    class BindingButton extends Button {
        boolean selected = false;
        int compactBinding = -1;
        int power;

        public BindingButton(int x, int y, int power) {
            super(builder(Component.empty(), (s) -> {
                BindingButton b = (BindingButton)s;
                b.selected = !b.selected;
                if (!b.selected) {
                    b.setFocused(false);
                    SCREEN.setFocused(null);
                }
                }).bounds(x, y, 50, 16));
            this.power = power;
        }

        public BindingButton setKeyBind(int compactBinding) {
            this.compactBinding = compactBinding;
            CompoundTag packet = Tags.singleton("binding", compactBinding);
            ScreenNBTMsg.send(power, packet);
            menu.upstreamNBTMessage(power, packet); //WARNING: hack to force the item to update client side
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
            SCREEN.setFocused(null);
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

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key != GLFW.GLFW_KEY_ESCAPE && (getFocused() == null))
            return false;
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    protected void init() {
        super.init();

        final TextEditWithSuggestions midiSelector = new TextEditWithSuggestions(font, getGuiLeft() + 99, getGuiTop() + 9, 100, 16, MidiHandler.currentDevice(), null, MidiHandler.getDevices()).forceSuggestion(true);
        addRenderableWidget(midiSelector);

        addRenderableWidget(Button.builder(Component.translatable("screen.button.bind_midi"), b->{
            MidiHandler.bindDevice(midiSelector.getValue());
            b.setFocused(false);
            SCREEN.setFocused(null);
            SCREEN.rebuildWidgets();
        }).bounds(getGuiLeft() + 8, getGuiTop() + 9, 88, 16).build());

        addRenderableOnly(new StaticStringWidget.Builder().at(getGuiLeft() + 11, getGuiTop() + 39).text(Component.translatable("screen.widget.bound_to", MidiHandler.currentDevice())).build());

        CompoundTag tag = menu.inv.player.getItemInHand(menu.hand).getTag();

        if (tag != null) {
            int[] bindings = KeyboardItem.getBindings(tag);
            if (bindings.length == 15)
                for (int y = 0; y < 5; ++y)
                    for (int x = 0; x < 3; ++x)
                        addRenderableWidget(new BindingButton(getGuiLeft() + 21 + x * 64, getGuiTop() + 60 + 20 * y, x + y * 3).setKeyBind(bindings[x + y * 3]));
        }
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics p_281635_, int p_282681_, int p_283686_) {}

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float tick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        gui.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    public void onClose() {
        if (MidiHandler.KEY_CATCHER == this)
            MidiHandler.KEY_CATCHER = null;
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !((getFocused() instanceof BindingButton));
    }
}
