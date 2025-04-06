package com.limachi.arss.utils.client;

import com.limachi.arss.utils.reflect.MethodAccess;
import com.limachi.arss.utils.reflect.ReflectUtils;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.*;
import dev.architectury.event.events.common.InteractionEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.network.chat.Component;

/**
 * all the architectury client events as Class references to be used with {@link com.limachi.arss.utils.client.annotations.RegisterClientEventListener}
 */
@Environment(EnvType.CLIENT)
public enum ClientEvents {
    /** @see InteractionEvent#CLIENT_LEFT_CLICK_AIR */ LEFT_CLICK_AIR(InteractionEvent.ClientLeftClickAir.class),
    /** @see InteractionEvent#CLIENT_RIGHT_CLICK_AIR */ RIGHT_CLICK_AIR(InteractionEvent.ClientRightClickAir.class),
    /** @see ClientChatEvent#SEND */ CHAT_SEND(ClientChatEvent.Send.class),
    /** @see ClientChatEvent#RECEIVED */ CHAT_RECEIVED(ClientChatEvent.Received.class),
//    /** @see ClientCommandRegistrationEvent#EVENT */ COMMAND_REGISTRATION(ClientCommandRegistrationEvent.class),
    /** @see ClientGuiEvent#RENDER_HUD */ GUI_RENDER_HUD(ClientGuiEvent.RenderHud.class),
    /** @see ClientGuiEvent#DEBUG_TEXT_LEFT */ GUI_DEBUG_TEXT_LEFT(ClientGuiEvent.DebugText.class),
    /** @see ClientGuiEvent#DEBUG_TEXT_RIGHT */ GUI_DEBUG_TEXT_RIGHT(ClientGuiEvent.DebugText.class),
    /** @see ClientGuiEvent#INIT_PRE */ GUI_INIT_PRE(ClientGuiEvent.ScreenInitPre.class),
    /** @see ClientGuiEvent#INIT_POST */ GUI_INIT_POST(ClientGuiEvent.ScreenInitPost.class),
    /** @see ClientGuiEvent#RENDER_PRE */ GUI_RENDER_PRE(ClientGuiEvent.ScreenRenderPre.class),
    /** @see ClientGuiEvent#RENDER_POST */ GUI_RENDER_POST(ClientGuiEvent.ScreenRenderPost.class),
    /** @see ClientGuiEvent#RENDER_CONTAINER_BACKGROUND */ GUI_RENDER_CONTAINER_BACKGROUND(ClientGuiEvent.ContainerScreenRenderBackground.class),
    /** @see ClientGuiEvent#RENDER_CONTAINER_FOREGROUND */ GUI_RENDER_CONTAINER_FOREGROUND(ClientGuiEvent.ContainerScreenRenderForeground.class),
    /** @see ClientGuiEvent#SET_SCREEN */ GUI_SET_SCREEN(ClientGuiEvent.SetScreen.class),
    /** @see ClientLifecycleEvent#CLIENT_STARTED */ CLIENT_STARTED(void.class, Minecraft.class),
    /** @see ClientLifecycleEvent#CLIENT_STOPPING */ CLIENT_STOPPING(void.class, Minecraft.class),
    /** @see ClientLifecycleEvent#CLIENT_LEVEL_LOAD */ LEVEL_LOAD(void.class, ClientLevel.class),
    /** @see ClientPlayerEvent#CLIENT_PLAYER_JOIN */ PLAYER_JOIN(ClientPlayerEvent.ClientPlayerJoin.class),
    /** @see ClientPlayerEvent#CLIENT_PLAYER_QUIT */ PLAYER_QUIT(ClientPlayerEvent.ClientPlayerQuit.class),
    /** @see ClientPlayerEvent#CLIENT_PLAYER_RESPAWN */ PLAYER_RESPAWN(ClientPlayerEvent.ClientPlayerRespawn.class),
    /** @see ClientRawInputEvent#MOUSE_SCROLLED */ MOUSE_SCROLLED(ClientRawInputEvent.MouseScrolled.class),
    /** @see ClientRawInputEvent#MOUSE_CLICKED_PRE */ MOUSE_CLICKED_PRE(ClientRawInputEvent.MouseClicked.class),
    /** @see ClientRawInputEvent#MOUSE_CLICKED_POST */ MOUSE_CLICKED_POST(ClientRawInputEvent.MouseClicked.class),
    /** @see ClientRawInputEvent#KEY_PRESSED */ KEY_PRESSED(ClientRawInputEvent.KeyPressed.class),
    /** @see ClientRecipeUpdateEvent#EVENT */ RECIPE_UPDATE(ClientRecipeUpdateEvent.class),
    /** @see ClientReloadShadersEvent#EVENT */ RELOAD_SHADERS(ClientReloadShadersEvent.class),
    /** @see ClientScreenInputEvent#MOUSE_SCROLLED_PRE */ SCREEN_MOUSE_SCROLLED_PRE(ClientScreenInputEvent.MouseScrolled.class),
    /** @see ClientScreenInputEvent#MOUSE_SCROLLED_POST */ SCREEN_MOUSE_SCROLLED_POST(ClientScreenInputEvent.MouseScrolled.class),
    /** @see ClientScreenInputEvent#MOUSE_CLICKED_PRE */ SCREEN_MOUSE_CLICKED_PRE(ClientScreenInputEvent.MouseClicked.class),
    /** @see ClientScreenInputEvent#MOUSE_CLICKED_POST */ SCREEN_MOUSE_CLICKED_POST(ClientScreenInputEvent.MouseClicked.class),
    /** @see ClientScreenInputEvent#MOUSE_RELEASED_PRE */ SCREEN_MOUSE_RELEASED_PRE(ClientScreenInputEvent.MouseReleased.class),
    /** @see ClientScreenInputEvent#MOUSE_RELEASED_POST */ SCREEN_MOUSE_RELEASED_POST(ClientScreenInputEvent.MouseReleased.class),
    /** @see ClientScreenInputEvent#MOUSE_DRAGGED_PRE */ SCREEN_MOUSE_DRAGGED_PRE(ClientScreenInputEvent.MouseDragged.class),
    /** @see ClientScreenInputEvent#MOUSE_DRAGGED_POST */ SCREEN_MOUSE_DRAGGED_POST(ClientScreenInputEvent.MouseDragged.class),
    /** @see ClientScreenInputEvent#CHAR_TYPED_PRE */ SCREEN_CHAR_TYPED_PRE(ClientScreenInputEvent.KeyTyped.class),
    /** @see ClientScreenInputEvent#CHAR_TYPED_POST */ SCREEN_CHAR_TYPED_POST(ClientScreenInputEvent.KeyTyped.class),
    /** @see ClientScreenInputEvent#KEY_PRESSED_PRE */ SCREEN_KEY_PRESSED_PRE(ClientScreenInputEvent.KeyPressed.class),
    /** @see ClientScreenInputEvent#KEY_PRESSED_POST */ SCREEN_KEY_PRESSED_POST(ClientScreenInputEvent.KeyPressed.class),
    /** @see ClientScreenInputEvent#KEY_RELEASED_PRE */ SCREEN_KEY_RELEASED_PRE(ClientScreenInputEvent.KeyReleased.class),
    /** @see ClientScreenInputEvent#KEY_RELEASED_POST */ SCREEN_KEY_RELEASED_POST(ClientScreenInputEvent.KeyReleased.class),
    /** @see ClientSystemMessageEvent#RECEIVED */ SYSTEM_MESSAGE(ClientSystemMessageEvent.Received.class),
    /** @see ClientTickEvent#CLIENT_PRE */ TICK_PRE(void.class, Minecraft.class),
    /** @see ClientTickEvent#CLIENT_POST */ TICK_POST(void.class, Minecraft.class),
    /** @see ClientTickEvent#CLIENT_LEVEL_PRE */ LEVEL_TICK_PRE(void.class, ClientLevel.class),
    /** @see ClientTickEvent#CLIENT_LEVEL_POST */ LEVEL_TICK_POST(void.class, ClientLevel.class),
    /** @see ClientTooltipEvent#ITEM */ TOOLTIP_ITEM(ClientTooltipEvent.Item.class),
    /** @see ClientTooltipEvent#RENDER_PRE */ TOOLTIP_RENDER(ClientTooltipEvent.Render.class),
    /** @see ClientTooltipEvent#RENDER_MODIFY_POSITION */ TOOLTIP_POSITION(ClientTooltipEvent.RenderModifyPosition.class),
    /** @see ClientTooltipEvent#RENDER_MODIFY_COLOR */ TOOLTIP_COLOR(ClientTooltipEvent.RenderModifyColor.class),
    ;

