package com.limachi.arss.utils.reflect;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.StackTrace;

import java.lang.reflect.Field;

@SuppressWarnings({"unchecked", "unused"})
//the unchecked are expected to throw an error on invalid cast
public class FieldAccess<C, T> implements InstancedAccess<C, T>, Named {
    public static boolean LOG = true;

    private final Field field;
    private final C object;
    private final Class<C> clazz;
    private final Class<T> type;

    public FieldAccess(C object, Field field) {
        this.field = field;
        this.object = object;
        this.clazz = (Class<C>) object.getClass();
        this.type = (Class<T>)field.getType();
    }

    public FieldAccess(Class<C> clazz, Field field) {
        this.field = field;
        this.object = null;
        this.clazz = clazz;
        this.type = (Class<T>)field.getType();
    }

    //names is a list of string for compatibility with obfuscation, put the most probable (obfuscated) name first
    public static <C, T> FieldAccess<C, T> of(Class<C> clazz, Class<T> type, String ... names) {
        for (String name : names) {
            try {
                Field f = clazz.getField(name);
                if (f.getType().equals(type))
                    return new FieldAccess<>(clazz, f);
            } catch (Throwable ignore) {}
        }
        return null;
    }

    //names is a list of string for compatibility with obfuscation, put the most probable (obfuscated) name first
    public static <C, T> FieldAccess<C, T> of(C object, Class<T> type, String ... names) {
        Class<C> clazz = (Class<C>) object.getClass();
        for (String name : names) {
            try {
                Field f = clazz.getField(name);
                if (f.getType().equals(type))
                    return new FieldAccess<>(clazz, f);
            } catch (Throwable ignore) {}
        }
        return null;
    }

    @Override
    public T get() { return get(object, LOG); }
    public T get(C object, boolean log) {
        try {
            return (T) field.get(object);
        } catch (Exception e) {
            if (log)
                ModBase.logger.error(new StackTrace(e));
            return null;
        }
    }

    @Override
    public boolean set(T val) { return set(object, LOG, val); }
    public boolean set(C object, boolean log, T val) {
        try {
            field.set(object, val);
            return true;
        } catch (Exception e) {
            if (log)
                ModBase.logger.error(new StackTrace(e));
            return false;
        }
    }

    @Override
    public void accept(C c, T t) {
        set(c, LOG, t);
    }

    @Override
    public T apply(C c) {
        return get(c, LOG);
    }

    @Override
    public String name() { return field.getName(); }

    public Class<C> clazz() { return clazz; }

    public C clazzInstance() { return object; }

    public Class<T> type() { return type; }
}
