package com.limachi.arss.menu;

import com.limachi.arss.blockEntities.AnalogJukeboxBlockEntity;
import com.limachi.arss.blocks.AnalogJukeboxBlock;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class AnalogJukeboxMenu extends AbstractContainerMenu {

    @RegisterMenu(name = "analog_jukebox")
    public static RegistryObject<MenuType<AnalogJukeboxMenu>> MENU;

    private final ContainerLevelAccess accessor;

    protected AnalogJukeboxMenu(int id, Inventory playerInv, Container container, BlockPos pos) {
        super(MENU.get(), id);
        accessor = ContainerLevelAccess.create(playerInv.player.level(), pos);
        for (int row = 0; row < 2; ++row)
            for (int column = 0; column < 8; ++column) {
                if (row == 0 && column == 0) continue;
                addSlot(new Slot(container, row * 8 + column - 1, 17 + column * 18, 25 + row * 29 ){
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.getItem() instanceof RecordItem;
                    }
                });
            }
        for (int row = 0; row < 3; ++row)
            for (int column = 0; column < 9; ++column)
                addSlot(new Slot(playerInv, 9 + row * 9 + column, 8 + column * 18, 84 + row * 18));
        for (int column = 0; column < 9; ++column)
            addSlot(new Slot(playerInv, column, 8 + column * 18, 142));
    }

    public AnalogJukeboxMenu(int id, Inventory playerInv, FriendlyByteBuf buff) {
        this(id, playerInv, new SimpleContainer(15), BlockPos.ZERO);
    }

    @Override
    public @Nonnull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 15) {
                if (!this.moveItemStackTo(itemstack1, 15, this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 15, false))
                return ItemStack.EMPTY;

            if (itemstack1.isEmpty())
                slot.setByPlayer(ItemStack.EMPTY);
            else
                slot.setChanged();
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) { return stillValid(accessor, player, AnalogJukeboxBlock.R_BLOCK.get()); }

    public static void open(Player player, AnalogJukeboxBlockEntity be) {
        Level level = be.getLevel();
        if (level != null && !level.isClientSide())
            NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                @Override
                public @Nonnull Component getDisplayName() { return Component.translatable("screen.title.analog_jukebox"); }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p_39956_) { return new AnalogJukeboxMenu(id, inventory, be, be.getBlockPos()); }
            });
    }
}
