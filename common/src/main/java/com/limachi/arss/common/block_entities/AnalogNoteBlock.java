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

    private int previousInput;

    @RegisterBlockEntity
    public static RegistrySupplier<BlockEntityType<AnalogNoteBlock>> TYPE;

    public AnalogNoteBlock(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("previous", previousInput);
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        previousInput = tag.getInt("previous");
    }

    public int getPreviousInput() { return previousInput; }

    public void setPreviousInput(int value) { previousInput = value; }
}
