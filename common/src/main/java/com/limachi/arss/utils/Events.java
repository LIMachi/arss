package com.limachi.arss.utils;

import com.limachi.arss.utils.reflect.MethodAccess;
import com.limachi.arss.utils.reflect.ReflectUtils;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * all the architectury events as Class references to be used with {@link com.limachi.arss.utils.annotations.RegisterEventListener}
 */
public enum Events {
    /** @see BlockEvent#BREAK */ BLOCK_BREAK(BlockEvent.Break.class),
    /** @see BlockEvent#PLACE */ BLOCK_PLACE(BlockEvent.Place.class),
    /** @see BlockEvent#FALLING_LAND */ BLOCK_FALLING_LAND(BlockEvent.FallingLand.class),
    /** @see ChatEvent#DECORATE */ CHAT_DECORATE(ChatEvent.Decorate.class),
    /** @see ChatEvent#RECEIVED */ CHAT_RECEIVED(ChatEvent.Received.class),
    /** @see ChunkEvent#SAVE_DATA */ CHUNK_SAVE_DATA(ChunkEvent.SaveData.class),
    /** @see ChunkEvent#LOAD_DATA */ CHUNK_LOAD_DATA(ChunkEvent.LoadData.class),
    // /** @see CommandPerformEvent */ COMMAND_EXECUTION,
    /** @see CommandRegistrationEvent#EVENT */ COMMAND_REGISTRATION(CommandRegistrationEvent.class),
    /** @see EntityEvent#LIVING_DEATH */ LIVING_DEATH(EntityEvent.LivingDeath.class),
    /** @see EntityEvent#LIVING_HURT */ LIVING_HURT(EntityEvent.LivingHurt.class),
    /** @see EntityEvent#LIVING_CHECK_SPAWN */ LIVING_CHECK_SPAWN(EntityEvent.LivingCheckSpawn.class),
    /** @see EntityEvent#ADD */ ENTITY_ADD(EntityEvent.Add.class),
    /** @see EntityEvent#ENTER_SECTION */ ENTITY_ENTER_SECTION(EntityEvent.EnterSection.class),
    /** @see EntityEvent#ANIMAL_TAME */ ANIMAL_TAME(EntityEvent.AnimalTame.class),
    /** @see ExplosionEvent#PRE */ EXPLOSION_PRE(ExplosionEvent.Pre.class),
    /** @see ExplosionEvent#DETONATE */ EXPLOSION_DETONATE(ExplosionEvent.Detonate.class),
    /** @see InteractionEvent#LEFT_CLICK_BLOCK */ LEFT_CLICK_BLOCK(InteractionEvent.LeftClickBlock.class),
    /** @see InteractionEvent#RIGHT_CLICK_BLOCK */ RIGHT_CLICK_BLOCK(InteractionEvent.RightClickBlock.class),
    /** @see InteractionEvent#RIGHT_CLICK_ITEM */ RIGHT_CLICK_ITEM(InteractionEvent.RightClickItem.class),
    /** @see InteractionEvent#INTERACT_ENTITY */ RIGHT_CLICK_ENTITY(InteractionEvent.InteractEntity.class),
    /** @see InteractionEvent#FARMLAND_TRAMPLE */ FARMLAND_TRAMPLE(InteractionEvent.FarmlandTrample.class),
    /** @see LifecycleEvent#SERVER_BEFORE_START */ SERVER_BEFORE_START(LifecycleEvent.InstanceState.class),
    /** @see LifecycleEvent#SERVER_STARTING */ SERVER_STARTING(LifecycleEvent.InstanceState.class),
    /** @see LifecycleEvent#SERVER_STARTED */ SERVER_STARTED(LifecycleEvent.InstanceState.class),
    /** @see LifecycleEvent#SERVER_STOPPING */ SERVER_STOPPING(LifecycleEvent.InstanceState.class),
    /** @see LifecycleEvent#SERVER_STOPPED */ SERVER_STOPPED(LifecycleEvent.InstanceState.class),
    /** @see LifecycleEvent#SERVER_LEVEL_LOAD */ SERVER_LEVEL_LOAD(LifecycleEvent.LevelState.class),
    /** @see LifecycleEvent#SERVER_LEVEL_UNLOAD */ SERVER_LEVEL_UNLOAD(LifecycleEvent.LevelState.class),
    /** @see LifecycleEvent#SERVER_LEVEL_SAVE */ SERVER_LEVEL_SAVE(LifecycleEvent.LevelState.class),
    /** NOTE: prefer @StaticInit and @ClientStaticInit
     * @see LifecycleEvent#SETUP */ SETUP(Runnable.class),
    /** @see LightningEvent#STRIKE */ LIGHTNING_STRIKE(LightningEvent.Strike.class),
    /** @see LootEvent#MODIFY_LOOT_TABLE */ MODIFY_LOOT_TABLE(LootEvent.ModifyLootTable.class),
    /** @see PlayerEvent#PLAYER_JOIN */ PLAYER_JOIN(PlayerEvent.PlayerJoin.class),
    /** @see PlayerEvent#PLAYER_QUIT */ PLAYER_QUIT(PlayerEvent.PlayerQuit.class),
    /** @see PlayerEvent#PLAYER_RESPAWN */ PLAYER_RESPAWN(PlayerEvent.PlayerRespawn.class),
    /** @see PlayerEvent#PLAYER_ADVANCEMENT */ PLAYER_ADVANCEMENT(PlayerEvent.PlayerAdvancement.class),
    /** @see PlayerEvent#PLAYER_CLONE */ PLAYER_CLONE(PlayerEvent.PlayerClone.class),
    /** @see PlayerEvent#CRAFT_ITEM */ CRAFT_ITEM(PlayerEvent.CraftItem.class),
    /** @see PlayerEvent#SMELT_ITEM */ SMELT_ITEM(PlayerEvent.SmeltItem.class),
    /** @see PlayerEvent#PICKUP_ITEM_PRE */ CAN_PICKUP_ITEM(PlayerEvent.PickupItemPredicate.class),
    /** @see PlayerEvent#PICKUP_ITEM_POST */ PICKUP_ITEM(PlayerEvent.PickupItem.class),
    /** @see PlayerEvent#CHANGE_DIMENSION */ CHANGE_DIMENSION(PlayerEvent.ChangeDimension.class),
    /** @see PlayerEvent#DROP_ITEM */ DROP_ITEM(PlayerEvent.DropItem.class),
    /** @see PlayerEvent#OPEN_MENU */ OPEN_MENU(PlayerEvent.OpenMenu.class),
    /** @see PlayerEvent#CLOSE_MENU */ CLOSE_MENU(PlayerEvent.CloseMenu.class),
    /** @see PlayerEvent#FILL_BUCKET */ FILL_BUCKET(PlayerEvent.FillBucket.class),
    /** @see PlayerEvent#ATTACK_ENTITY */ ATTACK_ENTITY(PlayerEvent.AttackEntity.class),
    /** @see TickEvent#SERVER_PRE */ TICK_SERVER_PRE(TickEvent.class),
    /** @see TickEvent#SERVER_POST */ TICK_SERVER_POST(TickEvent.class),
    /** @see TickEvent#SERVER_LEVEL_PRE */ TICK_SERVER_LEVEL_PRE(TickEvent.class),
    /** @see TickEvent#SERVER_LEVEL_POST */ TICK_SERVER_LEVEL_POST(TickEvent.class),
    /** @see TickEvent#PLAYER_PRE */ TICK_PLAYER_PRE(TickEvent.class),
    /** @see TickEvent#PLAYER_POST */ TICK_PLAYER_POST(TickEvent.class),
    ;

