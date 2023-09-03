package com.limachi.arss.blockEntities;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.lim_lib.registries.Registries;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
@StaticInit(Stage.BLOCK_ENTITY)
public class GenericDiodeBlockEntity extends BlockEntity {

    private int output;

    public static final RegistryObject<BlockEntityType<GenericDiodeBlockEntity>> TYPE = Util.make(()->{
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
            "edge_detector",
            "shifter",
        };
        Supplier<Block>[] diodes = new Supplier[diodeNames.length];
        for (int i = 0; i < diodes.length; ++i)
            diodes[i] = DiodeBlockFactory.getBlockRegister(diodeNames[i]);
        return Registries.blockEntity(Arss.MOD_ID, "generic_diode", GenericDiodeBlockEntity::new, diodes);
    });

    public GenericDiodeBlockEntity(BlockPos pos, BlockState state) { this(TYPE.get(), pos, state); }

    protected GenericDiodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("output", output);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        output = tag.getInt("output");
    }

    public int getOutput() { return output; }

    public void setOutput(int value) { output = value; }
}
