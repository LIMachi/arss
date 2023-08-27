package com.limachi.arss.blockEntities;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.lim_lib.registries.Registries;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

@StaticInit(Stage.BLOCK_ENTITY)
public class EdgeDetectorBlockEntity extends GenericDiodeBlockEntity {

    private int previousInput;

    public static final RegistryObject<BlockEntityType<EdgeDetectorBlockEntity>> TYPE = Registries.blockEntity(Arss.MOD_ID, "edge_detector", EdgeDetectorBlockEntity::new, DiodeBlockFactory.getBlockRegister("edge_detector"));

    public EdgeDetectorBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

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
