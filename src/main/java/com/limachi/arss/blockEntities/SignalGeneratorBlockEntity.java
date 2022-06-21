package com.limachi.arss.blockEntities;

import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.arss.utils.StaticInitializer;
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
import java.util.function.BiFunction;

import static com.limachi.arss.Registries.BLOCK_ENTITY_REGISTER;

@StaticInitializer.Static
public class SignalGeneratorBlockEntity extends BlockEntity {

    private int step = 0;
    private static final int[] sineGraph = new int[360];
    private static final Random RANDOM = new Random();

    static {
        for (int i = 0; i < 360; ++i) sineGraph[i] = (int)Math.round((Math.sin(((double)i / 180. * Math.PI)) + 1.) / 2. * 15.);
    }

    public static final RegistryObject<BlockEntityType<?>> TYPE = BLOCK_ENTITY_REGISTER.register("signal_generator", ()->BlockEntityType.Builder.of(SignalGeneratorBlockEntity::new, DiodeBlockFactory.getBlock("signal_generator")).build(null));

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

    protected int random(int step) {
        if (prev_random_step != step) {
            prev_random = RANDOM.nextInt(16);
            prev_random_step = step;
        }
        return prev_random;
    }

    protected int inverse_saw(int step) { return 15 - (step % 16); }
    protected int saw(int step) { return step % 16; }
    protected int sine(int step) { return sineGraph[step]; }
    protected int square(int step) { return step == 1 ? 15 : 0; }
    protected int triangle(int step) { return step < 16 ? step : 30 - step; }

    static protected Map<String, Pair<BiFunction<SignalGeneratorBlockEntity, Integer, Integer>, Integer>> functions = Map.of(
            "inverse_saw", new Pair<BiFunction<SignalGeneratorBlockEntity, Integer, Integer>, Integer>(SignalGeneratorBlockEntity::inverse_saw, 16),
            "random", new Pair<BiFunction<SignalGeneratorBlockEntity, Integer, Integer>, Integer>(SignalGeneratorBlockEntity::random, 2),
            "saw", new Pair<BiFunction<SignalGeneratorBlockEntity, Integer, Integer>, Integer>(SignalGeneratorBlockEntity::saw, 16),
            "sine", new Pair<BiFunction<SignalGeneratorBlockEntity, Integer, Integer>, Integer>(SignalGeneratorBlockEntity::sine, 360),
            "square", new Pair<BiFunction<SignalGeneratorBlockEntity, Integer, Integer>, Integer>(SignalGeneratorBlockEntity::square, 2),
            "triangle", new Pair<BiFunction<SignalGeneratorBlockEntity, Integer, Integer>, Integer>(SignalGeneratorBlockEntity::triangle, 29)
            );

    public int state(String mode) {
        Pair<BiFunction<SignalGeneratorBlockEntity, Integer, Integer>, Integer> pf = functions.get(mode);
        return pf.getFirst().apply(this, step % pf.getSecond());
    }

    public int step(String mode) {
        Pair<BiFunction<SignalGeneratorBlockEntity, Integer, Integer>, Integer> pf = functions.get(mode);
        int out = pf.getFirst().apply(this, step % pf.getSecond());
        step = (step + 1) % pf.getSecond();
        setChanged();
        return out;
    }
}
