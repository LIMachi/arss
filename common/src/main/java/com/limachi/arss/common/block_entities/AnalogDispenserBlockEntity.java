package com.limachi.arss.common.block_entities;

import com.limachi.lim_lib.common.annotations.RegisterBlockEntity;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AnalogDispenserBlockEntity extends DispenserBlockEntity {
    @RegisterBlockEntity(blocks = "arss:analog_dispenser")
    public static RegistrySupplier<BlockEntityType<BlockEntity>> TYPE;

    protected AnalogDispenserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public AnalogDispenserBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TYPE.get(), blockPos, blockState);
    }
}
