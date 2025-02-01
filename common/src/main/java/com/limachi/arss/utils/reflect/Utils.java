package com.limachi.arss.utils.reflect;

import java.lang.reflect.*;

import sun.misc.Unsafe;

import java.util.Arrays;
import java.util.Comparator;

@SuppressWarnings("unchecked")
public class Utils {
    private static final Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * run the constructor
     * @return null if an exception occurred (silence the error)
     */
    public static <T> T nullableInstance(Constructor<T> n, Object ... parameters) {
        try {
            return n.newInstance(parameters);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException ignore) {
            return null;
        }
    }

    /**
     * get and run the constructor matching the type of the parameters (note that null/untyped parameters always fail)
     * @return null if an exception occurred (silence the error)
     */
    public static <T> T nullableInstance(Class<T> clazz, Object ... parameters) {
        try {
            return getMatchingConstructor(clazz, parameters).newInstance(parameters);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static <T> Constructor<T> getMatchingConstructor(Class<T> clazz, Class<?> ... parameters) {
        for (var c : clazz.getConstructors()) {
            if (c.getParameterCount() != parameters.length)
                continue;
            Parameter[] cp = c.getParameters();
            int i;
            for (i = 0; i < parameters.length; ++i)
                if (!cp[i].getType().isAssignableFrom(parameters[i]))
                    break;
            if (i == parameters.length)
                return (Constructor<T>)c;
        }
        return null;
    }

    public static <T> Constructor<T> getMatchingConstructor(Class<T> clazz, Object ... parameters) {
        for (var c : clazz.getConstructors()) {
            if (c.getParameterCount() != parameters.length)
                continue;
            Parameter[] cp = c.getParameters();
            int i;
            for (i = 0; i < parameters.length; ++i)
                if (!cp[i].getType().isAssignableFrom(parameters[i].getClass()))
                    break;
            if (i == parameters.length)
                return (Constructor<T>)c;
        }
        return null;
    }

    /**
     * get the matching constructor for this class
     * @return null if an exception occurred (silence the error)
     */
    public static <T> Constructor<T> nullableConstructor(Class<T> clazz, Class<?> ... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    /**
     * get a record component or null if not accessible/invalid
     */
    public static <R extends Record, T> T getComponent(RecordComponent comp, R rec) {
        try {
            return (T)comp.getAccessor().invoke(rec);
        } catch (InvocationTargetException | IllegalAccessException ignore) {
            return null;
        }
    }

    /**
     * helper function to extract a {@code Class<T>} from a generic T <br>
     * uses a little trick with varargs (you can get the same result by doing: new T[0].getComponentType())
     */
    @SafeVarargs
    public static <T> Class<T> classOfGeneric(T ... emptyArg) {
        if (emptyArg.length != 0) throw new IllegalStateException("classOfGeneric expects 0 parameters");
        return (Class<T>) emptyArg.getClass().getComponentType();
    }

    /**
     * try to create a new instance WITHOUT running the constructor of the class, useful for pseudo static methods
     * @return null if the class is not instantiable
     */
    public static <T> T unsafeInstance(Class<T> clazz) {
        try {
            return (T)UNSAFE.allocateInstance(clazz);
        } catch (Exception ignore) {}
        return null;
    }

    /**
     * return the default representation of this object in memory
     */
    public static <T> T nullValue(Class<T> clazz) {
        if (!clazz.isPrimitive()) return null;
        if (clazz == boolean.class) return (T)(Boolean)false;
        if (clazz == char.class) return (T)(Character)'\0';
        return (T)(Object)0;
    }

    /**
     * try to create a new instance of the class using null(or 0, '\0', false) values as constructor parameters
     * @return null if the class has no visible constructor
     */
    public static <T> T nulledInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception ignore) {}
        return (T)Arrays.stream(clazz.getConstructors()).sorted(Comparator.comparingInt(Constructor::getParameterCount)).findFirst().map(c->{
            Object[] params = new Object[c.getParameterCount()];
            for (int i = 0; i < params.length; ++i)
                params[i] = nullValue(c.getParameters()[i].getType());
            return nullableInstance(c, params);
        }).orElse(null);
    }
}
