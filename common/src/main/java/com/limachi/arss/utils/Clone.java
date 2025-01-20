package com.limachi.arss.utils;

import java.lang.constant.Constable;

public interface Clone<T extends Clone<T>> extends Cloneable {
    T clone();

    /**
     * helper function to clone any object that is either Clone, Cloneable or Constable (in the latter case, we return the object itself)
     * @param o (any object)
     * @return either a clone of the object (Clone/Cloneable), the object itself (Constable) or null
     * @param <O> ensures that the output is of same type as the input (expected behavior of a clone)
     */
    @SuppressWarnings({"unused", "unchecked"})
    static <O> O tryClone(O o) {
        if (o instanceof Constable)
            return o;
        if (o instanceof Clone<?> c)
            return (O) c.clone();
        if (o instanceof Cloneable)
            try {
                return (O) o.getClass().getMethod("clone").invoke(o);
            } catch (Exception ignored) {}
        return null;
    }
}
