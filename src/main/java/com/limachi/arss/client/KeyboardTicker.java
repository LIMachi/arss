package com.limachi.arss.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.limachi.arss.Arss;
import com.limachi.arss.blockEntities.KeyboardLecternBlockEntity;
import com.limachi.arss.blocks.KeyboardLecternBlock;
import com.limachi.arss.items.KeyboardItem;
import com.limachi.lim_lib.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@Mod.EventBusSubscriber(modid = Arss.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
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
                NetworkManager.toServer(new KeyboardItem.KeyPressVisualFeedbackSlotMsg(playerSlot, power));
            else if (lectern != null)
                NetworkManager.toServer(new KeyboardLecternBlock.KeyPressVisualFeedbackLecternMsg(lectern, power));
        }
    }

    private static final ArrayList<Origin> cachedOrigins = new ArrayList<>();
    private static final Multimap<Integer, KeyAction> cachedMappings = ArrayListMultimap.create();
    private static Set<Integer> prevCachedMappings = new HashSet<>();

    private static final BlockPos INVALID_POS = new BlockPos(30000001, 1023, 30000001);

    private static void addAllBindings(BlockPos fromPos, CompoundTag tag) {
        if (tag.contains("bindings", Tag.TAG_INT_ARRAY)) {
            BlockPos pos = INVALID_POS;
            if (tag.contains("target")) {
                pos = BlockPos.of(tag.getLong("target"));
                if (pos.distSqr(fromPos) > KeyboardItem.KEYBOARD_REACH * KeyboardItem.KEYBOARD_REACH)
                    pos = INVALID_POS;
            }
            int[] bindings = tag.getIntArray("bindings");
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
            if (stack.getItem() instanceof KeyboardItem && stack.getTag() != null) {
                cachedOrigins.add(new Origin(i));
                if (KeyboardItem.inputActive(stack.getTag()))
                    addAllBindings(playerPos, stack.getTag());
            }
        }
        for (KeyboardLecternBlockEntity be : KeyboardLecternBlockEntity.getControlled(player)) {
            CompoundTag tag = be.getKeyboard().getTag();
            if (tag != null) {
                cachedOrigins.add(new Origin(be.getBlockPos()));
                addAllBindings(be.getBlockPos(), tag);
            }
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER || Minecraft.getInstance().player == null || !Minecraft.getInstance().player.getUUID().equals(event.player.getUUID()))
            return;
        reloadCache(event.player);
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
                NetworkManager.toServer(new KeyboardItem.KeyboardItemMsg(msg.getKey(), msg.getValue()));
    }

    public static boolean consumeKeyPress(int key) {
        return cachedMappings.containsKey(-key) || prevCachedMappings.contains(-key);
    }
}
