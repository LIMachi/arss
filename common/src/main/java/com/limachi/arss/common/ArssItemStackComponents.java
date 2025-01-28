package com.limachi.arss.common;

import com.limachi.arss.utils.CodecUtils;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.annotations.StaticInit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class ArssItemStackComponents {
    public static RegistrySupplier<DataComponentType<Integer>> OUTPUT;
    public static RegistrySupplier<DataComponentType<NamedPos>> TARGET;
    public static RegistrySupplier<DataComponentType<Boolean>> CATCH;
    public static RegistrySupplier<DataComponentType<Integer[]>> BINDINGS;

    public record NamedPos(BlockPos pos, String name) {
        public static final NamedPos UNSET = new NamedPos(BlockPos.of(-1), "");
        public boolean set() {
            return !Objects.equals(pos, BlockPos.of(-1)) && !name.isBlank();
        }
    }

    @StaticInit
    public static void registerComponent() {
        OUTPUT = ModBase.registries.component("output", Codec.INT, CodecUtils.INT_STREAM_CODEC);
        CATCH = ModBase.registries.component("catch", Codec.BOOL, CodecUtils.BOOL_STREAM_CODEC);
//        TARGET = ModBase.registries.component("target", CodecUtils.autoCodec(NamedPos.class), CodecUtils.autoStramCodec(NamedPos.class));
        TARGET = ModBase.registries.component("target",
                RecordCodecBuilder.create(b->
                        b.group(CodecUtils.POS_CODEC.fieldOf("pos").forGetter(NamedPos::pos), Codec.STRING.fieldOf("name").forGetter(NamedPos::name))
                                .apply(b, NamedPos::new)),
                StreamCodec.of((b, v)->{
                    b.writeBlockPos(v.pos);
                    b.writeUtf(v.name);
                }, b->new NamedPos(b.readBlockPos(), b.readUtf())));
        BINDINGS = ModBase.registries.component("bindings", CodecUtils.INT_ARRAY_CODEC, CodecUtils.INT_ARRAY_STREAM_CODEC);
    }
}
