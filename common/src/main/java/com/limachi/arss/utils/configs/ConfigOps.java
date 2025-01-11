package com.limachi.arss.utils.configs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import java.util.stream.Stream;

public class ConfigOps implements DynamicOps<ConfigValue> {

    public static final ConfigOps INSTANCE = new ConfigOps();

    @Override
    public ConfigValue empty() { return ConfigValue.EMPTY; }

    @Override
    public <U> U convertTo(DynamicOps<U> dynamicOps, ConfigValue configFileValue) {
        return switch (configFileValue.type()) {
            case LIST -> convertList(dynamicOps, configFileValue);
            case MAP/*, OBJECT*/ -> convertMap(dynamicOps, configFileValue);
            case NULL -> dynamicOps.empty();
            case STRING -> dynamicOps.createString(configFileValue.unsafeCast());
            case BOOLEAN -> dynamicOps.createBoolean(configFileValue.unsafeCast());
            case INT -> dynamicOps.createLong(configFileValue.unsafeCast());
            case FLOAT -> dynamicOps.createDouble(configFileValue.unsafeCast());
        };
    }

    @Override
    public DataResult<Number> getNumberValue(ConfigValue configFileValue) {
        return switch (configFileValue.type()) {
            case INT, FLOAT -> DataResult.success((Number)configFileValue.unsafeCast());
            default -> DataResult.error(()->"Not a number: " + configFileValue);
        };
    }

    @Override
    public ConfigValue createNumeric(Number number) {
        return new ConfigValue(number);
    }

    @Override
    public DataResult<String> getStringValue(ConfigValue configFileValue) {
        return configFileValue.type == ConfigValueType.STRING ? DataResult.success((String)configFileValue.hold) : DataResult.error(()->"Not a string: " + configFileValue);
    }

    @Override
    public ConfigValue createString(String s) {
        return new ConfigValue(s);
    }

    @Override
    public DataResult<ConfigValue> mergeToList(ConfigValue list, ConfigValue value) {
        if (list.type != ConfigValueType.LIST && !list.isNull())
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        else {
            ConfigValue out = new ConfigValue(ConfigValueType.LIST);
            var l = list.innerList();
            var t = out.innerList();
            if (!list.isNull() && !l.isEmpty())
                t.addAll(l);
            t.add(value);
            return DataResult.success(out);
        }
    }

    @Override
    public DataResult<ConfigValue> mergeToMap(ConfigValue map, ConfigValue key, ConfigValue value) {
        if (map.type != ConfigValueType.MAP && !map.isNull())
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        else {
            ConfigValue out = new ConfigValue(ConfigValueType.MAP);
            var m = map.innerMap();
            var t = out.innerMap();
            if (!map.isNull() && !m.isEmpty())
                t.putAll(m);
            t.put(key, value);
            return DataResult.success(out);
        }
    }

    @Override
    public DataResult<Stream<Pair<ConfigValue, ConfigValue>>> getMapValues(ConfigValue configFileValue) {
        if (configFileValue.type != ConfigValueType.MAP)
            return DataResult.error(()->"Not a map: " + configFileValue);
        return DataResult.success(configFileValue.innerMap().entrySet().stream().map(e->Pair.of(e.getKey(), e.getValue())));
    }

    @Override
    public ConfigValue createMap(Stream<Pair<ConfigValue, ConfigValue>> stream) {
        ConfigValue out = new ConfigValue(ConfigValueType.MAP);
        var t = out.innerMap();
        stream.forEach(p->t.put(p.getFirst(), p.getSecond()));
        return out;
    }

    @Override
    public DataResult<MapLike<ConfigValue>> getMap(ConfigValue input) {
        if (input.type != ConfigValueType.MAP)
            return DataResult.error(()->"Not a map: " + input);
        return DataResult.success(MapLike.forMap(input.innerMap(), this));
    }

    @Override
    public DataResult<Stream<ConfigValue>> getStream(ConfigValue configFileValue) {
        if (configFileValue.type != ConfigValueType.LIST)
            return DataResult.error(()->"Not a list: " + configFileValue);
        return DataResult.success(configFileValue.innerList().stream());
    }

    @Override
    public ConfigValue createList(Stream<ConfigValue> stream) {
        ConfigValue out = new ConfigValue(ConfigValueType.LIST);
        var t = out.innerList();
        stream.forEach(t::add);
        return out;
    }

    @Override
    public ConfigValue remove(ConfigValue configFileValue, String s) {
        if (configFileValue.type != ConfigValueType.MAP)
            return configFileValue;
        ConfigValue out = new ConfigValue(ConfigValueType.MAP);
        var t = out.innerMap();
        for (var p : configFileValue.innerMap().entrySet())
            if (!p.getKey().cached.equals(s))
                t.put(p.getKey(), p.getValue());
        return out;
    }
}
