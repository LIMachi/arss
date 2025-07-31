package com.limachi.arss.common.block_entities;

import com.limachi.arss.Arss;

import com.limachi.arss.common.blocks.diodes.DiodeBlockFactory;

import com.limachi.lim_lib.common.annotations.StaticInit;
import com.limachi.lim_lib.common.modCreation.Stage;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DelayerBlockEntity extends BaseAnalogDiodeBlockEntity {

    private final int[] memory = new int[16];
    private int length = 1;
    private int head = 0;

    public static RegistrySupplier<BlockEntityType<DelayerBlockEntity>> TYPE;

    @StaticInit(Stage.BLOCK_ENTITY)
    public static void registerType() {
        TYPE = Arss.INSTANCE.registries.blockEntity("delayer", DelayerBlockEntity::new, DiodeBlockFactory.getBlockRegister("delayer"));
    }

    public DelayerBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Head", this.head);
        tag.putIntArray("Memory", memory);
        tag.putInt("Length", length);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        head = tag.getInt("Head");
        length = tag.getInt("Length");
        int[] tmp = tag.getIntArray("Memory");
        for (int i = 0; i < 16; ++i)
            memory[i] = i < tmp.length && i < length ? tmp[i] : 0;
    }

    public int state() { return memory[head]; }

    public int step(int input, int delay) {
        if (delay < length) {
            for (int i = delay; i < 16; ++i)
                memory[i] = 0;
            if (head >= delay)
                head = 0;
        }
        length = delay;
        int out = memory[head];
        memory[head] = input;
        if (++head >= delay)
            head = 0;
        setChanged();
        return out;
    }
}