    private final Class<?>[] parameters;
    private final Class<?> returnType;

    Events(Class<?> clazz) {
        var m = ReflectUtils.functionalInterface(clazz).orElse(null);
        if (m == null)
            throw new RuntimeException("Event: " + clazz + " is not a functional interface");
        parameters = m.getParameterTypes();
        returnType = m.getReturnType();
    }

    Events() {
        parameters = new Class[]{EventActor.class};
        returnType = EventResult.class;
    }

    private static class Method {
        MethodAccess<?, ?> access;
        Method(MethodAccess<?, ?> methodAccess) { access = methodAccess; }
        EventResult eventResult(Object ... param) { return (EventResult)access.getStatic(param); }
        void none(Object ... param) { access.getStatic(param); }
        CompoundEventResult<ItemStack> compoundStack(Object ... param) { return (CompoundEventResult<ItemStack>)access.getStatic(param); }
    }

    /**
     * test if the given method matches the event then register it
     */
    public void register(MethodAccess<?, ?> methodAccess) {
        if (!methodAccess.mayCallWith(returnType, parameters))
            throw new RuntimeException("Trying to register event: " + this + " with invalid method type: " + methodAccess);
        Method m = new Method(methodAccess);
        switch (this) {
            case BLOCK_BREAK -> BlockEvent.BREAK.register(m::eventResult);
            case BLOCK_PLACE -> BlockEvent.PLACE.register(m::eventResult);
            case BLOCK_FALLING_LAND -> BlockEvent.FALLING_LAND.register(m::none);
            case CHAT_DECORATE -> ChatEvent.DECORATE.register(m::none);
            case CHAT_RECEIVED -> ChatEvent.RECEIVED.register(m::eventResult);
            case CHUNK_SAVE_DATA -> ChunkEvent.SAVE_DATA.register(m::none);
            case CHUNK_LOAD_DATA -> ChunkEvent.LOAD_DATA.register(m::none);
//            case COMMAND_EXECUTION -> CommandPerformEvent.EVENT.register(m::eventResult);
            case COMMAND_REGISTRATION -> CommandRegistrationEvent.EVENT.register(m::none);
            case LIVING_DEATH -> EntityEvent.LIVING_DEATH.register(m::eventResult);
            case LIVING_HURT -> EntityEvent.LIVING_HURT.register(m::eventResult);
            case LIVING_CHECK_SPAWN -> EntityEvent.LIVING_CHECK_SPAWN.register(m::eventResult);
            case ENTITY_ADD -> EntityEvent.ADD.register(m::eventResult);
            case ENTITY_ENTER_SECTION -> EntityEvent.ENTER_SECTION.register(m::eventResult);
            case ANIMAL_TAME -> EntityEvent.ANIMAL_TAME.register(m::eventResult);
            case EXPLOSION_PRE -> ExplosionEvent.PRE.register(m::eventResult);
            case EXPLOSION_DETONATE -> ExplosionEvent.DETONATE.register(m::none);
            case LEFT_CLICK_BLOCK -> InteractionEvent.LEFT_CLICK_BLOCK.register(m::eventResult);
            case RIGHT_CLICK_BLOCK -> InteractionEvent.RIGHT_CLICK_BLOCK.register(m::eventResult);
            case RIGHT_CLICK_ITEM -> InteractionEvent.RIGHT_CLICK_ITEM.register(m::compoundStack);
            case RIGHT_CLICK_ENTITY -> InteractionEvent.INTERACT_ENTITY.register(m::eventResult);
            case FARMLAND_TRAMPLE -> InteractionEvent.FARMLAND_TRAMPLE.register(m::eventResult);
            case SERVER_BEFORE_START -> LifecycleEvent.SERVER_BEFORE_START.register(m::none);
            case SERVER_STARTING -> LifecycleEvent.SERVER_STARTING.register(m::none);
            case SERVER_STARTED -> LifecycleEvent.SERVER_STARTED.register(m::none);
            case SERVER_STOPPING -> LifecycleEvent.SERVER_STOPPING.register(m::none);
            case SERVER_STOPPED -> LifecycleEvent.SERVER_STOPPED.register(m::none);
            case SERVER_LEVEL_LOAD -> LifecycleEvent.SERVER_LEVEL_LOAD.register(m::none);
            case SERVER_LEVEL_UNLOAD -> LifecycleEvent.SERVER_LEVEL_UNLOAD.register(m::none);
            case SERVER_LEVEL_SAVE -> LifecycleEvent.SERVER_LEVEL_SAVE.register(m::none);
            case SETUP -> LifecycleEvent.SETUP.register(m::none);
            case LIGHTNING_STRIKE -> LightningEvent.STRIKE.register(m::none);
            case MODIFY_LOOT_TABLE -> LootEvent.MODIFY_LOOT_TABLE.register(m::none);
            case PLAYER_JOIN -> PlayerEvent.PLAYER_JOIN.register(m::none);
            case PLAYER_QUIT -> PlayerEvent.PLAYER_QUIT.register(m::none);
            case PLAYER_RESPAWN -> PlayerEvent.PLAYER_RESPAWN.register(m::none);
            case PLAYER_ADVANCEMENT -> PlayerEvent.PLAYER_ADVANCEMENT.register(m::none);
            case PLAYER_CLONE -> PlayerEvent.PLAYER_CLONE.register(m::none);
            case CRAFT_ITEM -> PlayerEvent.CRAFT_ITEM.register(m::none);
            case SMELT_ITEM -> PlayerEvent.SMELT_ITEM.register(m::none);
            case CAN_PICKUP_ITEM -> PlayerEvent.PICKUP_ITEM_PRE.register(m::eventResult);
            case PICKUP_ITEM -> PlayerEvent.PICKUP_ITEM_POST.register(m::none);
            case CHANGE_DIMENSION -> PlayerEvent.CHANGE_DIMENSION.register(m::none);
            case DROP_ITEM -> PlayerEvent.DROP_ITEM.register(m::eventResult);
            case OPEN_MENU -> PlayerEvent.OPEN_MENU.register(m::none);
            case CLOSE_MENU -> PlayerEvent.CLOSE_MENU.register(m::none);
            case FILL_BUCKET -> PlayerEvent.FILL_BUCKET.register(m::compoundStack);
            case ATTACK_ENTITY -> PlayerEvent.ATTACK_ENTITY.register(m::eventResult);
            case TICK_SERVER_PRE -> TickEvent.SERVER_PRE.register(m::none);
            case TICK_SERVER_POST -> TickEvent.SERVER_POST.register(m::none);
            case TICK_SERVER_LEVEL_PRE -> TickEvent.SERVER_LEVEL_PRE.register(m::none);
            case TICK_SERVER_LEVEL_POST -> TickEvent.SERVER_LEVEL_POST.register(m::none);
            case TICK_PLAYER_PRE -> TickEvent.PLAYER_PRE.register(m::none);
            case TICK_PLAYER_POST -> TickEvent.PLAYER_POST.register(m::none);
        }
    }
}
