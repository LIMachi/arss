package com.limachi.arss.common.items;

import com.limachi.arss.client.ClientDef;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.Stage;
import com.limachi.arss.utils.annotations.StaticInit;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.registry.CreativeTabRegistry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
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

    private static ItemStack book(boolean withLore) {
        ItemStack out = new ItemStack(Items.WRITTEN_BOOK, 1);
        out.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(TITLE, "The Sculk", 0, PAGES, false));
        if (withLore)
            out.set(DataComponents.LORE, LORE);
        return out;
    }

    @StaticInit
    public static void registerClickEvent() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.is(Items.BOOK) || stack.is(Items.WRITABLE_BOOK) || stack.is(Items.WRITTEN_BOOK)) {
                    BlockState state = serverPlayer.level().getBlockState(pos);
                    if (state.is(Blocks.SCULK_SENSOR) || state.is(Blocks.CALIBRATED_SCULK_SENSOR))
                        serverPlayer.setItemInHand(hand, book(false));
                }
            }
            return EventResult.pass();
        });
    }

    @StaticInit(Stage.ITEM)
    public static void putBookInCreativeTab() {
        CreativeTabRegistry.appendStack(ModBase.registries.default_tab, book(true));
    }
}
