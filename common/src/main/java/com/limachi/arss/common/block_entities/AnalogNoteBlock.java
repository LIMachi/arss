package com.limachi.arss.common.block_entities;

import com.limachi.arss.utils.annotations.RegisterBlockEntity;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AnalogNoteBlock extends BlockEntity {

    private int previousInput = 0;
    private int transitiveState = 0;
    private long tick = 0;

    @RegisterBlockEntity
    public static RegistrySupplier<BlockEntityType<AnalogNoteBlock>> TYPE;

    public AnalogNoteBlock(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("previous", getPreviousInput());
    }

    public boolean shouldTick() {
        if (level != null && level.getGameTime() != tick) {
            tick = level.getGameTime();
            return true;
        }
        return false;
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        previousInput = tag.getInt("previous");
        tick = level != null ? level.getGameTime() : 0;
        transitiveState = previousInput;
    }

    public int getPreviousInput() {
        if (shouldTick())
            previousInput = transitiveState;
        return previousInput;
    }

    public void setPreviousInput(int value) { transitiveState = value; }
}
