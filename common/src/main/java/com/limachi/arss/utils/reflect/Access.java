package com.limachi.arss.utils.reflect;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
public interface Access<O> extends Supplier<O>, Consumer<O> {
    O get();
    boolean set(O val);

    @Override
    default void accept(O o) { set(o); }

    Access<?> NULL = new Access<>() {
        @Override
        public Object get() {
            return null;
        }

        @Override
        public boolean set(Object val) {
            return false;
        }
    };

    static <T> Access<T> of(Supplier<T> get, Function<T, Boolean> set) {
        return new Access<>() {
            @Override
            public T get() {
                return get.get();
            }

            @Override
            public boolean set(T val) {
                return set.apply(val);
            }
        };
    }

    static <T> Access<T> of(Supplier<T> get, Consumer<T> set) {
        return new Access<>() {
            @Override
            public T get() {
                return get.get();
            }

            @Override
            public boolean set(T val) {
                set.accept(val);
                return true;
            }
        };
    }

    static <T> Access<T> of(Function<T, Boolean> set) {
        return new Access<>() {
            @Override
            public T get() {
                return null;
            }

            @Override
            public boolean set(T val) {
                return set.apply(val);
            }
        };
    }

    static <T> Access<T> of(Consumer<T> set) {
        return new Access<>() {
            @Override
            public T get() {
                return null;
            }

            @Override
            public boolean set(T val) {
                set.accept(val);
                return true;
            }
        };
    }

    static <T> Access<T> of(Supplier<T> get) {
        return new Access<>() {
            @Override
            public T get() {
                return get.get();
            }

            @Override
            public boolean set(T val) {
                return false;
            }
        };
    }
}
