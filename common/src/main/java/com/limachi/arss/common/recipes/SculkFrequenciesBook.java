package com.limachi.arss.common.recipes;

import com.limachi.arss.Arss;

import com.limachi.lim_lib.common.annotations.RegisterEventListener;
import com.limachi.lim_lib.common.annotations.StaticInit;
import com.limachi.lim_lib.common.modCreation.Events;
import com.limachi.lim_lib.common.modCreation.Stage;

import dev.architectury.event.EventResult;
import dev.architectury.registry.CreativeTabRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SculkFrequenciesBook {

    public static final ArrayList<Filterable<Component>> PAGES = new ArrayList<>(15);
    public static final Filterable<String> TITLE = new Filterable<>("Sculk Vibrations", Optional.of("I'm pickin' up good vibrations"));
    public static final ItemLore LORE = new ItemLore(List.of(Component.translatable("book.sculk_vibrations.creative_tab_help")));

    static {
        for (int i = 1; i <= 15; ++i)
            PAGES.add(new Filterable<>(Component.translatable("book.sculk_vibrations.page_" + i), Optional.empty()));
    }

    private static ItemStack createBook(int count) {
        ItemStack out = new ItemStack(Items.WRITTEN_BOOK, count);
        out.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(TITLE, "The Sculk", 0, PAGES, false));
        out.set(DataComponents.LORE, LORE);
        return out;
    }

    @RegisterEventListener(Events.RIGHT_CLICK_BLOCK)
    public static EventResult clickBookOnSculk(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(Items.BOOK) || stack.is(Items.WRITABLE_BOOK) || stack.is(Items.WRITTEN_BOOK)) {
                BlockState state = serverPlayer.level().getBlockState(pos);
                if (state.is(Blocks.SCULK_SENSOR) || state.is(Blocks.CALIBRATED_SCULK_SENSOR))
                    serverPlayer.setItemInHand(hand, createBook(stack.getCount()));
            }
        }
        return EventResult.pass();
    }

    @StaticInit(Stage.ITEM) //FIXME: seem to fail on servers (but does not cause a crash, and the item is visible in the creative tab)
    public static void putBookInCreativeTab() {
        CreativeTabRegistry.appendStack(Arss.INSTANCE.registries.default_tab, createBook(1));
    }
}
