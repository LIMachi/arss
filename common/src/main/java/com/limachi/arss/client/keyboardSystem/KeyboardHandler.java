package com.limachi.arss.client.keyboardSystem;

import com.limachi.arss.common.ArssItemStackComponents;
import com.limachi.arss.common.block_entities.KeyboardLectern;
import com.limachi.arss.common.items.Keyboard;

import com.limachi.lim_lib.client.annotations.RegisterClientEventListener;
import com.limachi.lim_lib.client.modCreation.ClientEvents;
import com.limachi.lim_lib.client.utils.MidiHandler;
import com.limachi.lim_lib.common.annotations.RegisterMsg;
import com.limachi.lim_lib.common.network.IC2SMsg;
import dev.architectury.networking.NetworkManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class KeyboardHandler {
    protected static final HashMap<Integer, ArssItemStackComponents.Bindings> knownKeyboards = new HashMap<>();
    protected static final HashMap<KeyboardLectern, ArssItemStackComponents.Bindings> knownLecterns = new HashMap<>();

    public static boolean stopKeyPress(int key) {
        if (key >= 0) {
            for (var b : knownKeyboards.values())
                for (var k : b.key())
                    if (k == -key)
                        return true;
            for (var b : knownLecterns.values())
                for (var k : b.key())
                    if (k == -key)
                        return true;
        }
        return false;
    }

    public static void useLectern(KeyboardLectern lectern, boolean use) {
        if (use)
            knownLecterns.put(lectern, Keyboard.getBindings(lectern.getKeyboard()));
        else
            knownLecterns.remove(lectern);
    }

    protected static long applyBinding(ArssItemStackComponents.Bindings bindings, long window) {
        long mask = 0;
        for (int i = 0; i < 15; ++i) {
            int compact = bindings.key()[i];
            if (compact == -1)
                continue;
            long power = 0;
            if (compact < 0) {
                if (GLFW.glfwGetKey(window, -compact) == GLFW.GLFW_PRESS)
                    power = 15;
            } else {
                power = MidiHandler.keyState(compact >> 8, compact & 0xFF);
                if (power > 0) {
                    power = ((long) Math.floor(((double) power / 127.) * 14.) & 0xF);
                    if (power < 0xF)
                        ++power;
                }
            }
            mask |= power << (i * 4);
        }
        return mask;
    }

    @RegisterMsg
    public record KeyboardStateMask(int slot, long mask) implements IC2SMsg<KeyboardStateMask> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            if (ctx.getPlayer() instanceof ServerPlayer player)
                Keyboard.setKeyStates(player.getInventory().getItem(slot), mask, true);
        }
    }

    @RegisterMsg
    public record LecternStateMask(BlockPos pos, long mask) implements IC2SMsg<LecternStateMask> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            if (ctx.getPlayer() instanceof ServerPlayer player && player.level().getBlockEntity(pos) instanceof KeyboardLectern lectern) {
                if (lectern.getCurrentUser() != player) {
                    //FIXME: send a disconnect message (the user changed but the client missed the last message somehow)
                }
                else if (Keyboard.setKeyStates(lectern.getKeyboard(), mask, true))
                    lectern.setChanged();
            }
        }
    }

    protected static void keyboardInInventory(Inventory inv, long window) {
        knownKeyboards.clear();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.has(ArssItemStackComponents.BINDINGS.get()) && Keyboard.isListening(stack)) {
                var bindings = stack.get(ArssItemStackComponents.BINDINGS.get());
                knownKeyboards.put(i, bindings);
                new KeyboardStateMask(i, applyBinding(bindings, window)).sendToServer();
            }
        }
    }

    @RegisterClientEventListener(ClientEvents.TICK_PRE)
    public static void tick(Minecraft mc) {
        if (mc.player == null) return;
        long window = Minecraft.getInstance().getWindow().getWindow();
        keyboardInInventory(mc.player.getInventory(), window);
        knownLecterns.entrySet().removeIf(e -> {
            if (e.getValue() == null || !mc.player.position().closerThan(e.getKey().getBlockPos().getCenter(), KeyboardLectern.LECTERN_REACH) || !Keyboard.isListening(e.getKey().getKeyboard()))
                return true;
            new LecternStateMask(e.getKey().getBlockPos(), applyBinding(e.getValue(), window)).sendToServer();
            return false;
        });
    }
}
