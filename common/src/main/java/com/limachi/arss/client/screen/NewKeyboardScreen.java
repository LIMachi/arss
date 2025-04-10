package com.limachi.arss.client.screen;

import com.limachi.arss.common.ArssItemStackComponents;
import com.limachi.arss.common.items.Keyboard;

import com.limachi.lim_lib.client.screens.SimpleScreen;

import com.limachi.lim_lib.common.annotations.RegisterMsg;
import com.limachi.lim_lib.common.network.IC2SMsg;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.architectury.networking.NetworkManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * new system:
 * render the keyboard as a background
 * each key is linked to a button that pops up a new configuration screen (the button is transparent with an outline)
 * the redstone block is used to open the midi settings (bind/unbind to selected midi keyboard, forget/rename target)
 * keyboard behavior:
 * Shift right clicking a valid target open the target insertion menu
 * (auto generate the name of the target, put it as the last by default, or if full put as replacement of the first)
 */

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class NewKeyboardScreen extends SimpleScreen {
    public static void client_open(Player player, InteractionHand hand) {
        if (Minecraft.getInstance().isSameThread())
            Minecraft.getInstance().setScreen(new NewKeyboardScreen(player, hand));
    }

    public final Player player;
    public final InteractionHand hand;
    public final BakedModel model;

    protected NewKeyboardScreen(Player player, InteractionHand hand) {
        super(Component.empty());
        this.player = player;
        this.hand = hand;
        model = Minecraft.getInstance().getItemRenderer().getModel(player.getItemInHand(hand), player.level(), player, 0);
    }

    public static class Binding {
        String freq;
        byte power;
        int key;

        public Binding(String freq, byte power, int key) {
            this.freq = freq;
            this.power = power;
            this.key = key;
        }

        public Component getReadableBinding(boolean reduced) {
            if (key == -1)
                return Component.empty();
            if (key < -1) {
                if (GLFW.glfwGetKeyName(-key, 0) instanceof String name)
                    return Component.literal(name);
                return Component.keybind(InputConstants.Type.KEYSYM.getOrCreate(-key).getName());
            }
            else {
                int note = key & 0x7F;
                int channel = (key >> 8) & 0xF;
                int octave = note / 12;
                note = note % 12;
                return Component.translatable(reduced ? "screen.button.midi_keyboard_binding_compact" : "screen.button.midi_keyboard_binding", channel, octave, Component.translatable("display.arss.keyboard_item.semitone." + note));
            }
        }
    }

    Binding getBinding(int key) {
        if (key >= 0 && key < 15 && player.getItemInHand(hand).has(ArssItemStackComponents.BINDINGS.get())) {
            var bindings = player.getItemInHand(hand).get(ArssItemStackComponents.BINDINGS.get());
            return new Binding(bindings.freq()[key], bindings.power()[key], bindings.key()[key]);
        }
        return null;
    }

    void setBinding(int key, Binding binding) {
        if (key >= 0 && key < 15 && binding != null && player.getItemInHand(hand).has(ArssItemStackComponents.BINDINGS.get())) {
            var bindings = player.getItemInHand(hand).get(ArssItemStackComponents.BINDINGS.get());
            player.getItemInHand(hand).set(ArssItemStackComponents.BINDINGS.get(), bindings.setBinding(key, binding.freq, binding.power, binding.key));
        }
    }

    private final NewKeyboardScreen SCREEN = this;

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key != GLFW.GLFW_KEY_ESCAPE && (getFocused() == null))
            return false;
        return super.keyPressed(key, scancode, modifiers);
    }

    protected TransparentButton hoveredButton = null;
    float scale = 0.75f;

    @Environment(EnvType.CLIENT)
    protected abstract class TransparentButton extends AbstractButton {
        protected int color;

        public TransparentButton(float x, float y, float width, float height, int color) {
            super(centerX + Math.round(x * scale), centerY + Math.round(y * scale), Math.round(width * scale), Math.round(height * scale), Component.empty());
            this.color = color;
            alpha = 0.5f;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            if (hoveredButton == null && isMouseOver(i, j)) {
                isHovered = true;
                hoveredButton = this;
            } else
                isHovered = false;
            if (isHoveredOrFocused()) {
                guiGraphics.setColor((float) ((color & 0xFF0000) >> 16) / 255f, (float) ((color & 0xFF00) >> 8) / 255f, (float) (color & 0xFF) / 255f, alpha);
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                guiGraphics.hLine(getX() - 1, getX() + width, getY() - 1, -1);
                guiGraphics.hLine(getX() - 1, getX() + width, getY() + height, -1);
                guiGraphics.vLine(getX() - 1, getY() - 1, getY() + height, -1);
                guiGraphics.vLine(getX() + width, getY() - 1, getY() + height, -1);
                guiGraphics.setColor(1, 1, 1, 1);
            }
        }

        @Override
        protected boolean clicked(double d, double e) {
            return isHovered || (hoveredButton == null && isMouseOver(d, e));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        @Override
        public boolean isMouseOver(double d, double e) {
            return screen().isActive() && super.isMouseOver(d, e);
        }
    }

    @Environment(EnvType.CLIENT)
    protected class KeyButton extends TransparentButton {
        int key;
        public KeyButton(float x, float y, float width, float height, int key) {
            super(x, y, width, height, 0xFFFF);
            this.key = key;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            super.renderWidget(guiGraphics, i, j, f);
            guiGraphics.pose().pushPose();
            Component text = getBinding(key).getReadableBinding(true);
            int length = text.getString().length();
            float scale = length > 0 ? Float.max(2f / (float) length, 0.01f) : 1f;
            guiGraphics.pose().scale(scale, scale, scale);
            guiGraphics.drawCenteredString(minecraft.font, text, Math.round((getX() + 1 + width / 2) * (1 / scale)), Math.round((getY() + height - 9) * (1 / scale) - 5), -1);
            guiGraphics.pose().popPose();
        }

        @Override
        public void onPress() {
            screen().openChildScreen(p->new KeySettingsScreen(screen(), key));
        }
    }

    @Environment(EnvType.CLIENT)
    protected class RedstoneButton extends TransparentButton {
        public RedstoneButton(float x, float y, float width, float height) {
            super(x, y, width, height, 0xFF0000);
        }

        @Override
        public void onPress() {
            screen().openChildScreen(KeyboardSettingsScreen::new);
        }
    }

    @Override
    protected void init() {
        super.init();

        for (int i = 0; i < 3; ++i) {
            final int button = i + 12;
            addRenderableWidget(new KeyButton(-100 + i * 60, -60, 40, 40, i + 12));
        }
        addRenderableWidget(new RedstoneButton(85, -55, 20, 20));
        for (int i = 0; i < 6; ++i)
            if (i < 2)
                addRenderableWidget(new KeyButton(-80 + i * 30, 0, 20, 60, 1 + i * 2));
            else if (i > 2)
                addRenderableWidget(new KeyButton(-85 + i * 30, 0, 20, 60, 2 * i));
        for (int i = 0; i < 7; ++i)
            if (i < 3)
                addRenderableWidget(new KeyButton(-95 + i * 30, 0, 20, 80, 2 * i));
            else
                addRenderableWidget(new KeyButton(-100 + i * 30, 0, 20, 80, 2 * i - 1));
    }

    @Override
    public boolean shouldRenderBlur() {
        return false; //manually handled
    }

    private ItemStack ref;

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        hoveredButton = null;
        ItemStack stack = player.getItemInHand(hand);
        if (minecraft != null && minecraft.getItemRenderer() instanceof ItemRenderer renderer && stack.getItem() instanceof Keyboard) {
            if (ref == null) {
                ref = stack.copy();
                ref.set(ArssItemStackComponents.CATCH.get(), true);
            }
            Lighting.setupForFlatItems();
            RenderSystem.disableDepthTest();
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            if (Minecraft.getInstance().screen == this) {
                pose.translate(0, 0, -150);
                renderTransparentBackground(guiGraphics);
            }
            else
                pose.translate(0, 0, -50);
            pose.translate(centerX, centerY, 0);
            pose.scale(-320 * scale, -320 * scale, -1);
            renderer.render(ref, ItemDisplayContext.FIXED, false, pose, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, model);
            guiGraphics.flush();
            pose.popPose();
        } else
            onClose();
    }

    @Override
    public void renderFg(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public boolean isPauseScreen() { return false; } //TODO: thanks to this we could also allow the testing of the keyboard while in the menu

    @RegisterMsg
    public record UpdateBindings(InteractionHand hand, ItemStack keyboard) implements IC2SMsg<UpdateBindings> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            if (ctx.getPlayer() instanceof ServerPlayer player) {
                if (player.getItemInHand(hand).getItem() == keyboard.getItem() && player.getItemInHand(hand).has(ArssItemStackComponents.BINDINGS.get()))
                    player.getItemInHand(hand).set(ArssItemStackComponents.BINDINGS.get(), keyboard.get(ArssItemStackComponents.BINDINGS.get()));
            }
        }
    }

    @Override
    public void closing() { new UpdateBindings(hand, player.getItemInHand(hand)).sendToServer(); }
}