    private final Class<?>[] parameters;
    private final Class<?> returnType;

    ClientEvents(Class<?> clazz) {
        var m = ReflectUtils.functionalInterface(clazz).orElse(null);
        if (m == null)
            throw new RuntimeException("Event: " + clazz + " is not a functional interface");
        parameters = m.getParameterTypes();
        returnType = m.getReturnType();
    }

    ClientEvents(Class<?> returnType, Class<?>[] parameters) {
        this.parameters = parameters;
        this.returnType = returnType;
    }

    ClientEvents(Class<?> returnType, Class<?> parameter) {
        parameters = new Class[]{parameter};
        this.returnType = returnType;
    }

    ClientEvents() {
        parameters = new Class[]{EventActor.class};
        returnType = EventResult.class;
    }

    private static class Method {
        MethodAccess<?, ?> access;
        Method(MethodAccess<?, ?> methodAccess) { access = methodAccess; }
        EventResult eventResult(Object ... param) { return (EventResult)access.getStatic(param); }
        void none(Object ... param) { access.getStatic(param); }
        CompoundEventResult<Component> compoundComponent(Object ... param) { return (CompoundEventResult<Component>)access.getStatic(param); }
        CompoundEventResult<Screen> compoundScreen(Object ... param) { return (CompoundEventResult<Screen>)access.getStatic(param); }
    }

