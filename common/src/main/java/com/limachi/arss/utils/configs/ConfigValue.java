package com.limachi.arss.utils.configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ConfigValue implements Configs.ConfigSerializer {
    public static final ConfigValue EMPTY = new ConfigValue();

    protected Object hold;
    protected ConfigValueType type;
    protected String comment = null;
    protected ArrayList<String> cached = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigValue that = (ConfigValue) o;
        return type == that.type && Objects.equals(hold, that.hold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hold, type);
    }

    private ConfigValue() {
        hold = null;
        type = ConfigValueType.NULL;
    }

    public ConfigValue(Object hold) {
        if (hold instanceof ConfigValueType type) {
            this.type = type;
            this.hold = switch (type) {
                case STRING -> "";
                case BOOLEAN -> false;
                case INT -> 0;
                case FLOAT -> 0d;
                case LIST -> new ArrayList<ConfigValue>();
                case MAP -> new HashMap<ConfigValue, ConfigValue>();
                default -> null;
            };
        } else if (hold == null) {
            this.type = ConfigValueType.NULL;
            this.hold = null;
        } else if (hold instanceof Number num) {
            if (num instanceof Float || num instanceof Double) {
                type = ConfigValueType.FLOAT;
                this.hold = num.doubleValue();
            } else {
                type = ConfigValueType.INT;
                this.hold = num.longValue();
            }
        } else {
            if (hold instanceof String) {
                type = ConfigValueType.STRING;
                this.hold = hold;
            } else if (hold instanceof Boolean) {
                type = ConfigValueType.BOOLEAN;
                this.hold = hold;
            } else if (hold instanceof Iterable<?> it) {
                type = ConfigValueType.LIST;
                var list = new ArrayList<ConfigValue>();
                it.forEach(i->list.add(new ConfigValue(i)));
                this.hold = list;
            }
            else if (hold instanceof Map<?,?> it) {
                type = ConfigValueType.MAP;
                var map = new HashMap<ConfigValue, ConfigValue>();
                it.forEach((k, v)->map.put(new ConfigValue(k), new ConfigValue(k)));
                this.hold = map;
            } else {
//                    this.hold = hold;
//                    type = ConfigValueType.OBJECT; //FIXME, should be transformed to a special map of field access, if I do object representation
                this.hold = null;
                type = ConfigValueType.NULL;
            }
        }
    }

    public ConfigValueType type() { return type; }
    /**should only be called with String, Boolean, Long and Double!*/
    public <T> T unsafeCast() {
        if (type.primitive())
            return (T)hold;
        return (T)null;
    }

    public ArrayList<ConfigValue> innerList() {
        if (type == ConfigValueType.LIST)
            return (ArrayList<ConfigValue>)hold;
        return null;
    }

    public HashMap<ConfigValue, ConfigValue> innerMap() {
        if (type == ConfigValueType.MAP)
            return (HashMap<ConfigValue, ConfigValue>)hold;
        return null;
    }

    public boolean isNull() {
        return this == EMPTY || hold == null || type == ConfigValueType.NULL;
    }

    protected static String transform(ConfigStyle s, ConfigValue value, int depth, Function<Integer, ConfigStyle> style, boolean isKey) {
        String t = value.flatString(depth, style);
        return s.transformer(t, isKey, value);
    }

    protected String flatString(int depth, Function<Integer, ConfigStyle> style) {
        ConfigStyle s = style.apply(depth);
        return switch (type) {
            case STRING -> s.stringDelimiter + ((String)hold).replace(s.stringDelimiter, "\\" + s.stringDelimiter) + s.stringDelimiter;
            case BOOLEAN -> (Boolean)hold ? s.booleanTrue : s.booleanFalse;
            case INT -> ((Long)hold).toString();
            case FLOAT -> ((Double)hold).toString();
            case LIST -> {
                StringBuilder b = new StringBuilder(s.listStarter);
                var l = innerList().iterator();
                while (l.hasNext()) {
                    b.append(transform(s, l.next(), depth + 1, style, false));
                    if (l.hasNext())
                        b.append(s.separator).append(s.spacer);
                }
                b.append(s.listFinisher);
                yield b.toString();
            }
            case MAP -> {
                StringBuilder b = new StringBuilder(s.mapStarter);
                var m = innerMap().entrySet().iterator();
                while (m.hasNext()) {
                    var p = m.next();
                    b.append(transform(s, p.getKey(), depth + 1, style, true));
                    b.append(s.pairSeparator).append(s.spacer);
                    b.append(transform(s, p.getValue(), depth + 1, style, false));
                    if (m.hasNext())
                        b.append(s.separator).append(s.spacer);
                }
                b.append(s.mapFinisher);
                yield b.toString();
            }
            case NULL -> s.nullToken;
        };
    }

    protected void computeCache(int depth, Function<Integer, ConfigStyle> style) {
        cached.clear();
        if (comment != null)
            cached.add(style.apply(depth).commentStarter + comment);
        cached.add(flatString(depth, style));
    }

    public ArrayList<String> serialize(boolean recompute, int depth, Function<Integer, ConfigStyle> style) {
        if (recompute)
            computeCache(depth, style);
        return cached;
    }

    public ConfigValue deserialize(ArrayList<String> lines) {

        return EMPTY; //TODO
    }
}
