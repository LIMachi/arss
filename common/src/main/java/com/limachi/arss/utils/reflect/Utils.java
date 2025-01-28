package com.limachi.arss.utils.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

@SuppressWarnings("unchecked")
public class Utils {
    public static <T> T nullableInstance(Constructor<T> n, Object ... parameters) {
        try {
            return n.newInstance(parameters);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException ignore) {
            return null;
        }
    }

    public static <T> Constructor<T> nullableConstructor(Class<T> clazz, Class<?> ... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    public static <R extends Record, T> T getComponent(RecordComponent comp, R rec) {
        try {
            return (T)comp.getAccessor().invoke(rec);
        } catch (InvocationTargetException | IllegalAccessException ignore) {
            return null;
        }
    }

    /**
     * helper function to extract a {@code Class<T>} from a generic T <br>
     * uses a little trick with varargs
     */
    @SafeVarargs
    public static <T> Class<T> classOfGeneric(T ... emptyArg) {
        if (emptyArg.length != 0) throw new IllegalStateException("classOfGeneric expects 0 parameters");
        return (Class<T>) emptyArg.getClass().getComponentType();
    }
}
