package com.limachi.arss.blockEntities;

import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

@StaticInit(Stage.BLOCK_ENTITY)
public class AnalogNoteBlockBlockEntity extends BlockEntity {

    private int previousInput;

    @RegisterBlockEntity(blocks = "analog_note_block")
    public static RegistryObject<BlockEntityType<AnalogNoteBlockBlockEntity>> TYPE;

    public AnalogNoteBlockBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("previous", previousInput);
    }

    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        previousInput = tag.getInt("previous");
    }

    public int getPreviousInput() { return previousInput; }

    public void setPreviousInput(int value) { previousInput = value; }
}
