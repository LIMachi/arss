package com.limachi.arss.common.recipes;

import com.limachi.arss.Arss;
import com.limachi.arss.common.items.RedstoneBooster;
import com.limachi.lim_lib.common.annotations.StaticInit;
import com.limachi.lim_lib.common.modCreation.Stage;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;

public class RedstoneBoosterRecipe extends CustomRecipe {
    public static RegistrySupplier<RecipeSerializer<RedstoneBoosterRecipe>> TYPE;

    @StaticInit(Stage.BLOCK_ENTITY)
    public static void register() {
        TYPE = Arss.INSTANCE.registries.recipes.register("redstone_booster", ()->new SimpleCraftingRecipeSerializer<>(RedstoneBoosterRecipe::new));
    }

    public RedstoneBoosterRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput recipeInput, Level level) {
        if (recipeInput.width() != 3 || recipeInput.height() != 3)
            return false;
        for (int i = 0; i < 9; ++i)
            if (i != 4 && !(recipeInput.getItem(i).getItem() instanceof BlockItem bi && bi.getBlock() instanceof RedStoneWireBlock))
                return false;
        ItemStack potion = recipeInput.getItem(4);
        if (potion.getItem() instanceof PotionItem && potion.get(DataComponents.POTION_CONTENTS) instanceof PotionContents p) {
            boolean[] ok = {false};
            p.forEachEffect(i->{
                if (i.getEffect().is(MobEffects.MOVEMENT_SPEED) && i.getAmplifier() > 0)
                    ok[0] = true;
            });
            return ok[0];
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput recipeInput, HolderLookup.Provider provider) {
        return new ItemStack(RedstoneBooster.R_ITEM.get());
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i >= 3 && j >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TYPE.get();
    }
}
