package com.limachi.arss.utils;

import com.limachi.arss.utils.reflect.Utils;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class CodecUtils {
    public static PrimitiveCodec<Character> CHAR_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Character> read(DynamicOps<T> ops, T input) { return ops.getNumberValue(input).map(n->(char)n.intValue()); }
        @Override
        public <T> T write(DynamicOps<T> ops, Character value) { return ops.createInt(value); }
    };

    public static PrimitiveCodec<BlockPos> POS_CODEC = new PrimitiveCodec<BlockPos>() {
        @Override
        public <T> DataResult<BlockPos> read(DynamicOps<T> ops, T input) { return ops.getNumberValue(input).map(n->BlockPos.of(n.longValue())); }
        @Override
        public <T> T write(DynamicOps<T> ops, BlockPos value) { return ops.createLong(value.asLong()); }
    };

    public static PrimitiveCodec<Boolean[]> BOOL_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Boolean[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getBooleanValue(e).isSuccess())) {
                    var out = new Boolean[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getBooleanValue(list.get(i)).result().get();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Boolean[] value) {
            return ops.createList(Arrays.stream(value).map(ops::createBoolean));
        }
    };

    public static PrimitiveCodec<Byte[]> BYTE_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Byte[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    var out = new Byte[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().byteValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Byte[] value) {
            return ops.createList(Arrays.stream(value).map(ops::createByte));
        }
    };

    public static PrimitiveCodec<Character[]> CHAR_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Character[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    var out = new Character[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = (char)ops.getNumberValue(list.get(i)).result().get().intValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Character[] value) {
            return ops.createList(Arrays.stream(value).map(ops::createInt));
        }
    };

    public static PrimitiveCodec<Short[]> SHORT_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Short[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    var out = new Short[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().shortValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Short[] value) {
            return ops.createList(Arrays.stream(value).map(ops::createShort));
        }
    };

    public static PrimitiveCodec<Integer[]> INT_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Integer[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    var out = new Integer[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().intValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Integer[] value) {
            return ops.createList(Arrays.stream(value).map(ops::createInt));
        }
    };

    public static PrimitiveCodec<Long[]> LONG_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Long[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    var out = new Long[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().longValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Long[] value) {
            return ops.createList(Arrays.stream(value).map(ops::createLong));
        }
    };

    public static PrimitiveCodec<Float[]> FLOAT_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Float[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    var out = new Float[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().floatValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Float[] value) {
            return ops.createList(Arrays.stream(value).map(ops::createFloat));
        }
    };

    public static PrimitiveCodec<Double[]> DOUBLE_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Double[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    var out = new Double[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().doubleValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Double[] value) {
            return ops.createList(Arrays.stream(value).map(ops::createDouble));
        }
    };

    public static PrimitiveCodec<String[]> STR_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<String[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getStringValue(e).isSuccess())) {
                    var out = new String[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getStringValue(list.get(i)).result().get();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, String[] value) {
            return ops.createList(Arrays.stream(value).map(ops::createString));
        }
    };

    public static PrimitiveCodec<BlockPos[]> POS_ARRAY_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<BlockPos[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    var out = new BlockPos[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = BlockPos.of(ops.getNumberValue(list.get(i)).result().get().longValue());
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, BlockPos[] value) {
            return ops.createList(Arrays.stream(value).map(r->ops.createLong(r.asLong())));
        }
    };

    public static StreamCodec<RegistryFriendlyByteBuf, Boolean> BOOL_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeBoolean, RegistryFriendlyByteBuf::readBoolean);
    public static StreamCodec<RegistryFriendlyByteBuf, Byte> BYTE_STREAM_CODEC = StreamCodec.of((b, i)->b.writeByte(i), FriendlyByteBuf::readByte);
    public static StreamCodec<RegistryFriendlyByteBuf, Character> CHAR_STREAM_CODEC = StreamCodec.of((b, c)->b.writeChar(c), FriendlyByteBuf::readChar);
    public static StreamCodec<RegistryFriendlyByteBuf, Short> SHORT_STREAM_CODEC = StreamCodec.of((b, s)->b.writeShort(s), RegistryFriendlyByteBuf::readShort);
    public static StreamCodec<RegistryFriendlyByteBuf, Integer> INT_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeInt, RegistryFriendlyByteBuf::readInt);
    public static StreamCodec<RegistryFriendlyByteBuf, Long> LONG_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeLong, RegistryFriendlyByteBuf::readLong);
    public static StreamCodec<RegistryFriendlyByteBuf, Float> FLOAT_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeFloat, RegistryFriendlyByteBuf::readFloat);
    public static StreamCodec<RegistryFriendlyByteBuf, Double> DOUBLE_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeDouble, RegistryFriendlyByteBuf::readDouble);
    public static StreamCodec<RegistryFriendlyByteBuf, String> STR_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf);
    public static StreamCodec<RegistryFriendlyByteBuf, BlockPos> POS_STREAM_CODEC = StreamCodec.of((b, c)->b.writeLong(c.asLong()), b->BlockPos.of(b.readLong()));

    public static StreamCodec<RegistryFriendlyByteBuf, Boolean[]> BOOL_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->b.writeByteArray(compactBoolArray(l)), b->fromCompactBoolArray(b.readByteArray()));
    public static StreamCodec<RegistryFriendlyByteBuf, Byte[]> BYTE_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeByte(p);
    }, b->{
        var out = new Byte[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readByte();
        return out;
    });
    public static StreamCodec<RegistryFriendlyByteBuf, Character[]> CHAR_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeChar(p);
    }, b->{
        var out = new Character[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readChar();
        return out;
    });

    public static StreamCodec<RegistryFriendlyByteBuf, Short[]> SHORT_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeShort(p);
    }, b->{
        var out = new Short[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readShort();
        return out;
    });

    public static StreamCodec<RegistryFriendlyByteBuf, Integer[]> INT_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeInt(p);
    }, b->{
        var out = new Integer[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readInt();
        return out;
    });

    public static StreamCodec<RegistryFriendlyByteBuf, Long[]> LONG_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeLong(p);
    }, b->{
        var out = new Long[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readLong();
        return out;
    });

    public static StreamCodec<RegistryFriendlyByteBuf, Float[]> FLOAT_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeFloat(p);
    }, b->{
        var out = new Float[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readFloat();
        return out;
    });

    public static StreamCodec<RegistryFriendlyByteBuf, Double[]> DOUBLE_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeDouble(p);
    }, b->{
        var out = new Double[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readDouble();
        return out;
    });

    public static StreamCodec<RegistryFriendlyByteBuf, String[]> STR_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeUtf(p);
    }, b->{
        String[] out = new String[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readUtf();
        return out;
    });

    public static StreamCodec<RegistryFriendlyByteBuf, BlockPos[]> POS_ARRAY_STREAM_CODEC = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeLong(p.asLong());
    }, b->{
        BlockPos[] out = new BlockPos[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = BlockPos.of(b.readLong());
        return out;
    });

    public static byte[] compactBoolArray(Boolean[] ar) {
        if (ar.length == 0)
            return new byte[0];
        int pl = ar.length + 3;
        var out = new byte[(pl >> 3) + ((pl & 7) != 0 ? 1 : 0)];
        out[0] = (byte)(8 - (pl & 7));
        for (int i = 0; i < ar.length; ++i)
            out[(i + 3) >> 3] |= (byte) ((ar[i] ? 1 : 0) << ((i + 3) & 7));
        return out;
    }

    public static Boolean[] fromCompactBoolArray(byte[] ar) {
        if (ar.length == 0)
            return new Boolean[0];
        int pad = ar[0] & 7;
        var out = new Boolean[(ar.length << 3) - 3 - pad];
        for (int i = 0; i < out.length; ++i)
            out[i] = (ar[(i + 3) >> 3] & (1 << ((i + 3) & 7))) != 0;
        return out;
    }

    public static <T extends Record> Codec<T> recordCodec() { return recordCodec(Utils.classOfGeneric()); }
    public static <T extends Record> Codec<T> recordCodec(Class<T> rec) {
        var comps = rec.getRecordComponents();
        if (comps == null)
            throw new RuntimeException("getRecordComponents returned null for " + rec);
        if (comps.length == 0 || comps.length > 16)
            throw new RuntimeException("invalid record length " + comps.length + " for " + rec);
        return RecordCodecBuilder.create(b->{
            var types = new Class[comps.length];
            var codecs = new RecordCodecBuilder[comps.length];
            for (int i = 0; i < comps.length; ++i) {
                types[i] = comps[i].getType();
                Codec<?> tc;
                if ((tc = autoCodec(types[i])) == null)
                    throw new RuntimeException("no codec found for type: " + types[i]);
                int finalI = i;
                Function<T, Object> get = o-> {
                    try {
                        return comps[finalI].getAccessor().invoke(o);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
                ((MapCodec<Object>)tc.fieldOf(comps[i].getName())).forGetter(get);
            }
            Constructor<T> n;
            try {
                n = rec.getConstructor(types);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            return switch (comps.length) {
                case 1 -> b.group(codecs[0]).apply(b, p->Utils.nullableInstance(n, p));
                case 2 -> b.group(codecs[0], codecs[1]).apply(b, (p0, p1)->Utils.nullableInstance(n, p0, p1));
                case 3 -> b.group(codecs[0], codecs[1], codecs[2]).apply(b, (p0, p1, p2)->Utils.nullableInstance(n, p0, p1, p2));
                case 4 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3]).apply(b, (p0, p1, p2, p3)->Utils.nullableInstance(n, p0, p1, p2, p3));
                case 5 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4]).apply(b, (p0, p1, p2, p3, p4)->Utils.nullableInstance(n, p0, p1, p2, p3, p4));
                case 6 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5]).apply(b, (p0, p1, p2, p3, p4, p5)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5));
                case 7 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6]).apply(b, (p0, p1, p2, p3, p4, p5, p6)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6));
                case 8 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7));
                case 9 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8));
                case 10 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9));
                case 11 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));
                case 12 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11));
                case 13 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11], codecs[12]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12));
                case 14 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11], codecs[12], codecs[13]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13));
                case 15 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11], codecs[12], codecs[13], codecs[14]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14));
                case 16 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11], codecs[12], codecs[13], codecs[14], codecs[15]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15)->Utils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15));
                default -> throw new IllegalStateException("Unexpected value: " + comps.length);
            };
        });
    }

    public static <T> Codec<T> autoCodec() { return autoCodec(Utils.classOfGeneric()); }
    public static <T> Codec<T> autoCodec(Class<T> clazz) {
        if (clazz.isArray()) {
            Class<?> c = clazz.getComponentType();
            if (Boolean.class.isAssignableFrom(c))
                return (Codec<T>)BOOL_ARRAY_CODEC;
            if (Byte.class.isAssignableFrom(c))
                return (Codec<T>)BYTE_ARRAY_CODEC;
            if (Character.class.isAssignableFrom(c))
                return (Codec<T>)CHAR_ARRAY_CODEC;
            if (Short.class.isAssignableFrom(c))
                return (Codec<T>)SHORT_ARRAY_CODEC;
            if (Integer.class.isAssignableFrom(c))
                return (Codec<T>)INT_ARRAY_CODEC;
            if (Long.class.isAssignableFrom(c))
                return (Codec<T>)LONG_ARRAY_CODEC;
            if (Float.class.isAssignableFrom(c))
                return (Codec<T>)FLOAT_ARRAY_CODEC;
            if (Double.class.isAssignableFrom(c))
                return (Codec<T>)DOUBLE_ARRAY_CODEC;
            if (String.class.isAssignableFrom(c))
                return (Codec<T>)STR_ARRAY_CODEC;
            if (BlockPos.class.isAssignableFrom(c))
                return (Codec<T>)POS_ARRAY_CODEC;
        }
        if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz))
            return (Codec<T>)Codec.BOOL;
        if (Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz))
            return (Codec<T>)Codec.BYTE;
        if (Character.class.isAssignableFrom(clazz) || char.class.isAssignableFrom(clazz))
            return (Codec<T>)CHAR_CODEC;
        if (Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz))
            return (Codec<T>)Codec.SHORT;
        if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz))
            return (Codec<T>)Codec.INT;
        if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz))
            return (Codec<T>)Codec.LONG;
        if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz))
            return (Codec<T>)Codec.FLOAT;
        if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz))
            return (Codec<T>)Codec.DOUBLE;
        if (String.class.isAssignableFrom(clazz))
            return (Codec<T>)Codec.STRING;
        if (BlockPos.class.isAssignableFrom(clazz))
            return (Codec<T>)POS_CODEC;
        if (Record.class.isAssignableFrom(clazz))
            return (Codec<T>)recordCodec((Class<Record>) clazz);
        return null;
    }

    public static <B extends RegistryFriendlyByteBuf, T> T read(B buf) { return read(buf, Utils.classOfGeneric()); }
    public static <B extends RegistryFriendlyByteBuf, T> T read(B buf, Class<T> clazz) {
        if (clazz.isArray()) {
            Class<?> c = clazz.getComponentType();
            if (Boolean.class.isAssignableFrom(c))
                return (T)BOOL_ARRAY_STREAM_CODEC.decode(buf);
            if (Byte.class.isAssignableFrom(c))
                return (T)BYTE_ARRAY_STREAM_CODEC.decode(buf);
            if (Character.class.isAssignableFrom(c))
                return (T)CHAR_ARRAY_STREAM_CODEC.decode(buf);
            if (Short.class.isAssignableFrom(c))
                return (T)SHORT_ARRAY_STREAM_CODEC.decode(buf);
            if (Integer.class.isAssignableFrom(c))
                return (T)INT_ARRAY_STREAM_CODEC.decode(buf);
            if (Long.class.isAssignableFrom(c))
                return (T)LONG_ARRAY_STREAM_CODEC.decode(buf);
            if (Float.class.isAssignableFrom(c))
                return (T)FLOAT_ARRAY_STREAM_CODEC.decode(buf);
            if (Double.class.isAssignableFrom(c))
                return (T)DOUBLE_ARRAY_STREAM_CODEC.decode(buf);
            if (String.class.isAssignableFrom(c))
                return (T)STR_ARRAY_STREAM_CODEC.decode(buf);
            if (BlockPos.class.isAssignableFrom(c))
                return (T)POS_ARRAY_STREAM_CODEC.decode(buf);
        }
        if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz))
            return (T)(Boolean)buf.readBoolean();
        if (Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz))
            return (T)(Byte)buf.readByte();
        if (Character.class.isAssignableFrom(clazz) || char.class.isAssignableFrom(clazz))
            return (T)(Character)buf.readChar();
        if (Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz))
            return (T)(Short)buf.readShort();
        if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz))
            return (T)(Integer)buf.readInt();
        if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz))
            return (T)(Long)buf.readLong();
        if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz))
            return (T)(Float)buf.readFloat();
        if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz))
            return (T)(Double)buf.readDouble();
        if (String.class.isAssignableFrom(clazz))
            return (T)buf.readUtf();
        if (BlockPos.class.isAssignableFrom(clazz))
            return (T)BlockPos.of(buf.readLong());
        return null;
    }

    public static <B extends RegistryFriendlyByteBuf, T> B write(B buf, T obj) {
        if (obj.getClass().isArray()) {
            Class<?> c = obj.getClass().getComponentType();
            if (Boolean.class.isAssignableFrom(c))
                BOOL_ARRAY_STREAM_CODEC.encode(buf, (Boolean[])obj);
            if (Byte.class.isAssignableFrom(c))
                BYTE_ARRAY_STREAM_CODEC.encode(buf, (Byte[])obj);
            if (Character.class.isAssignableFrom(c))
                CHAR_ARRAY_STREAM_CODEC.encode(buf, (Character[])obj);
            if (Short.class.isAssignableFrom(c))
                SHORT_ARRAY_STREAM_CODEC.encode(buf, (Short[])obj);
            if (Integer.class.isAssignableFrom(c))
                INT_ARRAY_STREAM_CODEC.encode(buf, (Integer[])obj);
            if (Long.class.isAssignableFrom(c))
                LONG_ARRAY_STREAM_CODEC.encode(buf, (Long[])obj);
            if (Float.class.isAssignableFrom(c))
                FLOAT_ARRAY_STREAM_CODEC.encode(buf, (Float[])obj);
            if (Double.class.isAssignableFrom(c))
                DOUBLE_ARRAY_STREAM_CODEC.encode(buf, (Double[])obj);
            if (String.class.isAssignableFrom(c))
                STR_ARRAY_STREAM_CODEC.encode(buf, (String[])obj);
            if (BlockPos.class.isAssignableFrom(c))
                POS_ARRAY_STREAM_CODEC.encode(buf, (BlockPos[])obj);
            return buf;
        }
        if (obj instanceof Boolean o)
            return (B)buf.writeBoolean(o);
        if (obj instanceof Byte o)
            return (B)buf.writeByte(o);
        if (obj instanceof Character o)
            return (B)buf.writeChar(o);
        if (obj instanceof Short o)
            return (B)buf.writeShort(o);
        if (obj instanceof Integer o)
            return (B)buf.writeInt(o);
        if (obj instanceof Long o)
            return (B)buf.writeLong(o);
        if (obj instanceof Float o)
            return (B)buf.writeFloat(o);
        if (obj instanceof Double o)
            return (B)buf.writeDouble(o);
        if (obj instanceof String o)
            return (B)buf.writeUtf(o);
        if (obj instanceof BlockPos o)
            return (B)buf.writeLong(o.asLong());
        return buf;
    }

    public static <B extends RegistryFriendlyByteBuf, T extends Record> StreamCodec<B, T> recordStreamCodec() { return recordStreamCodec(Utils.classOfGeneric()); }
    public static <B extends RegistryFriendlyByteBuf, T extends Record> StreamCodec<B, T> recordStreamCodec(Class<T> rec) {
        var comps = rec.getRecordComponents();
        var types = new Class[comps.length];
        for (int i = 0; i < comps.length; ++i)
            types[i] = comps[i].getType();
        Constructor<T> n = Utils.nullableConstructor(rec, types);
        StreamDecoder<B, T> reader = b->{
            Object[] params = new Object[comps.length];
            for (int i = 0; i < comps.length; ++i)
                params[i] = read(b, types[i]);
            return Utils.nullableInstance(n, params);
        };
        StreamEncoder<B, T> writer = (b, t)->{
            for (var comp : comps)
                write(b, Utils.getComponent(comp, t));
        };
        return StreamCodec.of(writer, reader);
    }

    public static <B extends RegistryFriendlyByteBuf, T> StreamCodec<B, T> autoStramCodec() { return autoStramCodec(Utils.classOfGeneric()); }
    public static <B extends RegistryFriendlyByteBuf, T> StreamCodec<B, T> autoStramCodec(Class<T> clazz) {
        if (clazz.isArray()) {
            Class<?> c = clazz.getComponentType();
            if (Boolean.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)BOOL_ARRAY_STREAM_CODEC;
            if (Byte.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)BYTE_ARRAY_STREAM_CODEC;
            if (Character.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)CHAR_ARRAY_STREAM_CODEC;
            if (Short.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)SHORT_ARRAY_STREAM_CODEC;
            if (Integer.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)INT_ARRAY_STREAM_CODEC;
            if (Long.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)LONG_ARRAY_STREAM_CODEC;
            if (Float.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)FLOAT_ARRAY_STREAM_CODEC;
            if (Double.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)DOUBLE_ARRAY_STREAM_CODEC;
            if (String.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)STR_ARRAY_STREAM_CODEC;
            if (BlockPos.class.isAssignableFrom(c))
                return (StreamCodec<B, T>)POS_ARRAY_STREAM_CODEC;
        }
        if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)BOOL_STREAM_CODEC;
        if (Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)BYTE_STREAM_CODEC;
        if (Character.class.isAssignableFrom(clazz) || char.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)CHAR_STREAM_CODEC;
        if (Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)SHORT_STREAM_CODEC;
        if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)INT_STREAM_CODEC;
        if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)LONG_STREAM_CODEC;
        if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)FLOAT_STREAM_CODEC;
        if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)DOUBLE_STREAM_CODEC;
        if (String.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)STR_STREAM_CODEC;
        if (BlockPos.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)POS_STREAM_CODEC;
        if (Record.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>)recordStreamCodec((Class<Record>) clazz);
        return null;
    }
}
