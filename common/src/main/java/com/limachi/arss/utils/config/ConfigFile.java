package com.limachi.arss.utils.config;

import com.limachi.arss.utils.reflect.FieldAccess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConfigFile {
    protected record Serializer<T>(Function<T, String> serialize, Function<String, T> deserialize){}

    protected static final HashMap<Class<?>, Serializer<?>> SERIALIZERS = new HashMap<>();

    protected static <T, P> void registerSerializer(Class<T> clazz, Class<P> prim, Function<T, String> serialize, Function<String, T> deserialize) {
        var s = new Serializer<T>(serialize, deserialize);
        SERIALIZERS.put(clazz, s);
        if (prim != null)
            SERIALIZERS.put(prim, s);
    }

    static {
        registerSerializer(Boolean.class, boolean.class, Object::toString, s->s.matches("[Ff][Aa][Ll][Ss][Ee]|[Tt][Rr][Uu][Ee]") ? Boolean.parseBoolean(s) : null);
        registerSerializer(Byte.class, byte.class, Object::toString, s->{try { return Byte.parseByte(s); } catch (NumberFormatException ignore) { return null; }});
        registerSerializer(Short.class, short.class, Object::toString, s->{try { return Short.parseShort(s); } catch (NumberFormatException ignore) { return null; }});
        registerSerializer(Integer.class, int.class, Object::toString, s->{try { return Integer.parseInt(s); } catch (NumberFormatException ignore) { return null; }});
        registerSerializer(Long.class, long.class, Object::toString, s->{try { return Long.parseLong(s); } catch (NumberFormatException ignore) { return null; }});
        registerSerializer(Float.class, float.class, Object::toString, s->{try { return Float.parseFloat(s); } catch (NumberFormatException ignore) { return null; }});
        registerSerializer(Double.class, double.class, Object::toString, s->{try { return Double.parseDouble(s); } catch (NumberFormatException ignore) { return null; }});
        registerSerializer(String.class, null, s->s, s->s);
    }
    public record ConfigValue<T, O>(String comment, FieldAccess<?, T> access, Function<O, O> validator, boolean reload){
        private O read0(Class<O> clazz, String value) {
            var ser = SERIALIZERS.get(clazz);
            if (ser == null)
                return null;
            O t = (O)ser.deserialize.apply(value);
            if (t == null)
                return null;
            return validator.apply(t);
        }
        public boolean read(String value) {
            if (access.type().isArray()) {
                Class<O> type = (Class<O>) access.type().getComponentType();
                var lv = readSet(value);
                var ar = Array.newInstance(access.type().getComponentType(), lv.size());
                Object t;
                for (int i = 0; i < lv.size(); ++i)
                    if ((t = read0(type, lv.get(i))) == null)
                        return false;
                    else
                        Array.set(ar, i, t);
                return access.set((T)ar);
            }
            T t = (T)read0((Class<O>) access.type(), value);
            if (t == null)
                return false;
            return access.set(t);
        }

        private String write0(Class<O> clazz, O value) {
            if (value == null)
                return null;
            var ser = SERIALIZERS.get(clazz);
            if (ser == null)
                return null;
            value = validator.apply(value);
            if (value == null)
                return null;
            return ((Function<O, String>)ser.serialize).apply(value);
        }

        public String write() {
            if (access.type().isArray()) {
                Class<O> type = (Class<O>) access.type().getComponentType();
                StringBuilder str = new StringBuilder();
                var ar = access.get();
                for (int i = 0; i < Array.getLength(ar); ++i) {
                    String s = write0(type, (O)Array.get(ar, i));
                    if (s == null)
                        return null;
                    str.append(s);
                    if (i < Array.getLength(ar) - 1)
                        str.append(", ");
                }
                return str.toString();
            }
            return write0((Class<O>)access.type(), (O)access.get());
        }
    }

    protected final Path filePath;

    protected final HashMap<String, ConfigValue<?, ?>> parts = new HashMap<>();

    public <T, O> boolean registerValue(String path, String comment, FieldAccess<?, T> access, Function<O, O> validator, boolean reload) {
        if (access == null || path == null)
            return false;
        if (validator == null)
            validator = o->o;
        parts.put(path, new ConfigValue<>(comment == null || comment.isBlank() ? "" : "#" + comment.replaceAll("\n\r?|\r\n?", "\n#") + "\n", access, validator, reload));
        return true;
    }

    public ConfigFile(String filePath) { this.filePath = new File(filePath).toPath(); }
    public ConfigFile(Path filePath) { this.filePath = filePath; }

    protected static String readKey(String line) {
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        boolean escaped = false;
        int len = 0;
        for (char c : line.toCharArray()) {
            switch (c) {
                case '\\' -> escaped = !escaped;
                case '"' -> {
                    if (!escaped && !inSingleQuote)
                        inDoubleQuote = !inDoubleQuote;
                    escaped = false;
                }
                case '\'' -> {
                    if (!escaped && !inDoubleQuote)
                        inSingleQuote = !inSingleQuote;
                    escaped  = false;
                }
                case ' ', '\t', '\f' -> {}
                case ':', '=' -> {
                    if (!inSingleQuote && !inDoubleQuote && !escaped)
                        return line.substring(0, len);
                    escaped = false;
                }
                default -> escaped = false;
            }
            ++len;
        }
        return null;
    }

    protected static ArrayList<String> readSet(String line) {
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        boolean escaped = false;
        int len = 0;
        int start = 0;
        ArrayList<String> out = new ArrayList<>();
        for (char c : line.toCharArray()) {
            switch (c) {
                case '\\' -> escaped = !escaped;
                case '"' -> {
                    if (!escaped && !inSingleQuote)
                        inDoubleQuote = !inDoubleQuote;
                    escaped = false;
                }
                case '\'' -> {
                    if (!escaped && !inDoubleQuote)
                        inSingleQuote = !inSingleQuote;
                    escaped  = false;
                }
                case ' ', '\t', '\f' -> {}
                case ',', '|' -> {
                    if (!inSingleQuote && !inDoubleQuote && !escaped) {
                        out.add(line.substring(start, len).strip());
                        start = len + 1;
                    }
                    escaped = false;
                }
                default -> escaped = false;
            }
            ++len;
        }
        if (start < len && start < line.length())
            out.add(line.substring(start, len).strip());
        return out;
    }

    public boolean load(boolean reload) {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException ignore) {
            return save();
        }
        for (String line : lines) {
            line = line.strip();
            if (line.isBlank() || line.startsWith("#"))
                continue;
            String key = readKey(line);
            if (key == null)
                continue;
            ConfigValue<?, ?> part = parts.get(key);
            if (part == null || (!part.reload && reload))
                continue;
            if (!part.read(line.substring(key.length() + 1).strip()))
                return false;
        }
        return reload || save();
    }

    public boolean save() {
        if (parts.isEmpty())
            return true;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()));
            var entries = new ArrayList<>(parts.entrySet());
            entries.sort(Map.Entry.comparingByKey());
            for (var entry : entries) {
                String cmt = entry.getValue().comment();
                if (!cmt.isBlank())
                    writer.write(cmt);
                String w = entry.getValue().write();
                if (w == null)
                    return false;
                writer.write(entry.getKey() + "=" + w);
                writer.newLine();
                writer.newLine();
            }
            writer.flush();
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }

    public <T> T get(String path) {
        var p = parts.get(path);
        if (p == null)
            return null;
        return (T) p.access.get();
    }

    public <T> boolean set(String path, T value) {
        var p = parts.get(path);
        if (p == null)
            return false;
        ((ConfigValue<T, ?>)p).access.set(value);
        return true;
    }
}
