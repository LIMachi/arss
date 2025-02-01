package com.limachi.arss.common.block_entities;

import com.limachi.arss.Arss;

import com.limachi.arss.common.blocks.diodes.DiodeBlockFactory;
import com.limachi.arss.utils.Stage;
import com.limachi.arss.utils.annotations.StaticInit;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EdgeDetectorBlockEntity extends BaseAnalogDiodeBlockEntity {

    private int previousInput;

    public static RegistrySupplier<BlockEntityType<EdgeDetectorBlockEntity>> TYPE;

    @StaticInit(Stage.BLOCK_ENTITY)
    public static void registerType() {
        TYPE = Arss.registries.blockEntity("edge_detector", EdgeDetectorBlockEntity::new, DiodeBlockFactory.getBlockRegister("edge_detector"));
    }

    public EdgeDetectorBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("previous", previousInput);
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        previousInput = tag.getInt("previous");
        setChanged();
    }

    public int getPreviousInput() { return previousInput; }

    public void setPreviousInput(int value) { previousInput = value; }
}