    public void register(MethodAccess<?, ?> methodAccess) {
        if (!methodAccess.mayCallWith(returnType, parameters))
            throw new RuntimeException("Trying to register client event: " + this + " with invalid method type: " + methodAccess);
        Method m = new Method(methodAccess);
        switch (this) {
            case LEFT_CLICK_AIR -> InteractionEvent.CLIENT_LEFT_CLICK_AIR.register(m::none);
            case RIGHT_CLICK_AIR -> InteractionEvent.CLIENT_RIGHT_CLICK_AIR.register(m::none);
            case CHAT_SEND -> ClientChatEvent.SEND.register(m::eventResult);
            case CHAT_RECEIVED -> ClientChatEvent.RECEIVED.register(m::compoundComponent);
//            case COMMAND_REGISTRATION -> ClientCommandRegistrationEvent.EVENT.register(m::none);
            case GUI_RENDER_HUD -> ClientGuiEvent.RENDER_HUD.register(m::none);
            case GUI_DEBUG_TEXT_LEFT -> ClientGuiEvent.DEBUG_TEXT_LEFT.register(m::none);
            case GUI_DEBUG_TEXT_RIGHT -> ClientGuiEvent.DEBUG_TEXT_RIGHT.register(m::none);
            case GUI_INIT_PRE -> ClientGuiEvent.INIT_PRE.register(m::eventResult);
            case GUI_INIT_POST -> ClientGuiEvent.INIT_POST.register(m::none);
            case GUI_RENDER_PRE -> ClientGuiEvent.RENDER_PRE.register(m::eventResult);
            case GUI_RENDER_POST -> ClientGuiEvent.RENDER_POST.register(m::none);
            case GUI_RENDER_CONTAINER_BACKGROUND -> ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.register(m::none);
            case GUI_RENDER_CONTAINER_FOREGROUND -> ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.register(m::none);
            case GUI_SET_SCREEN -> ClientGuiEvent.SET_SCREEN.register(m::compoundScreen);
            case CLIENT_STARTED -> ClientLifecycleEvent.CLIENT_STARTED.register(m::none);
            case CLIENT_STOPPING -> ClientLifecycleEvent.CLIENT_STOPPING.register(m::none);
            case LEVEL_LOAD -> ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(m::none);
            case PLAYER_JOIN -> ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(m::none);
            case PLAYER_QUIT -> ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(m::none);
            case PLAYER_RESPAWN -> ClientPlayerEvent.CLIENT_PLAYER_RESPAWN.register(m::none);
            case MOUSE_SCROLLED -> ClientRawInputEvent.MOUSE_SCROLLED.register(m::eventResult);
            case MOUSE_CLICKED_PRE -> ClientRawInputEvent.MOUSE_CLICKED_PRE.register(m::eventResult);
            case MOUSE_CLICKED_POST -> ClientRawInputEvent.MOUSE_CLICKED_POST.register(m::eventResult);
            case KEY_PRESSED -> ClientRawInputEvent.KEY_PRESSED.register(m::eventResult);
            case RECIPE_UPDATE -> ClientRecipeUpdateEvent.EVENT.register(m::none);
            case RELOAD_SHADERS -> ClientReloadShadersEvent.EVENT.register(m::none);
            case SCREEN_MOUSE_SCROLLED_PRE -> ClientScreenInputEvent.MOUSE_SCROLLED_PRE.register(m::eventResult);
            case SCREEN_MOUSE_SCROLLED_POST -> ClientScreenInputEvent.MOUSE_SCROLLED_POST.register(m::eventResult);
            case SCREEN_MOUSE_CLICKED_PRE -> ClientScreenInputEvent.MOUSE_CLICKED_PRE.register(m::eventResult);
            case SCREEN_MOUSE_CLICKED_POST -> ClientScreenInputEvent.MOUSE_CLICKED_POST.register(m::eventResult);
            case SCREEN_MOUSE_RELEASED_PRE -> ClientScreenInputEvent.MOUSE_RELEASED_PRE.register(m::eventResult);
            case SCREEN_MOUSE_RELEASED_POST -> ClientScreenInputEvent.MOUSE_RELEASED_POST.register(m::eventResult);
            case SCREEN_MOUSE_DRAGGED_PRE -> ClientScreenInputEvent.MOUSE_DRAGGED_PRE.register(m::eventResult);
            case SCREEN_MOUSE_DRAGGED_POST -> ClientScreenInputEvent.MOUSE_DRAGGED_POST.register(m::eventResult);
            case SCREEN_CHAR_TYPED_PRE -> ClientScreenInputEvent.CHAR_TYPED_PRE.register(m::eventResult);
            case SCREEN_CHAR_TYPED_POST -> ClientScreenInputEvent.CHAR_TYPED_POST.register(m::eventResult);
            case SCREEN_KEY_PRESSED_PRE -> ClientScreenInputEvent.KEY_PRESSED_PRE.register(m::eventResult);
            case SCREEN_KEY_PRESSED_POST -> ClientScreenInputEvent.KEY_PRESSED_POST.register(m::eventResult);
            case SCREEN_KEY_RELEASED_PRE -> ClientScreenInputEvent.KEY_RELEASED_PRE.register(m::eventResult);
            case SCREEN_KEY_RELEASED_POST -> ClientScreenInputEvent.KEY_RELEASED_POST.register(m::eventResult);
            case SYSTEM_MESSAGE -> ClientSystemMessageEvent.RECEIVED.register(m::compoundComponent);
            case TICK_PRE -> ClientTickEvent.CLIENT_PRE.register(m::none);
            case TICK_POST -> ClientTickEvent.CLIENT_POST.register(m::none);
            case LEVEL_TICK_PRE -> ClientTickEvent.CLIENT_LEVEL_PRE.register(m::none);
            case LEVEL_TICK_POST -> ClientTickEvent.CLIENT_LEVEL_POST.register(m::none);
            case TOOLTIP_ITEM -> ClientTooltipEvent.ITEM.register(m::none);
            case TOOLTIP_RENDER -> ClientTooltipEvent.RENDER_PRE.register(m::eventResult);
            case TOOLTIP_POSITION -> ClientTooltipEvent.RENDER_MODIFY_POSITION.register(m::none);
            case TOOLTIP_COLOR -> ClientTooltipEvent.RENDER_MODIFY_COLOR.register(m::none);
        }
    }
}
