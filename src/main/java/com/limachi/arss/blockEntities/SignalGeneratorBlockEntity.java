package com.limachi.arss.blockEntities;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.lim_lib.registries.Registries;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@StaticInit(Stage.BLOCK_ENTITY)
public class SignalGeneratorBlockEntity extends BlockEntity {

    private int step = 0;
    private static final int[] sineGraph = new int[360];
    private static final Random RANDOM = new Random();

    static {
        for (int i = 0; i < 360; ++i) sineGraph[i] = (int)Math.round((Math.sin(((double)i / 180. * Math.PI)) + 1.) / 2. * 15.);
    }

    public static final RegistryObject<BlockEntityType<SignalGeneratorBlockEntity>> TYPE = Registries.blockEntity(Arss.MOD_ID, "signal_generator_block_entity", SignalGeneratorBlockEntity::new, DiodeBlockFactory.getBlockRegister("signal_generator"));

    public SignalGeneratorBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Step", step);
    }

    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        step = tag.getInt("Step");
    }

    private int prev_random_step;
    private int prev_random;

    protected int random_get(Pair<Integer, Integer> sides) {
        if (prev_random_step != step) {
            int min = Integer.min(sides.getFirst(), sides.getSecond());
            int max = Integer.max(sides.getFirst(), sides.getSecond());
            if (max == 0) max = 15;
            prev_random = RANDOM.nextInt(max - min + 1) + min;
            prev_random_step = step;
        }
        return prev_random;
    }
    protected void random_step(Pair<Integer, Integer> sides) {
        step = (step + 1) % 2;
        setChanged();
    }

    protected int sine_get(Pair<Integer, Integer> sides) { return sineGraph[step]; }
    protected void sine_step(Pair<Integer, Integer> sides) {
        int max = Integer.max(sides.getFirst(), sides.getSecond());
        step = (step + 1 + max) % 360;
        setChanged();
    }

    protected int square_get(Pair<Integer, Integer> sides) {
        int max = Integer.max(sides.getFirst(), sides.getSecond());
        return step >= 1 + max ? 15 : 0;
    }
    protected void square_step(Pair<Integer, Integer> sides) {
        int max = Integer.max(sides.getFirst(), sides.getSecond());
        step = (step + 1) % (2 + max * 2);
        setChanged();
    }

    protected int inverse_saw_get(Pair<Integer, Integer> sides) {
        int min = Integer.min(sides.getFirst(), sides.getSecond());
        int max = Integer.max(sides.getFirst(), sides.getSecond());
        if (max == 0) max = 15;
        return max - (step % (1 + max - min));
    }
    protected int saw_get(Pair<Integer, Integer> sides) {
        int min = Integer.min(sides.getFirst(), sides.getSecond());
        int max = Integer.max(sides.getFirst(), sides.getSecond());
        if (max == 0) max = 15;
        return step % (1 + max - min) + min;
    }
    protected void saw_step(Pair<Integer, Integer> sides) {
        int min = Integer.min(sides.getFirst(), sides.getSecond());
        int max = Integer.max(sides.getFirst(), sides.getSecond());
        if (max == 0) max = 15;
        step = (step + 1) % (1 + max - min);
        setChanged();
    }

    protected int triangle_get(Pair<Integer, Integer> sides) {
        int min = Integer.min(sides.getFirst(), sides.getSecond());
        int max = Integer.max(sides.getFirst(), sides.getSecond());
        if (max == 0) max = 15;
        if (max != min) {
            int s = step % (2 * (max - min));
            return step <= (max - min) ? s + min : 2 * (max - min) - s + min;
        }
        return min;
    }
    protected void triangle_step(Pair<Integer, Integer> sides) {
        int min = Integer.min(sides.getFirst(), sides.getSecond());
        int max = Integer.max(sides.getFirst(), sides.getSecond());
        if (max == 0) max = 15;
        if (max != min)
            step = (step + 1) % (2 * (max - min));
        else
            step = 0;
        setChanged();
    }

    static protected Map<String, Pair<BiFunction<SignalGeneratorBlockEntity, Pair<Integer, Integer>, Integer>, BiConsumer<SignalGeneratorBlockEntity, Pair<Integer, Integer>>>> functions = Map.of(
        "inverse_saw", new Pair<BiFunction<SignalGeneratorBlockEntity, Pair<Integer, Integer>, Integer>, BiConsumer<SignalGeneratorBlockEntity, Pair<Integer, Integer>>>(SignalGeneratorBlockEntity::inverse_saw_get, SignalGeneratorBlockEntity::saw_step),
        "random", new Pair<BiFunction<SignalGeneratorBlockEntity, Pair<Integer, Integer>, Integer>, BiConsumer<SignalGeneratorBlockEntity, Pair<Integer, Integer>>>(SignalGeneratorBlockEntity::random_get, SignalGeneratorBlockEntity::random_step),
        "saw", new Pair<BiFunction<SignalGeneratorBlockEntity, Pair<Integer, Integer>, Integer>, BiConsumer<SignalGeneratorBlockEntity, Pair<Integer, Integer>>>(SignalGeneratorBlockEntity::saw_get, SignalGeneratorBlockEntity::saw_step),
        "sine", new Pair<BiFunction<SignalGeneratorBlockEntity, Pair<Integer, Integer>, Integer>, BiConsumer<SignalGeneratorBlockEntity, Pair<Integer, Integer>>>(SignalGeneratorBlockEntity::sine_get, SignalGeneratorBlockEntity::sine_step),
        "square", new Pair<BiFunction<SignalGeneratorBlockEntity, Pair<Integer, Integer>, Integer>, BiConsumer<SignalGeneratorBlockEntity, Pair<Integer, Integer>>>(SignalGeneratorBlockEntity::square_get, SignalGeneratorBlockEntity::square_step),
        "triangle", new Pair<BiFunction<SignalGeneratorBlockEntity, Pair<Integer, Integer>, Integer>, BiConsumer<SignalGeneratorBlockEntity, Pair<Integer, Integer>>>(SignalGeneratorBlockEntity::triangle_get, SignalGeneratorBlockEntity::triangle_step)
    );

    public int state(String mode, Pair<Integer, Integer> side_power) {
        Pair<BiFunction<SignalGeneratorBlockEntity, Pair<Integer, Integer>, Integer>, BiConsumer<SignalGeneratorBlockEntity, Pair<Integer, Integer>>> pf = functions.get(mode);
        return pf.getFirst().apply(this, side_power);
    }

    public int step(String mode, Pair<Integer, Integer> side_power) {
        Pair<BiFunction<SignalGeneratorBlockEntity, Pair<Integer, Integer>, Integer>, BiConsumer<SignalGeneratorBlockEntity, Pair<Integer, Integer>>> pf = functions.get(mode);
        int out = pf.getFirst().apply(this, side_power);
        pf.getSecond().accept(this, side_power);
        return out;
    }
}
