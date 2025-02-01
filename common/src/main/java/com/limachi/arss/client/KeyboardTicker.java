package com.limachi.arss.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.limachi.arss.common.ArssItemStackComponents;
import com.limachi.arss.common.block_entities.KeyboardLectern;
import com.limachi.arss.common.items.Keyboard;
import com.limachi.arss.utils.client.MidiHandler;

import com.limachi.arss.utils.client.annotations.StaticInitClient;
import dev.architectury.event.events.client.ClientTickEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

import java.util.*;

@Environment(EnvType.CLIENT)
public class KeyboardTicker {

    private record KeyAction(BlockPos target, int power, int origin) {}

    private static class Origin {
        final BlockPos lectern;
        final int playerSlot;
        int power = 0;

        Origin(BlockPos lectern) {
            this.lectern = lectern;
            playerSlot = -1;
        }

        Origin(int playerSlot) {
            this.playerSlot = playerSlot;
            lectern = null;
        }

        void send() {
            if (playerSlot != -1)
                new Keyboard.KeyPressVisualFeedbackSlotMsg(playerSlot, power).sendToServer();
            else if (lectern != null)
                new com.limachi.arss.common.blocks.KeyboardLectern.KeyPressVisualFeedbackLecternMsg(lectern, power).sendToServer();
        }
    }

    private static final ArrayList<Origin> cachedOrigins = new ArrayList<>();
    private static final Multimap<Integer, KeyAction> cachedMappings = ArrayListMultimap.create();
    private static Set<Integer> prevCachedMappings = new HashSet<>();

    private static final BlockPos INVALID_POS = new BlockPos(30000001, 1023, 30000001);

    private static void addAllBindings(BlockPos fromPos, ItemStack stack) {
        int[] bindings = Keyboard.getBindings(stack).raw();
        if (bindings != null) {
            BlockPos pos = INVALID_POS;
            ArssItemStackComponents.NamedPos target = stack.get(ArssItemStackComponents.TARGET.get());
            if (target != null) {
                pos = target.pos();
                if (pos.distSqr(fromPos) > Keyboard.KEYBOARD_REACH * Keyboard.KEYBOARD_REACH)
                    pos = INVALID_POS;
            }
            for (int i = 0; i < bindings.length && i < 15; ++i) {
                if (bindings[i] == -1)
                    continue;
                cachedMappings.put(bindings[i], new KeyAction(pos, i + 1, cachedOrigins.size() - 1));
            }
        }
    }

    private static void reloadCache(Player player) {
        cachedOrigins.clear();
        prevCachedMappings = cachedMappings.keySet();
        cachedMappings.clear();
        Inventory inv = player.getInventory();
        int l = inv.getContainerSize();
        BlockPos playerPos = player.blockPosition();
        for (int i = 0; i < l; ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof Keyboard) {
                cachedOrigins.add(new Origin(i));
                if (Keyboard.inputActive(stack))
                    addAllBindings(playerPos, stack);
            }
        }
        for (KeyboardLectern be : KeyboardLectern.getControlled(player)) {
            ItemStack stack = be.getKeyboard();
            if (!stack.isEmpty()) {
                cachedOrigins.add(new Origin(be.getBlockPos()));
                addAllBindings(be.getBlockPos(), stack);
            }
        }
    }

    @StaticInitClient
    public static void registerTickEvent() {
        ClientTickEvent.CLIENT_PRE.register(client->{
            if (client.player == null)
                return;
            reloadCache(client.player);
            if (cachedMappings.isEmpty())
                return;
            long window = Minecraft.getInstance().getWindow().getWindow();
            HashMap<BlockPos, Integer> messages = new HashMap<>();
            for (int binding : cachedMappings.keySet()) {
                boolean play = (binding < 0 && GLFW.glfwGetKey(window, -binding) == GLFW.GLFW_PRESS) || (binding >= 0 && MidiHandler.keyState(binding >> 8, binding & 0xFF));
                for (KeyAction action : cachedMappings.get(binding))
                    messages.compute(action.target, (pos, power) -> {
                        if (play) {
                            int out = power == null ? action.power : Math.max(action.power, power);
                            cachedOrigins.get(action.origin).power = out;
                            return out;
                        }
                        return power == null ? 0 : power;
                    });
            }
            for (Origin origin : cachedOrigins)
                origin.send();
            for (Map.Entry<BlockPos, Integer> msg : messages.entrySet())
                if (!INVALID_POS.equals(msg.getKey()))
                    new Keyboard.KeyboardItemMsg(msg.getKey(), msg.getValue()).sendToServer();
        });
    }

    public static boolean consumeKeyPress(int key) {
        return cachedMappings.containsKey(-key) || prevCachedMappings.contains(-key);
    }
}
