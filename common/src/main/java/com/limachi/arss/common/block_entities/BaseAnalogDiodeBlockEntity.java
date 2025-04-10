package com.limachi.arss.common.block_entities;

import com.limachi.arss.Arss;
import com.limachi.arss.common.blocks.diodes.DiodeBlockFactory;

import com.limachi.lim_lib.common.annotations.StaticInit;
import com.limachi.lim_lib.common.mod_creation.Stage;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class BaseAnalogDiodeBlockEntity extends BlockEntity {
    private int output;

    public static RegistrySupplier<BlockEntityType<BaseAnalogDiodeBlockEntity>> TYPE;

    @StaticInit(Stage.BLOCK_ENTITY)
    public static void buildType() {
        String[] diodeNames = {
                "adder",
                "analog_and",
                "analog_cell",
                "analog_nand",
                "analog_nor",
                "analog_or",
                "analog_xnor",
                "analog_xor",
                "better_comparator",
                "checker",
                "demuxer",
                "shifter"
        };
        Supplier<Block>[] diodes = new Supplier[diodeNames.length];
        for (int i = 0; i < diodes.length; ++i)
            diodes[i] = DiodeBlockFactory.getBlockRegister(diodeNames[i]);
        TYPE = Arss.registries.blockEntity("generic_diode", BaseAnalogDiodeBlockEntity::new, diodes);
    }

    public BaseAnalogDiodeBlockEntity(BlockPos pos, BlockState state) { this(TYPE.get(), pos, state); }

    protected BaseAnalogDiodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("output", output);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        output = tag.getInt("output");
    }

    public int getOutput() { return output; }

    public void setOutput(int value) { output = value; }

    public List<ItemStack> getDrops(ServerLevel level, BlockPos pos, BlockState state, Player player) {
        return Collections.emptyList();
    }
}