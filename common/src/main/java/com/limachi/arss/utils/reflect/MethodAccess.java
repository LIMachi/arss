package com.limachi.arss.utils.reflect;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.StackTrace;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;

@SuppressWarnings({"unchecked", "unused"})
public class MethodAccess<C, T> implements InstancedAccess<C, T>, Named {
    public static boolean LOG = true;

    private final Method method;
    private final C object;
    private final Class<C> clazz;
    private final Class<T> type;

    MethodAccess(Class<C> clazz, Method method) {
        this.method = method;
        this.object = null;
        this.clazz = clazz;
        this.type = (Class<T>)method.getReturnType(); //used as an assertion
    }

    MethodAccess(C object, Method method) {
        this.method = method;
        this.object = object;
        this.clazz = (Class<C>) object.getClass();
        this.type = (Class<T>)method.getReturnType(); //used as an assertion
    }

    public static <C, T> MethodAccess<C, T> of(Class<C> clazz, Class<T> ret, String name, Class<?> ... parameters) { return of(clazz, ret, Collections.singleton(name), parameters); }
    public static <C, T> MethodAccess<C, T> of(Class<C> clazz, Class<T> ret, Iterable<String> names, Class<?> ... parameters) {
        for (String name : names) {
            try {
                Method m = clazz.getMethod(name, parameters);
                if (m.getReturnType().equals(ret)) {
                    m.setAccessible(true);
                    return new MethodAccess<>(clazz, m);
                }
            } catch (Throwable ignore) {}
        }
        return null;
    }

    public static <C, T> MethodAccess<C, T> of(C object, Class<T> ret, String name, Class<?> ... parameters) { return of(object, ret, Collections.singleton(name), parameters); }
    public static <C, T> MethodAccess<C, T> of(C object, Class<T> ret, Iterable<String> names, Class<?> ... parameters) {
        Class<C> clazz = (Class<C>) object.getClass();
        for (String name : names) {
            try {
                Method m = clazz.getMethod(name, parameters);
                if (m.getReturnType().equals(ret)) {
                    m.setAccessible(true);
                    return new MethodAccess<>(object, m);
                }
            } catch (Throwable ignore) {}
        }
        return null;
    }

    @Override
    public T get() {
        return get(object, LOG);
    }

    @Override
    public boolean set(T val) {
        return false;
    }

    @Override
    public T apply(C c) {
        return get(c, LOG);
    }

    @Override
    public void accept(C c, T t) {}

    public T get(C object, boolean log, Object ... params) {
        try {
            return (T) method.invoke(object, params);
        } catch (Exception e) {
            if (log)
                ModBase.logger.error(new StackTrace(e));
            return null;
        }
    }

    @Override
    public String name() { return method.getName(); }

    public Class<C> clazz() { return clazz; }

    public C clazzInstance() { return object; }

    public Parameter[] parameters() { return method.getParameters(); }

    public Class<?> returnType() { return type; }
}
