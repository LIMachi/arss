package com.limachi.arss.common;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.annotations.StaticInit;
import com.limachi.arss.utils.codec.Codecs;
import com.limachi.arss.utils.codec.StreamCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Objects;

public class ArssItemStackComponents {
    public static RegistrySupplier<DataComponentType<Integer>> OUTPUT;
    public static RegistrySupplier<DataComponentType<NamedPos>> TARGET;
    public static RegistrySupplier<DataComponentType<Boolean>> CATCH;
    public static RegistrySupplier<DataComponentType<Bindings>> BINDINGS;
    public static RegistrySupplier<DataComponentType<SequencerData>> SEQUENCER_DATA;

    public record NamedPos(BlockPos pos, String name) {
        public static final NamedPos UNSET = new NamedPos(BlockPos.of(-1), "");
        public boolean set() { return !Objects.equals(pos, BlockPos.of(-1)) && !name.isBlank(); }
    }

    public record Bindings(int[] raw) {
        private static final int[] rawDefault = Util.make(new int[15], a->{ for (int i = 0; i < 15; ++i) a[i] = -1; });
        public static Bindings empty() { return new Bindings(rawDefault.clone()); }
    }

    public record SequencerData(byte[] ticks, int length, int head, int[] limits, boolean preview) {
        public String toBase64() {
            ByteBuffer tmp = ByteBuffer.allocate(6 + ticks.length + limits.length * 2);
            tmp.putShort((short)length);
            tmp.putShort((short)head);
            tmp.put(preview ? (byte)1 : 0);
            tmp.put((byte)limits.length);
            for (int l : limits)
                tmp.putShort((short)l);
            tmp.put(ticks);
            return Base64.getEncoder().encodeToString(tmp.array());
        }

        public static SequencerData fromBase64(String base64) {
            ByteBuffer bb;
            try {
                bb = ByteBuffer.wrap(Base64.getDecoder().decode(base64));
            } catch (IllegalArgumentException ignore) {
                return null;
            }
            try {
                int al = bb.array().length - 6;
                if (al <= 0) return null;
                int length = bb.getShort();
                int head = bb.getShort();
                boolean preview = bb.get() != 0;
                int ll = bb.get();
                al -= ll * 2;
                if (al <= 0) return null;
                int[] limits = new int[ll];
                for (int i = 0; i < ll; ++i)
                    limits[i] = bb.getShort();
                byte[] ticks = new byte[al];
                bb.get(ticks);
                return new SequencerData(ticks, length, head, limits, preview);
            } catch (BufferUnderflowException ignore) {
                return null;
            }
        }

        public static SequencerData fromCompoundTag(CompoundTag tag) {
            return new SequencerData(tag.getByteArray("Ticks"), tag.getInt("Length"), tag.getInt("Head"), tag.getIntArray("Limits"), tag.getBoolean("Preview"));
        }

        public CompoundTag toCompoundTag(CompoundTag tag) {
            tag.putInt("Head", head);
            tag.putBoolean("Preview", preview);
            tag.putByteArray("Ticks", ticks);
            tag.putInt("Length", length);
            tag.putIntArray("Limits", limits);
            return tag;
        }
    }

    @StaticInit
    public static void registerComponent() {
        OUTPUT = ModBase.registries.component("output", Codecs.INT, StreamCodecs.INT);
        CATCH = ModBase.registries.component("catch", Codecs.BOOL, StreamCodecs.BOOL);
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
        SEQUENCER_DATA = ModBase.registries.component("sequencer_data",
                RecordCodecBuilder.create(b->
                        b.group(
                                Codecs.PRIM_BYTE_ARRAY.fieldOf("ticks").forGetter(SequencerData::ticks),
                                Codecs.INT.fieldOf("length").forGetter(SequencerData::length),
                                Codecs.INT.fieldOf("head").forGetter(SequencerData::head),
                                Codecs.PRIM_INT_ARRAY.fieldOf("limits").forGetter(SequencerData::limits),
                                Codecs.BOOL.fieldOf("preview").forGetter(SequencerData::preview)
                        ).apply(b, SequencerData::new)),
                StreamCodec.of((b, v)->{
                    b.writeByteArray(v.ticks);
                    b.writeInt(v.length);
                    b.writeInt(v.head);
                    b.writeVarIntArray(v.limits);
                    b.writeBoolean(v.preview);
                }, b->new SequencerData(b.readByteArray(), b.readInt(), b.readInt(), b.readVarIntArray(), b.readBoolean())));
    }
}
