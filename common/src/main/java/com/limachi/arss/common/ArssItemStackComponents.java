package com.limachi.arss.common;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.annotations.StaticInit;
import com.limachi.arss.utils.codec.Codecs;
import com.limachi.arss.utils.codec.StreamCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Base64;

public class ArssItemStackComponents {
    public static RegistrySupplier<DataComponentType<Integer>> OUTPUT;
    public static RegistrySupplier<DataComponentType<Boolean>> CATCH;
    public static RegistrySupplier<DataComponentType<Bindings>> BINDINGS;
    public static RegistrySupplier<DataComponentType<SequencerData>> SEQUENCER_DATA;

    public record Bindings(String[] freq, byte[] power, int[] key) {
        public static Bindings empty() {
            return new Bindings(Util.make(new String[15], a->{
                for (int i = 0; i < 15; ++i)
                    a[i] = "";
            }), new byte[15], new int[15]);
        }
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
        BINDINGS = ModBase.registries.component("bindings",
                RecordCodecBuilder.create(b->b.group(
                        Codecs.STR_ARRAY.fieldOf("freq").forGetter(Bindings::freq),
                        Codecs.PRIM_BYTE_ARRAY.fieldOf("power").forGetter(Bindings::power),
                        Codecs.PRIM_INT_ARRAY.fieldOf("key").forGetter(Bindings::key)
                ).apply(b, Bindings::new)),
                StreamCodec.of((b, v)->{
                    for (int i = 0; i < 15; ++i)
                        b.writeUtf(v.freq[i]);
                    b.writeByteArray(v.power);
                    b.writeVarIntArray(v.key);
                }, b->{
                    String[] freq = new String[15];
                    for (int i = 0; i < 15; ++i)
                        freq[i] = b.readUtf();
                    return new Bindings(freq, b.readByteArray(), b.readVarIntArray());
                }));
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
