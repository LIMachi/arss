package com.limachi.arss.utils.codec;

import com.limachi.arss.utils.ModBase;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;

import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Codecs {
    public static final Codec<Boolean> BOOL = Codec.BOOL;
    public static final Codec<Byte> BYTE = Codec.BYTE;
    public static final Codec<Character> CHAR = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Character> read(DynamicOps<T> ops, T input) { return ops.getNumberValue(input).map(n->(char)n.intValue()); }
        @Override
        public <T> T write(DynamicOps<T> ops, Character value) { return ops.createInt(value); }
    };
    public static final Codec<Short> SHORT = Codec.SHORT;
    public static final Codec<Integer> INT = Codec.INT;
    public static final Codec<Long> LONG = Codec.LONG;
    public static final Codec<Float> FLOAT = Codec.FLOAT;
    public static final Codec<Double> DOUBLE = Codec.DOUBLE;
    public static final Codec<String> STR = Codec.STRING;
    public static final Codec<BlockPos> POS = new PrimitiveCodec<BlockPos>() {
        @Override
        public <T> DataResult<BlockPos> read(DynamicOps<T> ops, T input) { return ops.getNumberValue(input).map(n->BlockPos.of(n.longValue())); }
        @Override
        public <T> T write(DynamicOps<T> ops, BlockPos value) { return ops.createLong(value.asLong()); }
    };
    public static final Codec<Boolean[]> BOOL_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<boolean[]> PRIM_BOOL_ARRAY = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<boolean[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getBooleanValue(e).isSuccess())) {
                    var out = new boolean[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getBooleanValue(list.get(i)).result().get();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, boolean[] value) {
            T[] s = (T[])new Object[value.length];
            for (int i = 0; i < value.length; ++i)
                s[i] = ops.createBoolean(value[i]);
            return ops.createList(Arrays.stream(s));
        }
    };
    public static final Codec<Byte[]> BYTE_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<byte[]> PRIM_BYTE_ARRAY = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<byte[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getBooleanValue(e).isSuccess())) {
                    var out = new byte[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().byteValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, byte[] value) {
            T[] s = (T[])new Object[value.length];
            for (int i = 0; i < value.length; ++i)
                s[i] = ops.createByte(value[i]);
            return ops.createList(Arrays.stream(s));
        }
    };
    public static final Codec<Character[]> CHAR_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<char[]> PRIM_CHAR_ARRAY = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<char[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getBooleanValue(e).isSuccess())) {
                    var out = new char[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = (char)ops.getNumberValue(list.get(i)).result().get().intValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, char[] value) {
            T[] s = (T[])new Object[value.length];
            for (int i = 0; i < value.length; ++i)
                s[i] = ops.createInt(value[i]);
            return ops.createList(Arrays.stream(s));
        }
    };
    public static final Codec<Short[]> SHORT_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<short[]> PRIM_SHORT_ARRAY = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<short[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getBooleanValue(e).isSuccess())) {
                    var out = new short[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().shortValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, short[] value) {
            T[] s = (T[])new Object[value.length];
            for (int i = 0; i < value.length; ++i)
                s[i] = ops.createShort(value[i]);
            return ops.createList(Arrays.stream(s));
        }
    };
    public static final Codec<Integer[]> INT_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<int[]> PRIM_INT_ARRAY = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<int[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getBooleanValue(e).isSuccess())) {
                    var out = new int[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().intValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, int[] value) {
            T[] s = (T[])new Object[value.length];
            for (int i = 0; i < value.length; ++i)
                s[i] = ops.createInt(value[i]);
            return ops.createList(Arrays.stream(s));
        }
    };
    public static final Codec<Long[]> LONG_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<long[]> PRIM_LONG_ARRAY = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<long[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getBooleanValue(e).isSuccess())) {
                    var out = new long[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().longValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, long[] value) {
            T[] s = (T[])new Object[value.length];
            for (int i = 0; i < value.length; ++i)
                s[i] = ops.createLong(value[i]);
            return ops.createList(Arrays.stream(s));
        }
    };
    public static final Codec<Float[]> FLOAT_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<float[]> PRIM_FLOAT_ARRAY = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<float[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getBooleanValue(e).isSuccess())) {
                    var out = new float[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().floatValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, float[] value) {
            T[] s = (T[])new Object[value.length];
            for (int i = 0; i < value.length; ++i)
                s[i] = ops.createFloat(value[i]);
            return ops.createList(Arrays.stream(s));
        }
    };
    public static final Codec<Double[]> DOUBLE_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<double[]> PRIM_DOUBLE_ARRAY = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<double[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.stream().allMatch(e->ops.getBooleanValue(e).isSuccess())) {
                    var out = new double[list.size()];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = ops.getNumberValue(list.get(i)).result().get().doubleValue();
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, double[] value) {
            T[] s = (T[])new Object[value.length];
            for (int i = 0; i < value.length; ++i)
                s[i] = ops.createDouble(value[i]);
            return ops.createList(Arrays.stream(s));
        }
    };
    public static final Codec<String[]> STR_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<BlockPos[]> POS_ARRAY = new PrimitiveCodec<>() {
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
    public static final Codec<Level> LEVEL = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Level> read(DynamicOps<T> ops, T input) {
            return ops.getStringValue(input).flatMap(n->{
                var location = ResourceLocation.parse(n);
                Level out = EnvExecutor.getEnvSpecific(()->()->{
                    var cl = Minecraft.getInstance().level;
                    if (cl == null)
                        return null;
                    return cl.dimension().location().equals(location) ? cl : null;
                }, ()->()->{
                    var server = GameInstance.getServer();
                    if (server == null)
                        return null;
                    return server.getLevel(ResourceKey.create(Registries.DIMENSION, location));
                });
                if (out == null)
                    return DataResult.error(()->"Could not find level: " + n);
                return DataResult.success(out);
            });
        }
        @Override
        public <T> T write(DynamicOps<T> ops, Level value) { return ops.createString(value.dimension().location().toString()); }
    };
    public static <T> Pair<List<T>, Stream<T>> collectSome(int qty, Stream<T> stream) {
        var t = new ArrayList<T>(qty);
        var it = stream.spliterator();
        for (int i = 0; i < qty && it.tryAdvance(t::add); ++i);
        return new Pair<>(t, StreamSupport.stream(it, stream.isParallel()));
    }
    public static Stream<Double> streamFromVec3(Vec3 vec) {
        return Stream.of(vec.x, vec.y, vec.z);
    }
    public static Pair<Vec3, Stream<Double>> streamToVec3(Stream<Double> stream) {
        return collectSome(3, stream).mapFirst(l->{
            if (l.size() == 3)
                return new Vec3(l.get(0), l.get(1), l.get(2));
            return null;
        });
    }
    public static final Codec<Vec3> VEC3 = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Vec3> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if (list.size() == 3 && list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    return DataResult.success(new Vec3(
                            ops.getNumberValue(list.get(0)).result().get().doubleValue(),
                            ops.getNumberValue(list.get(1)).result().get().doubleValue(),
                            ops.getNumberValue(list.get(2)).result().get().doubleValue()
                    ));
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Vec3 value) {
            return ops.createList(streamFromVec3(value).map(ops::createDouble));
        }
    };
    public static final Codec<Vec3[]> VEC3_ARRAY = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Vec3[]> read(DynamicOps<T> ops, T input) {
            return ops.getStream(input).flatMap(s->{
                List<T> list = s.toList();
                if ((list.size() % 3 == 0) && list.stream().allMatch(e->ops.getNumberValue(e).isSuccess())) {
                    var out = new Vec3[list.size() / 3];
                    for (int i = 0; i < out.length; ++i)
                        out[i] = new Vec3(
                                ops.getNumberValue(list.get(i * 3)).result().get().doubleValue(),
                                ops.getNumberValue(list.get(i * 3 + 1)).result().get().doubleValue(),
                                ops.getNumberValue(list.get(i * 3 + 2)).result().get().doubleValue()
                        );
                    return DataResult.success(out);
                }
                return DataResult.error(()-> "Some elements did not match the expected type: " + input);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Vec3[] value) {
            return ops.createList(Arrays.stream(value).flatMap(Codecs::streamFromVec3).map(ops::createDouble));
        }
    };
    public static final Codec<Tag> TAG = Codec.PASSTHROUGH.comapFlatMap((dynamic) -> DataResult.success(dynamic.convert(NbtOps.INSTANCE).getValue().copy()), (tag) -> new Dynamic<>(NbtOps.INSTANCE, tag.copy()));
    public static final Codec<CompoundTag> COMPOUND_TAG = CompoundTag.CODEC;
    private static final HashMap<Class<?>, Codec<?>> CODECS = new HashMap<>();

    static {
        CODECS.put(boolean[].class.componentType(), BOOL);
        CODECS.put(Boolean.class, BOOL);
        CODECS.put(boolean[].class, PRIM_BOOL_ARRAY);
        CODECS.put(Boolean[].class, BOOL_ARRAY);

        CODECS.put(byte[].class.componentType(), BYTE);
        CODECS.put(Byte.class, BYTE);
        CODECS.put(byte[].class, PRIM_BYTE_ARRAY);
        CODECS.put(Byte[].class, BYTE_ARRAY);

        CODECS.put(char[].class.componentType(), CHAR);
        CODECS.put(Character.class, CHAR);
        CODECS.put(char[].class, PRIM_CHAR_ARRAY);
        CODECS.put(Character[].class, CHAR_ARRAY);

        CODECS.put(short[].class.componentType(), SHORT);
        CODECS.put(Short.class, SHORT);
        CODECS.put(short[].class, PRIM_SHORT_ARRAY);
        CODECS.put(Short[].class, SHORT_ARRAY);

        CODECS.put(int[].class.componentType(), INT);
        CODECS.put(Integer.class, INT);
        CODECS.put(int[].class, PRIM_INT_ARRAY);
        CODECS.put(Integer[].class, INT_ARRAY);

        CODECS.put(long[].class.componentType(), LONG);
        CODECS.put(Long.class, LONG);
        CODECS.put(long[].class, PRIM_LONG_ARRAY);
        CODECS.put(Long[].class, LONG_ARRAY);

        CODECS.put(float[].class.componentType(), FLOAT);
        CODECS.put(Float.class, FLOAT);
        CODECS.put(float[].class, PRIM_FLOAT_ARRAY);
        CODECS.put(Float[].class, FLOAT_ARRAY);

        CODECS.put(double[].class.componentType(), DOUBLE);
        CODECS.put(Double.class, DOUBLE);
        CODECS.put(double[].class, PRIM_DOUBLE_ARRAY);
        CODECS.put(Double[].class, DOUBLE_ARRAY);

        CODECS.put(String.class, STR);
        CODECS.put(String[].class, STR_ARRAY);

        CODECS.put(BlockPos.class, POS);
        CODECS.put(BlockPos[].class, POS_ARRAY);
        CODECS.put(BlockPos.MutableBlockPos.class, POS);
        CODECS.put(BlockPos.MutableBlockPos[].class, POS_ARRAY);

        CODECS.put(Level.class, LEVEL);

        CODECS.put(Vec3.class, VEC3);
        CODECS.put(Vec3[].class, VEC3_ARRAY);

        CODECS.put(Tag.class, TAG);

        CODECS.put(CompoundTag.class, COMPOUND_TAG);
    }

    public static <T> Codec<T> getCodec(Class<T> clazz) {
        if (!CODECS.containsKey(clazz))
            ModBase.logger.error("no codec defined for class: " + clazz);
        return (Codec<T>)CODECS.get(clazz);
    }
}
