package com.limachi.arss.items;

import com.limachi.arss.Arss;
import com.limachi.lim_lib.LimLib;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Arss.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SculkFrequenciesBook extends CustomRecipe {

    public static final String KEY = "crafting_special_sculk_book";
    public static final RecipeSerializer<SculkFrequenciesBook> SERIALIZER = new SimpleCraftingRecipeSerializer<>((p1, p2)->new SculkFrequenciesBook());

    public SculkFrequenciesBook() { super(new ResourceLocation(Arss.MOD_ID, KEY), CraftingBookCategory.REDSTONE); }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, new ResourceLocation(Arss.MOD_ID, KEY), ()->SERIALIZER);
    }

    @SubscribeEvent
    public static void addBookToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == LimLib.INSTANCES.get(Arss.MOD_ID).tab().get())
            event.accept(book(true));
    }

    @Override
    public boolean matches(CraftingContainer container, @Nonnull Level level) {
        if (canCraftInDimensions(container.getWidth(), container.getHeight())) {
            boolean hasBook = false;
            boolean hasSculk = false;
            for (ItemStack stack : container.getItems())
                if (stack.is(Items.WRITABLE_BOOK)) {
                    if (hasBook)
                        return false;
                    hasBook = true;
                } else if (stack.is(Items.SCULK_SENSOR) || stack.is(Items.CALIBRATED_SCULK_SENSOR)) {
                    if (hasSculk)
                        return false;
                    hasSculk = true;
                } else if (!stack.isEmpty())
                    return false;
            return hasSculk && hasBook;
        }
        return false;
    }

    private static ItemStack book(boolean withLore) {
        ItemStack out = new ItemStack(Items.WRITTEN_BOOK, 1);
        CompoundTag tag = out.getOrCreateTag();
        tag.putString("author", "The Sculk");
        tag.putString("title", "Sculk Vibrations");
        tag.putString("filtered_title", "I'm pickin' up good vibrations");
        ListTag pages = new ListTag();
        for (int i = 1; i <= 15; ++i)
            pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("book.sculk_vibrations.page_" + i))));
        tag.put("pages", pages);
        if (withLore)
            tag.put("display", Util.make(new CompoundTag(), c->c.put("Lore", Util.make(new ListTag(), l->l.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("book.sculk_vibrations.creative_tab_help"))))))));
        return out;
    }

    @Override
    @Nonnull
    public ItemStack assemble(CraftingContainer container, @Nonnull RegistryAccess registryAccess) {
        ItemStack out = ItemStack.EMPTY;
        if (canCraftInDimensions(container.getWidth(), container.getHeight())) {
            ItemStack book = ItemStack.EMPTY;
            boolean hasSculk = false;
            for (ItemStack stack : container.getItems())
                if (stack.is(Items.WRITABLE_BOOK)) {
                    if (!book.isEmpty())
                        return out;
                    book = stack;
                } else if (stack.is(Items.SCULK_SENSOR) || stack.is(Items.CALIBRATED_SCULK_SENSOR)) {
                    if (hasSculk)
                        return out;
                    hasSculk = true;
                } else if (!stack.isEmpty())
                    return out;
            if (!book.isEmpty() && hasSculk) {
                out = book(false);
            }
        }
        return out;
    }

    @Override
    @Nonnull
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack stack = container.getItem(i);
            if (stack.hasCraftingRemainingItem()) {
                nonnulllist.set(i, stack.getCraftingRemainingItem());
            } else if (stack.is(Items.SCULK_SENSOR) || stack.is(Items.CALIBRATED_SCULK_SENSOR)) {
                nonnulllist.set(i, stack.copyWithCount(1));
                break;
            }
        }
        return nonnulllist;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return Math.max(width, height) >= 2; }

    @Override
    @Nonnull
    public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
}
