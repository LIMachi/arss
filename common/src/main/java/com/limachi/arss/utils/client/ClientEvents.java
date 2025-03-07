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

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * all the architectury client events as Class references to be used with {@link com.limachi.arss.utils.client.annotations.RegisterClientEventListener}
 */
@Environment(EnvType.CLIENT)
public enum ClientEvents {
    /** @see InteractionEvent#CLIENT_LEFT_CLICK_AIR */ LEFT_CLICK_AIR(InteractionEvent.ClientLeftClickAir.class),
    /** @see InteractionEvent#CLIENT_RIGHT_CLICK_AIR */ RIGHT_CLICK_AIR(InteractionEvent.ClientRightClickAir.class),
    /** @see ClientChatEvent#SEND */ CHAT_SEND(ClientChatEvent.Send.class),
    /** @see ClientChatEvent#RECEIVED */ CHAT_RECEIVED(ClientChatEvent.Received.class),
    //TODO missing client events
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

    ClientEvents() {
        parameters = new Class[]{EventActor.class};
        returnType = EventResult.class;
    }

    private static class Method {
        MethodAccess<?, ?> access;
        Method(MethodAccess<?, ?> methodAccess) { access = methodAccess; }
        EventResult eventResult(Object ... param) { return (EventResult)access.getStatic(param); }
        void none(Object ... param) { access.getStatic(param); }
        CompoundEventResult<ItemStack> compoundStack(Object ... param) { return (CompoundEventResult<ItemStack>)access.getStatic(param); }
        CompoundEventResult<Component> compoundComponent(Object ... param) { return (CompoundEventResult<Component>)access.getStatic(param); }
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
        }
    }
}
