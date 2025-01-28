package com.limachi.arss.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * reproduces the way a vanilla chest works in the form of an interface that can be implemented on any extended Object
 * expected to be implemented on BlockEntity, but should work for other objects too
 */
public interface MinimalListInventory extends Container {
    /**
     * demonstration of a minimal implementation
     */
    class MinimalListInventorySized implements MinimalListInventory {
        private NonNullList<ItemStack> stacks;
        public MinimalListInventorySized(int size) { resetSize(size); }
        @Override
        public NonNullList<ItemStack> getItems() { return stacks; }
        @Override
        public void setItems(NonNullList<ItemStack> items) { stacks = items; }

        @Override
        public void setChanged() {}
    }

    /**
     * demonstration of a minimal implementation for BlockEntity
     */
    class MinimalListInventoryBlockEntity extends BlockEntity implements MinimalListInventory {
        private NonNullList<ItemStack> stacks;
        public MinimalListInventoryBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, int maxSize) {
            super(blockEntityType, blockPos, blockState);
            resetSize(maxSize);
        }
        @Override
        public NonNullList<ItemStack> getItems() { return stacks; }
        @Override
        public void setItems(NonNullList<ItemStack> items) { stacks = items; }

        @Override
        protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
            super.loadAdditional(tag, provider);
            clearContent();
            if (!(this instanceof RandomizableContainer r) || !r.tryLoadLootTable(tag))
                ContainerHelper.loadAllItems(tag, getItems(), provider);
        }

        @Override
        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
            super.saveAdditional(tag, provider);
            if (!(this instanceof RandomizableContainer r) || !r.trySaveLootTable(tag))
                ContainerHelper.saveAllItems(tag, getItems(), provider);
        }
    }

    NonNullList<ItemStack> getItems();
    void setItems(NonNullList<ItemStack> items);

    default void resetSize(int size) {
        setItems(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    @Override
    default int getContainerSize() { return getItems().size(); }

    @Override
    default boolean isEmpty() {
        for (ItemStack item : getItems())
            if (!item.isEmpty())
                return false;
        return true;
    }

    @Override
    default ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    @Override
    default ItemStack removeItem(int slot, int qty) {
        ItemStack itemStack = ContainerHelper.removeItem(getItems(), slot, qty);
        if (!itemStack.isEmpty())
            setChanged();
        return itemStack;
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.getItems(), slot);
    }

    @Override
    default void setItem(int slot, ItemStack item) {
        getItems().set(slot, item);
        item.limitSize(this.getMaxStackSize(item));
        setChanged();
    }

    @Override
    default boolean stillValid(Player player) {
        if (this instanceof BlockEntity be)
            return Container.stillValidBlockEntity(be, player);
        return true;
    }

    @Override
    default void clearContent() {
        getItems().clear();
    }
}
