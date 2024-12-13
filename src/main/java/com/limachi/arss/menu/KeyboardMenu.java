package com.limachi.arss.menu;

import com.limachi.arss.items.KeyboardItem;
import com.limachi.lim_lib.menus.IAcceptUpStreamNBT;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
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
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class KeyboardMenu extends AbstractContainerMenu implements IAcceptUpStreamNBT {

    public final InteractionHand hand;
    public final Inventory inv;

    @RegisterMenu
    public static RegistryObject<MenuType<KeyboardMenu>> MENU;

    public KeyboardMenu(int id, Inventory playerInv, boolean offHand) {
        super(MENU.get(), id);
        hand = offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        inv = playerInv;
    }

    public KeyboardMenu(int id, Inventory playerInv, FriendlyByteBuf buff) {
        this(id, playerInv, buff.readBoolean());
    }

    @Override
    public @Nonnull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) { return player.getItemInHand(hand).getItem() instanceof KeyboardItem; }

    public static void open(Player player, InteractionHand hand) {
        if (!player.level().isClientSide())
            NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                @Override
                public @Nonnull Component getDisplayName() { return player.getItemInHand(hand).getDisplayName(); }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p_39956_) { return new KeyboardMenu(id, inventory, hand == InteractionHand.OFF_HAND); }
            }, b->b.writeBoolean(hand == InteractionHand.OFF_HAND));
    }

    @Override
    public void upstreamNBTMessage(int power, CompoundTag binding) {
        ItemStack stack = inv.player.getItemInHand(hand);
        if (stack.getItem() instanceof KeyboardItem)
            KeyboardItem.setBinding(stack.getOrCreateTag(), power, binding.getInt("binding"));
    }
}
