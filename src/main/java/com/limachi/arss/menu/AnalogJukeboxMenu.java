package com.limachi.arss.menu;

import com.limachi.arss.Registries;
import com.limachi.arss.blockEntities.AnalogJukeboxBlockEntity;
import com.limachi.arss.blocks.AnalogJukebox;
import com.limachi.arss.utils.StaticInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import org.lwjgl.system.NonnullDefault;

@StaticInitializer.Static
@NonnullDefault
public class AnalogJukeboxMenu extends AbstractContainerMenu {

    public static final RegistryObject<MenuType<AnalogJukeboxMenu>> MENU = Registries.MENU_REGISTER.register("analog_jukebox", ()->new MenuType<>(AnalogJukeboxMenu::new));

    private final ContainerLevelAccess accessor;

    //use 'open' server side
    protected AnalogJukeboxMenu(int id, Inventory playerInv, Container container, BlockPos pos) {
        super(MENU.get(), id);
        accessor = ContainerLevelAccess.create(playerInv.player.level, pos);
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

    //Client only
    public AnalogJukeboxMenu(int id, Inventory playerInv) {
        this(id, playerInv, new SimpleContainer(15), BlockPos.ZERO);
    }

    //disable shift-click
    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) { return stillValid(accessor, player, AnalogJukebox.R_BLOCK.get()); }

    public static void open(Player player, AnalogJukeboxBlockEntity be) {
        Level level = be.getLevel();
        if (level != null && !level.isClientSide())
            NetworkHooks.openGui((ServerPlayer) player, new MenuProvider() {
                @Override
                public Component getDisplayName() { return new TranslatableComponent("screen.title.analog_jukebox"); }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p_39956_) { return new AnalogJukeboxMenu(id, inventory, be, be.getBlockPos()); }
            });
    }
}
