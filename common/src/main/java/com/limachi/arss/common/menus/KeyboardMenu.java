package com.limachi.arss.common.menus;

import com.limachi.arss.common.items.Keyboard;
import com.limachi.arss.utils.annotations.RegisterMenu;
import com.limachi.arss.utils.menu.IAcceptUpStreamNBT;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public class KeyboardMenu extends AbstractContainerMenu implements IAcceptUpStreamNBT {

    public final InteractionHand hand;
    public final Inventory inv;

    @RegisterMenu
    public static RegistrySupplier<MenuType<KeyboardMenu>> MENU;

    public KeyboardMenu(int id, Inventory playerInv, boolean offHand) {
        super(MENU.get(), id);
        hand = offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        inv = playerInv;
    }

    public KeyboardMenu(int id, Inventory playerInv, FriendlyByteBuf buff) {
        this(id, playerInv, buff.readBoolean());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) { return player.getItemInHand(hand).getItem() instanceof Keyboard; }

    public static void open(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer)
            MenuRegistry.openExtendedMenu(serverPlayer, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return player.getItemInHand(hand).getDisplayName();
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new KeyboardMenu(id, inventory, hand == InteractionHand.OFF_HAND);
                }
            }, b->b.writeBoolean(hand == InteractionHand.OFF_HAND));
    }

    @Override
    public void upstreamNBTMessage(int power, CompoundTag binding) {
        ItemStack stack = inv.player.getItemInHand(hand);
        if (stack.getItem() instanceof Keyboard)
            Keyboard.setBinding(stack, power, binding.getInt("binding"));
    }
}
