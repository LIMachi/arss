package com.limachi.arss.blockEntities;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.lim_lib.registries.Registries;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

@StaticInit(Stage.BLOCK_ENTITY)
public class DelayerBlockEntity extends BlockEntity {

    private final int[] memory = new int[16];
    private int head = 0;

    public static final RegistryObject<BlockEntityType<DelayerBlockEntity>> TYPE = Registries.blockEntity(Arss.MOD_ID, "delayer_block_entity", DelayerBlockEntity::new, DiodeBlockFactory.getBlockRegister("delayer"));

    public DelayerBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Head", this.head);
        tag.putIntArray("Memory", memory);
    }

    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        head = tag.getInt("Head");
        int[] tmp = tag.getIntArray("Memory");
        for (int i = 0; i < 16; ++i) {
            memory[i] = i < tmp.length ? tmp[i] : 0;
        }
    }

    public int state() { return memory[head]; }

    public int step(int input, int delay) {
        int out = memory[head];
        memory[Math.min(head, delay)] = input;
        if (++head >= delay)
            head = 0;
        setChanged();
        return out;
    }
}
