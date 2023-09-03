package com.limachi.arss.items;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Arss.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RedstoneBooster extends Item {

    @RegisterItem
    public static RegistryObject<Item> R_ITEM;

    @SubscribeEvent
    public static void registerRecipe(FMLCommonSetupEvent event) {
        event.enqueueWork(()->BrewingRecipeRegistry.addRecipe(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRONG_SWIFTNESS)), Ingredient.of(Items.REDSTONE_BLOCK), new ItemStack(R_ITEM.get())));
    }

    public RedstoneBooster() { super(new Properties().durability(16)); }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return super.damageItem(stack, amount, entity, onBroken);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.hasProperty(ArssBlockStateProperties.BOOSTED) && !state.getValue(ArssBlockStateProperties.BOOSTED)) {
            context.getLevel().setBlockAndUpdate(context.getClickedPos(), state.setValue(ArssBlockStateProperties.BOOSTED, true));
            if (context.getPlayer() instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("display.arss.redstone_booster.used"), true);
                if (!player.isCreative())
                    if (stack.hurt(1, context.getLevel().random, player))
                        stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}