package com.limachi.arss.blocks.diodes;

import com.limachi.arss.Static;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import static com.limachi.arss.Registries.BLOCK_ENTITY_REGISTER;

@Static
public class DelayerBlockEntity extends BlockEntity {

    private final int[] memory = new int[16];
    private int head = 0;

    public static final RegistryObject<BlockEntityType<?>> TYPE = BLOCK_ENTITY_REGISTER.register("delayer", ()->BlockEntityType.Builder.of(DelayerBlockEntity::new, DiodeBlockFactory.getBlock("delayer")).build(null));

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
