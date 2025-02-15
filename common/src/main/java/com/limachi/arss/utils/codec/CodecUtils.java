package com.limachi.arss.utils.codec;

import com.limachi.arss.utils.reflect.ReflectUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class CodecUtils {
    public static <T extends Record> Codec<T> recordCodec() { return recordCodec(ReflectUtils.classOfGeneric()); }
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
                case 1 -> b.group(codecs[0]).apply(b, p-> ReflectUtils.nullableInstance(n, p));
                case 2 -> b.group(codecs[0], codecs[1]).apply(b, (p0, p1)-> ReflectUtils.nullableInstance(n, p0, p1));
                case 3 -> b.group(codecs[0], codecs[1], codecs[2]).apply(b, (p0, p1, p2)-> ReflectUtils.nullableInstance(n, p0, p1, p2));
                case 4 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3]).apply(b, (p0, p1, p2, p3)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3));
                case 5 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4]).apply(b, (p0, p1, p2, p3, p4)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4));
                case 6 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5]).apply(b, (p0, p1, p2, p3, p4, p5)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5));
                case 7 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6]).apply(b, (p0, p1, p2, p3, p4, p5, p6)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6));
                case 8 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7));
                case 9 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8));
                case 10 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9));
                case 11 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));
                case 12 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11));
                case 13 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11], codecs[12]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12));
                case 14 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11], codecs[12], codecs[13]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13));
                case 15 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11], codecs[12], codecs[13], codecs[14]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14));
                case 16 -> b.group(codecs[0], codecs[1], codecs[2], codecs[3], codecs[4], codecs[5], codecs[6], codecs[7], codecs[8], codecs[9], codecs[10], codecs[11], codecs[12], codecs[13], codecs[14], codecs[15]).apply(b, (p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15)-> ReflectUtils.nullableInstance(n, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15));
                default -> throw new IllegalStateException("Unexpected value: " + comps.length);
            };
        });
    }

    public static <T> Codec<T> autoCodec() { return autoCodec(ReflectUtils.classOfGeneric()); }
    public static <T> Codec<T> autoCodec(Class<T> clazz) {
        if (Record.class.isAssignableFrom(clazz))
            return (Codec<T>)recordCodec((Class<Record>) clazz);
        return Codecs.getCodec(clazz);
    }

    public static <B extends RegistryFriendlyByteBuf, T> T read(B buf) { return read(buf, ReflectUtils.classOfGeneric()); }
    public static <B extends RegistryFriendlyByteBuf, T> T read(B buf, Class<T> clazz) {
        return StreamCodecs.getCodec(clazz).decode(buf);
    }

    public static <B extends RegistryFriendlyByteBuf, T> B write(B buf, T obj) {
        StreamCodec<RegistryFriendlyByteBuf, T> codec = (StreamCodec<RegistryFriendlyByteBuf, T>) StreamCodecs.getCodec(obj.getClass());
        codec.encode(buf, obj);
        return buf;
    }

    public static <B extends RegistryFriendlyByteBuf, T extends Record> StreamCodec<B, T> recordStreamCodec() { return recordStreamCodec(ReflectUtils.classOfGeneric()); }
    public static <B extends RegistryFriendlyByteBuf, T extends Record> StreamCodec<B, T> recordStreamCodec(Class<T> rec) {
        var comps = rec.getRecordComponents();
        var types = new Class[comps.length];
        for (int i = 0; i < comps.length; ++i)
            types[i] = comps[i].getType();
        Constructor<T> n = ReflectUtils.nullableConstructor(rec, types);
        StreamDecoder<B, T> reader = b->{
            Object[] params = new Object[comps.length];
            for (int i = 0; i < comps.length; ++i)
                params[i] = read(b, types[i]);
            return ReflectUtils.nullableInstance(n, params);
        };
        StreamEncoder<B, T> writer = (b, t)->{
            for (var comp : comps)
                write(b, ReflectUtils.getComponent(comp, t));
        };
        return StreamCodec.of(writer, reader);
    }

    public static <B extends RegistryFriendlyByteBuf, T> StreamCodec<B, T> autoStreamCodec() { return autoStreamCodec(ReflectUtils.classOfGeneric()); }
    public static <B extends RegistryFriendlyByteBuf, T> StreamCodec<B, T> autoStreamCodec(Class<T> clazz) {
        if (Record.class.isAssignableFrom(clazz))
            return (StreamCodec<B, T>) recordStreamCodec((Class<Record>) clazz);
        return (StreamCodec<B, T>) StreamCodecs.getCodec(clazz);
    }
}
