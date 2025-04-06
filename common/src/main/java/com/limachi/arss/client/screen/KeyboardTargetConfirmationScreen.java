package com.limachi.arss.client.screen;

import com.limachi.arss.common.ArssItemStackComponents;
import com.limachi.arss.common.items.Keyboard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import org.lwjgl.glfw.GLFW;

//layout:
//top: name box
//middle: confirmation / cancel buttons (mouse forced moved on the confirmation on first init call)
//bottom: list of 15 slots (with an highlight of which will be used, each is clickable + removal button)

/*
@Environment(EnvType.CLIENT)
public class KeyboardTargetConfirmationScreen extends Screen {
    public static void client_open(Player player, InteractionHand hand, BlockPos target) {
        if (Minecraft.getInstance().isSameThread())
            Minecraft.getInstance().setScreen(new KeyboardTargetConfirmationScreen(player, hand, target));
    }

    public final Player player;
    public final InteractionHand hand;
    public final BlockPos target;
    public final BlockState state;
    public final ArssItemStackComponents.NamedPos np;
    public String name;
    public int slot;

    protected KeyboardTargetConfirmationScreen(Player player, InteractionHand hand, BlockPos target) {
        super(Component.empty());
        this.player = player;
        this.hand = hand;
        this.target = target;
        state = player.level().getBlockState(target);
        np = player.getItemInHand(hand).get(ArssItemStackComponents.TARGETS.get());
        if (np == null)
            throw new RuntimeException("KeyboardTargetConfirmationScreen called with invalid item components: " + player.getItemInHand(hand));
        name = state.getBlock().getName().getString();
        name += "#" + np.countNames(name);
        slot = np.emptySlot(); //slot 15 is reserved to signal no slot is available!
    }

    protected boolean firstInit = true;

    protected EditBox namer;
    protected Button validate;
    protected Button cancel;

    @Override
    protected void init() {
        namer = new EditBox(minecraft.font, 50, 20, 120, 10, namer, Component.empty());
        validate = Button.builder(Component.translatable("validate"), b->{
            namer.getValue();
            //TODO: send validation message
            onClose();
        }).pos(50, 50).size(60, 10).build();
        cancel = Button.builder(Component.translatable("cancel"), b->onClose()).pos(170, 50).size(60, 10).build();
        validate.active = slot != 15;
        addRenderableWidget(namer);
        addRenderableWidget(validate);
        addRenderableWidget(cancel);
        //setup the validate and cancel buttons
        if (firstInit) {
            firstInit = false;
            namer.setValue(name);
            GLFW.glfwSetCursorPos(Minecraft.getInstance().getWindow().getWindow(), 50 + (validate.active ? 60 : 180), 55);
            if (validate.active)
                setFocused(validate);
            else
                setFocused(cancel);
        }
        //setup list of already known names (selectable, only one at a time) and removal
        for (int i = 0; i < 15; ++i) {
            final int s = i;
            addRenderableWidget(Button.builder(Component.literal(""), b->slot = s).pos(50, 70 + i * 10).size(60, 10).build());
            addRenderableWidget(Button.builder(Component.translatable("remove"), b->{}).pos(170, 70 + i * 10).size(60, 10).build());
        }
    }

    protected boolean stillValid() {
        return player.getItemInHand(hand).getItem() instanceof Keyboard && player.level().getBlockState(target).equals(state);
    }
}
*/