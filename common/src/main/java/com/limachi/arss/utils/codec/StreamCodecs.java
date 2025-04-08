package com.limachi.arss.utils.codec;

import com.limachi.arss.utils.Game;
import com.limachi.arss.utils.ModBase;

import io.netty.handler.codec.DecoderException;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

public class StreamCodecs {
    public static final StreamCodec<RegistryFriendlyByteBuf, Boolean> BOOL = StreamCodec.of(RegistryFriendlyByteBuf::writeBoolean, RegistryFriendlyByteBuf::readBoolean);
    public static final StreamCodec<RegistryFriendlyByteBuf, Byte> BYTE = StreamCodec.of((b, i)->b.writeByte(i), FriendlyByteBuf::readByte);
    public static final StreamCodec<RegistryFriendlyByteBuf, Character> CHAR = StreamCodec.of((b, c)->b.writeChar(c), FriendlyByteBuf::readChar);
    public static final StreamCodec<RegistryFriendlyByteBuf, Short> SHORT = StreamCodec.of((b, s)->b.writeShort(s), RegistryFriendlyByteBuf::readShort);
    public static final StreamCodec<RegistryFriendlyByteBuf, Integer> INT = StreamCodec.of(RegistryFriendlyByteBuf::writeInt, RegistryFriendlyByteBuf::readInt);
    public static final StreamCodec<RegistryFriendlyByteBuf, Long> LONG = StreamCodec.of(RegistryFriendlyByteBuf::writeLong, RegistryFriendlyByteBuf::readLong);
    public static final StreamCodec<RegistryFriendlyByteBuf, Float> FLOAT = StreamCodec.of(RegistryFriendlyByteBuf::writeFloat, RegistryFriendlyByteBuf::readFloat);
    public static final StreamCodec<RegistryFriendlyByteBuf, Double> DOUBLE = StreamCodec.of(RegistryFriendlyByteBuf::writeDouble, RegistryFriendlyByteBuf::readDouble);
    public static final StreamCodec<RegistryFriendlyByteBuf, String> STR = StreamCodec.of(RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPos> POS = StreamCodec.of((b, c)->b.writeLong(c.asLong()), b->BlockPos.of(b.readLong()));

    public static final StreamCodec<RegistryFriendlyByteBuf, Boolean[]> BOOL_ARRAY = StreamCodec.of((b, l)->b.writeByteArray(compactBoolArray(l)), b->fromCompactBoolArray(b.readByteArray()));
    public static final StreamCodec<RegistryFriendlyByteBuf, Byte[]> BYTE_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeByte(p);
    }, b->{
        var out = new Byte[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readByte();
        return out;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, Character[]> CHAR_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeChar(p);
    }, b->{
        var out = new Character[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readChar();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, Short[]> SHORT_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeShort(p);
    }, b->{
        var out = new Short[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readShort();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, Integer[]> INT_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeInt(p);
    }, b->{
        var out = new Integer[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readInt();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, Long[]> LONG_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeLong(p);
    }, b->{
        var out = new Long[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readLong();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, Float[]> FLOAT_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeFloat(p);
    }, b->{
        var out = new Float[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readFloat();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, Double[]> DOUBLE_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeDouble(p);
    }, b->{
        var out = new Double[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readDouble();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, boolean[]> PRIM_BOOL_ARRAY = StreamCodec.of((b, l)->b.writeByteArray(primCompactBoolArray(l)), b->primFromCompactBoolArray(b.readByteArray()));
    public static final StreamCodec<RegistryFriendlyByteBuf, byte[]> PRIM_BYTE_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeByte(p);
    }, b->{
        var out = new byte[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readByte();
        return out;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, char[]> PRIM_CHAR_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeChar(p);
    }, b->{
        var out = new char[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readChar();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, short[]> PRIM_SHORT_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeShort(p);
    }, b->{
        var out = new short[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readShort();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, int[]> PRIM_INT_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeInt(p);
    }, b->{
        var out = new int[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readInt();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, long[]> PRIM_LONG_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeLong(p);
    }, b->{
        var out = new long[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readLong();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, float[]> PRIM_FLOAT_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeFloat(p);
    }, b->{
        var out = new float[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readFloat();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, double[]> PRIM_DOUBLE_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeDouble(p);
    }, b->{
        var out = new double[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readDouble();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, String[]> STR_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeUtf(p);
    }, b->{
        String[] out = new String[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readUtf();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPos[]> POS_ARRAY = StreamCodec.of((b, l)->{
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

    public static byte[] primCompactBoolArray(boolean[] ar) {
        if (ar.length == 0)
            return new byte[0];
        int pl = ar.length + 3;
        var out = new byte[(pl >> 3) + ((pl & 7) != 0 ? 1 : 0)];
        out[0] = (byte)(8 - (pl & 7));
        for (int i = 0; i < ar.length; ++i)
            out[(i + 3) >> 3] |= (byte) ((ar[i] ? 1 : 0) << ((i + 3) & 7));
        return out;
    }

    public static boolean[] primFromCompactBoolArray(byte[] ar) {
        if (ar.length == 0)
            return new boolean[0];
        int pad = ar[0] & 7;
        var out = new boolean[(ar.length << 3) - 3 - pad];
        for (int i = 0; i < out.length; ++i)
            out[i] = (ar[(i + 3) >> 3] & (1 << ((i + 3) & 7))) != 0;
        return out;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, Level> LEVEL = StreamCodec.of((b, l)->b.writeUtf(l.dimension().location().toString()), b->Game.getLevel(ResourceLocation.parse(b.readUtf())));

    public static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3 = StreamCodec.of(FriendlyByteBuf::writeVec3, FriendlyByteBuf::readVec3);

    public static final StreamCodec<RegistryFriendlyByteBuf, Vec3[]> VEC3_ARRAY = StreamCodec.of((b, l)->{
        b.writeVarInt(l.length);
        for (var p : l)
            b.writeVec3(p);
    }, b->{
        Vec3[] out = new Vec3[b.readVarInt()];
        for (int i = 0; i < out.length; ++i)
            out[i] = b.readVec3();
        return out;
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, Tag> TAG = StreamCodec.of((b, l)->b.writeNbt(l), b->{
        if (b.readNbt(NbtAccounter.create(2097152L)) instanceof Tag out)
            return out;
        throw new DecoderException("Expected tag");
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, CompoundTag> COMPOUND_TAG = StreamCodec.of((b, l)->b.writeNbt(l), b->{
        if (b.readNbt() instanceof CompoundTag out)
            return out;
        throw new DecoderException("Expected compound tag");
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> STACK = ItemStack.OPTIONAL_STREAM_CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, InteractionHand> HAND = StreamCodec.of((b, l)->b.writeBoolean(l == InteractionHand.OFF_HAND), b-> b.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

    private static final HashMap<Class<?>, StreamCodec<RegistryFriendlyByteBuf, ?>> CODECS = new HashMap<>();

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

        CODECS.put(ItemStack.class, STACK);

        CODECS.put(InteractionHand.class, HAND);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> getCodec(Class<T> clazz) {
        if (!CODECS.containsKey(clazz))
            ModBase.logger.error("no codec defined for class: " + clazz);
        return (StreamCodec<RegistryFriendlyByteBuf, T>)CODECS.get(clazz);
    }
}
