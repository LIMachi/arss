package com.limachi.arss.common.block_entities;

import com.limachi.arss.utils.MinimalListInventory;
import com.limachi.arss.utils.Stage;
import com.limachi.arss.utils.annotations.RegisterBlockEntity;
import com.limachi.arss.utils.annotations.StaticInit;
import com.mojang.datafixers.util.Pair;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.HashMap;
import java.util.HashSet;

public class InstrumentSwapper extends MinimalListInventory.MinimalListInventoryBlockEntity {

    @RegisterBlockEntity
    public static RegistrySupplier<BlockEntityType<BlockEntity>> TYPE;

    private static final HashMap<Item, ResourceLocation> EASTER_EGGS = new HashMap<>();
    private static final HashSet<ResourceLocation> TUNABLE_CUSTOM_SOUND = new HashSet<>();

    public static void registerEasterEggSound(Item item, String soundEvent, boolean tunable) {
        registerEasterEggSound(item, ResourceLocation.withDefaultNamespace(soundEvent), tunable);
    }
    public static void registerEasterEggSound(Item item, ResourceLocation soundEvent, boolean tunable) {
        EASTER_EGGS.put(item, soundEvent);
        if (tunable)
            TUNABLE_CUSTOM_SOUND.add(soundEvent);
    }

    public static boolean isTunable(ResourceLocation getCustomSoundId) { return TUNABLE_CUSTOM_SOUND.contains(getCustomSoundId); }

    @StaticInit(Stage.ITEM)
    public static void setEasterEggs() {
        registerEasterEggSound(Items.AMETHYST_SHARD, "block.amethyst_block.resonate", true);
    }

    public InstrumentSwapper(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state, 16); }

    public void dropAllInstruments() {
        if (level instanceof ServerLevel) {
            for (int i = 0; i < getContainerSize(); ++i) {
                ItemStack stack = getItem(i);
                if (!stack.isEmpty()) {
                    double d0 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
                    double d1 = (double)(level.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
                    double d2 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
                    ItemEntity itementity = new ItemEntity(level, (double)worldPosition.getX() + d0, (double)worldPosition.getY() + d1, (double)worldPosition.getZ() + d2, stack);
                    itementity.setDefaultPickUpDelay();
                    level.addFreshEntity(itementity);
                }
            }
            clearContent();
            level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
        }
    }

    public ResourceLocation customSkullSound() {
        ItemStack stack = getItem(getBlockState().getValue(BlockStateProperties.POWER));
        if (EASTER_EGGS.get(stack.getItem()) instanceof ResourceLocation rl)
            return rl;
        if (stack.has(DataComponents.NOTE_BLOCK_SOUND))
            return stack.get(DataComponents.NOTE_BLOCK_SOUND);
        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof EntityBlock ebi)
            if (ebi.newBlockEntity(worldPosition, bi.getBlock().defaultBlockState()) instanceof SkullBlockEntity skull) {
                if (stack.has(DataComponents.BLOCK_ENTITY_DATA) && level != null)
                    skull.loadCustomOnly(stack.get(DataComponents.BLOCK_ENTITY_DATA).copyTag(), level.registryAccess());
                return skull.getNoteBlockSound();
            }
        return null;
    }

    public static boolean accept(ItemStack stack) {
        return EASTER_EGGS.containsKey(stack.getItem()) || stack.has(DataComponents.NOTE_BLOCK_SOUND) || stack.getItem() instanceof BlockItem;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return accept(stack) && super.canPlaceItem(slot, stack);
    }

    public void updateInstrument(BlockState state) {
        if (level instanceof ServerLevel) {
            ItemStack stack = getItem(state.getValue(BlockStateProperties.POWER));
            if (EASTER_EGGS.containsKey(stack.getItem()) || stack.has(DataComponents.NOTE_BLOCK_SOUND))
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT, NoteBlockInstrument.CUSTOM_HEAD));
            else if (stack.getItem() instanceof BlockItem bi)
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT, bi.getBlock().defaultBlockState().instrument()));
            else
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT, NoteBlockInstrument.HARP));
        }
    }

    @Override
    public void setChanged() {
        updateInstrument(getBlockState());
        super.setChanged();
    }
}
