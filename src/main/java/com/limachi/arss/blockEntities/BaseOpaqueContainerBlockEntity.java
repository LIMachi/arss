package com.limachi.arss.blockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities; //VERSION 1.19.2
//import net.minecraftforge.items.CapabilityItemHandler; //VERSION 1.18.2
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * simplified version of BaseContainerBlockEntity, without MenuProvider (GUI) or Nameable (display name)
 * also provide a default load and save function to store the container as an ordered list of items
 */
@ParametersAreNonnullByDefault
public class BaseOpaqueContainerBlockEntity extends BlockEntity implements Container {

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(this::createUnSidedHandler);

    protected List<ItemStack> items = new ArrayList<>();
    protected int maxSize;

    protected BaseOpaqueContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxSize) {
        super(type, pos, state);
        this.maxSize = maxSize;
        for (int i = 0; i < maxSize; ++i)
            items.add(ItemStack.EMPTY);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ListTag list = tag.getList("Items", 10);
        for (int i = 0; i < list.size(); ++i)
            setItem(i, ItemStack.of(list.getCompound(i)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        ListTag list = new ListTag();
        for (int i = 0; i < getContainerSize(); ++i)
            list.add(getItem(i).serializeNBT());
        tag.put("Items", list);
    }

    protected @Nonnull IItemHandler createUnSidedHandler() { return new InvWrapper(this); }

    public @Nonnull <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (!this.remove && cap ==
//                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY //VERSION 1.18.2
                ForgeCapabilities.ITEM_HANDLER //VERSION 1.19.2
        )
            return itemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = net.minecraftforge.common.util.LazyOptional.of(this::createUnSidedHandler);
    }

    @Override
    public int getContainerSize() { return items.size(); }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); ++i)
            if (!getItem(i).isEmpty()) return false;
        return true;
    }

    @Override
    public @Nonnull ItemStack getItem(int i) { return i >= 0 && i < getContainerSize() ? items.get(i) : ItemStack.EMPTY; }

    @Override
    public @Nonnull ItemStack removeItem(int i, int qty) {
        if (qty <= 0) return ItemStack.EMPTY;
        ItemStack stack = getItem(i);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = stack.split(qty);
        setItem(i, stack);
        return out;
    }

    @Override
    public @Nonnull ItemStack removeItemNoUpdate(int i) {
        if (i < 0 || i >= getContainerSize()) return ItemStack.EMPTY;
        ItemStack stack = items.get(i);
        items.set(i, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int i, ItemStack stack) {
        if (i >= 0 && i < getContainerSize())
            items.set(i, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) { return false; }

    @Override
    public void clearContent() {
        for (int i = 0; i < getContainerSize(); ++i)
            items.set(i, ItemStack.EMPTY);
        setChanged();
    }
}
