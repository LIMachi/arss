package com.limachi.arss.common;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.annotations.StaticInit;
import com.limachi.arss.utils.codec.CodecUtils;
import com.limachi.arss.utils.codec.Codecs;
import com.limachi.arss.utils.codec.StreamCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class ArssItemStackComponents {
    public static RegistrySupplier<DataComponentType<Integer>> OUTPUT;
    public static RegistrySupplier<DataComponentType<NamedPos>> TARGET;
    public static RegistrySupplier<DataComponentType<Boolean>> CATCH;
    public static RegistrySupplier<DataComponentType<Bindings>> BINDINGS;

    public record NamedPos(BlockPos pos, String name) {
        public static final NamedPos UNSET = new NamedPos(BlockPos.of(-1), "");
        public boolean set() { return !Objects.equals(pos, BlockPos.of(-1)) && !name.isBlank(); }
    }

    public record Bindings(int[] raw) {
        private static final int[] rawDefault = Util.make(new int[15], a->{ for (int i = 0; i < 15; ++i) a[i] = -1; });
        public static Bindings empty() { return new Bindings(rawDefault.clone()); }
    }

    @StaticInit
    public static void registerComponent() {
        OUTPUT = ModBase.registries.component("output", Codecs.INT, StreamCodecs.INT);
        CATCH = ModBase.registries.component("catch", Codecs.BOOL, StreamCodecs.BOOL);
//        TARGET = ModBase.registries.component("target", CodecUtils.autoCodec(NamedPos.class), CodecUtils.autoStramCodec(NamedPos.class));
        TARGET = ModBase.registries.component("target",
                RecordCodecBuilder.create(b->
                        b.group(Codecs.POS.fieldOf("pos").forGetter(NamedPos::pos), Codec.STRING.fieldOf("name").forGetter(NamedPos::name))
                                .apply(b, NamedPos::new)),
                StreamCodec.of((b, v)->{
                    b.writeBlockPos(v.pos);
                    b.writeUtf(v.name);
                }, b->new NamedPos(b.readBlockPos(), b.readUtf())));
        BINDINGS = ModBase.registries.component("bindings",
                RecordCodecBuilder.create(b->b.group(Codecs.PRIM_INT_ARRAY.fieldOf("raw").forGetter(Bindings::raw)).apply(b, Bindings::new)),
                StreamCodec.of((b, v)->b.writeVarIntArray(v.raw), b->new Bindings(b.readVarIntArray())));
    }
}
