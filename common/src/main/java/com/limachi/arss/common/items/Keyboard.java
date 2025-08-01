package com.limachi.arss.common.items;

import com.limachi.arss.client.ClientDef;
import com.limachi.arss.client.screen.KeyboardScreen;
import com.limachi.arss.common.ArssItemStackComponents;
import com.limachi.arss.common.block_entities.ResonantGateBlockEntity;

import com.limachi.lim_lib.client.annotations.ItemTinter;
import com.limachi.lim_lib.common.annotations.RegisterEventListener;
import com.limachi.lim_lib.common.annotations.RegisterItem;
import com.limachi.lim_lib.common.annotations.RegisterMsg;
import com.limachi.lim_lib.common.items.IItemMixin;
import com.limachi.lim_lib.common.modCreation.Events;
import com.limachi.lim_lib.common.network.IC2SMsg;
import com.limachi.lim_lib.common.utils.Game;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

import static com.limachi.arss.common.ArssItemStackComponents.*;

import java.util.List;

@SuppressWarnings("unused")
public class Keyboard extends Item implements IItemMixin {
    @RegisterItem
    public static RegistrySupplier<Item> R_ITEM;

    @RegisterMsg
    public record KeyPressVisualFeedbackSlotMsg(int slot, int keyStates) implements IC2SMsg<KeyPressVisualFeedbackSlotMsg> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            ItemStack stack = ctx.getPlayer().getInventory().getItem(slot);
            if (stack.getItem() instanceof Keyboard)
                stack.set(OUTPUT.get(), keyStates);
        }
    }

    public static int getTint(int index, int states, boolean recording) {
        if (index == 15)
            return 0xFF000000 | RedStoneWireBlock.getColorForPower(recording ? 15 : 0);
        if (index >= 0 && index < 15 && (states & (1 << index)) != 0)
            return 0xFF00FFFF;
        return -1;
    }

    @ItemTinter
    public static int getTint(ItemStack stack, int index) {
        return getTint(index, stack.getOrDefault(OUTPUT.get(), 0), stack.getOrDefault(CATCH.get(), false));
    }

    public Keyboard(Properties props) { super(props.stacksTo(1).component(OUTPUT.get(), 0).component(CATCH.get(), false).component(BINDINGS.get(), Bindings.empty())); }

    public static void setListening(ItemStack stack, boolean listening) {
        if (stack.has(CATCH.get()))
            stack.set(CATCH.get(), listening);
    }

    public static boolean isListening(ItemStack stack) {
        if (stack.has(CATCH.get()))
            return stack.get(CATCH.get());
        return false;
    }

    public static boolean setKeyStates(ItemStack stack, long mask, boolean andUpdate) {
        if (stack.has(ArssItemStackComponents.OUTPUT.get()) && stack.has(ArssItemStackComponents.BINDINGS.get())) {
            int prev = stack.get(ArssItemStackComponents.OUTPUT.get());
            var bindings = stack.get(ArssItemStackComponents.BINDINGS.get());
            int pressedMask = 0;
            for (int i = 0; i < 15; ++i) {
                long power = ((mask >> (i * 4)) & 0xF);
                boolean pressed = power > 0;
                if (pressed == ((prev & (1 << i)) == 0)) {
                    if (pressed) {
                        if (bindings.power()[i] == 16)
                            ResonantGateBlockEntity.setDirect(bindings.freq()[i], (int)power);
                        else
                            ResonantGateBlockEntity.setDirect(bindings.freq()[i], bindings.power()[i]);
                    } else
                        ResonantGateBlockEntity.setDirect(bindings.freq()[i], 0);
                }
                if (pressed)
                    pressedMask |= 1 << i;
            }
            stack.set(ArssItemStackComponents.OUTPUT.get(), pressedMask);
            return true;
        }
        return false;
    }

    @RegisterMsg
    public record KeyboardKeypressMsg(byte key) implements IC2SMsg<KeyboardKeypressMsg> {
        public static void sendKeyPress(int key, InteractionHand hand) {
            new KeyboardKeypressMsg((byte)((key & 0xF) + (hand == InteractionHand.MAIN_HAND ? 0 : 0x10))).sendToServer();
        }
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            int pressed = key & 0xF;
            InteractionHand hand = (key & 0x10) == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack stack = ctx.getPlayer().getItemInHand(hand);
            if (stack.is(Keyboard.R_ITEM.get())) {

            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> text, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, text, flags);
        ClientDef.commonHoverText("keyboard_item", text);
    }

    public static Bindings getBindings(ItemStack stack) {
        var out = stack.get(BINDINGS.get());
        if (out == null)
            return Bindings.empty();
        return out;
    }

    public static boolean inputActive(ItemStack stack) {
        return stack.getOrDefault(CATCH.get(), false);
    }

    public static void setInputState(ItemStack stack, boolean active) {
        stack.set(CATCH.get(), active);
    }

    public static boolean toggleInputState(ItemStack stack) {
        boolean state = !stack.getOrDefault(CATCH.get(), false);
        stack.set(CATCH.get(), state);
        return state;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
//        if (state.is(Blocks.LECTERN) && !state.getValue(LecternBlock.HAS_BOOK)) {
//            KeyboardLectern.replaceLectern(ctx.getLevel(), ctx.getClickedPos(), state, ctx.getItemInHand().copy());
//            if (!(ctx.getPlayer() instanceof Player player && player.isCreative()))
//                ctx.getItemInHand().setCount(0);
//        }
        if (ctx.getPlayer() instanceof Player player)
            return use(ctx.getLevel(), player, ctx.getHand()).getResult();
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown())
            Game.runLogical(()->()-> KeyboardScreen.client_open(player, hand), null);
        else
            player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard." + toggleInputState(player.getItemInHand(hand))), true);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack newStack, ItemStack oldStack, int slot) {
        return !(newStack.is(R_ITEM.get()) && oldStack.is(R_ITEM.get()));
    }

    @RegisterEventListener(Events.PLAYER_QUIT)
    public static void onPlayerQuitWhileUsingKeyboard(ServerPlayer player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.has(CATCH.get()) && stack.get(CATCH.get()) == true)
                stack.set(CATCH.get(), false);
        }
    }
}
