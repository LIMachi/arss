package com.limachi.arss.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.Field;

public class ClassExtractor {
    public static final List<Class<?>> CLASSES = new ArrayList<>();

    public static class FieldAccess<C> {
        private final Field field;
        private final Class<C> clazz;

        FieldAccess(Class<C> clazz, Field field) {
            this.clazz = clazz;
            this.field = field;
        }

        public <T> T getStatic() {
            try {
                return (T) field.get(null);
            } catch (IllegalAccessException e) {
                return null;
            }
        }

        public <T> T getInstanced(C object) {
            try {
                return (T) field.get(object);
            } catch (IllegalAccessException e) {
                return null;
            }
        }

        public <T> boolean setStatic(T val) {
            try {
                field.set(null, val);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        }

        public <T> boolean setInstanced(C object, T val) {
            try {
                field.set(object, val);
                return true;
            } catch (IllegalAccessException e) {
                return false;
            }
        }

        public Class<C> clazz() {
            return clazz;
        }

        public String name() {
            return field.getName();
        }

        public Class<?> type() {
            return field.getType();
        }
    }

    public static class MethodAccess<C> {
        private final Method method;
        private final Class<C> clazz;

        MethodAccess(Class<C> clazz, Method method) {
            this.method = method;
            this.clazz = clazz;
        }

        public <T> T invokeStatic(Object ... params) {
            try {
                return (T) method.invoke(null, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return null;
            }
        }

        public <T> T invokeInstanced(C object, Object ... params) {
            try {
                return (T) method.invoke(object, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return null;
            }
        }

        public Class<C> clazz() {
            return clazz;
        }

        public String name() {
            return method.getName();
        }

        public Parameter[] parameters() {
            return method.getParameters();
        }

        public Class<?> returnType() {
            return method.getReturnType();
        }
    }

    public static String getJarPath(Class<?> clazz) throws IOException {
        String jarPath = URLDecoder.decode(clazz.getProtectionDomain().getCodeSource().getLocation().getFile(), StandardCharsets.UTF_8);
        String[] t = jarPath.split("\\.jar"); //temporary jar files used in debug mode might have invalid characters/numbers after the jar extension
        if (t.length > 1)
            jarPath = t[0] + ".jar";
        File jarFile = new File(jarPath);
        if (jarFile.exists() && jarFile.getName().endsWith(".jar"))
            return jarPath;
        return null;
    }

    public static void extractClasses() {
        extractClasses(null);
    }

    public static void extractClasses(Class<?> clazz) {
        JarFile jarFile;
        if (clazz == null)
            clazz = ClassExtractor.class;
        CLASSES.clear();
        try {
            jarFile = new JarFile(Objects.requireNonNull(getJarPath(clazz)));
        } catch (IOException | NullPointerException ignore) {
            return;
        }
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.endsWith(".class") && !entry.isDirectory())
                try {
                    CLASSES.add(clazz.getClassLoader().loadClass(entryName.replace("/", ".").replace(".class", "")));
                } catch (Throwable ignore) {}
        }
        try {
            jarFile.close();
        } catch (IOException ignore) {}
    }

    public static <A extends Annotation> void runClassAnnotations(Class<A> type, BiConsumer<Class<?>, A> runner) {
        runClassAnnotations(type, null, null, runner);
    }

    public static <A extends Annotation, R extends Annotation> void runClassAnnotations(Class<A> type, Class<R> rep_type, Function<R, A[]> rep_getter, BiConsumer<Class<?>, A> runner) {
        for (Class<?> clazz : CLASSES) {
            A annotation = clazz.getAnnotation(type);
            if (annotation != null)
                runner.accept(clazz, annotation);
            if (rep_type != null && rep_getter != null) {
                R rep = clazz.getAnnotation(rep_type);
                if (rep != null)
                    for (A a : rep_getter.apply(rep))
                        runner.accept(clazz, a);
            }
        }
    }

    public static <A extends Annotation> void runFieldAnnotations(Class<A> type, BiConsumer<FieldAccess<?>, A> runner) {
        runFieldAnnotations(type, null, null, runner);
    }

    public static <A extends Annotation, R extends Annotation> void runFieldAnnotations(Class<A> type, Class<R> rep_type, Function<R, A[]> rep_getter, BiConsumer<FieldAccess<?>, A> runner) {
        for (Class<?> clazz : CLASSES)
            for (Field f : clazz.getDeclaredFields()) {
                A annotation = f.getAnnotation(type);
                if (annotation != null)
                    runner.accept(new FieldAccess<>(clazz, f), annotation);
                if (rep_type != null && rep_getter != null) {
                    R rep = clazz.getAnnotation(rep_type);
                    if (rep != null)
                        for (A a : rep_getter.apply(rep))
                            runner.accept(new FieldAccess<>(clazz, f), a);
                }
            }
    }

    public static <A extends Annotation> void runMethodAnnotations(Class<A> type, BiConsumer<MethodAccess<?>, A> runner) {
        runMethodAnnotations(type, null, null, runner);
    }

    public static <A extends Annotation, R extends Annotation> void runMethodAnnotations(Class<A> type, Class<R> rep_type, Function<R, A[]> rep_getter, BiConsumer<MethodAccess<?>, A> runner) {
        for (Class<?> clazz : CLASSES)
            for (Method m : clazz.getDeclaredMethods()) {
                A annotation = m.getAnnotation(type);
                if (annotation != null)
                    runner.accept(new MethodAccess<>(clazz, m), annotation);
                if (rep_type != null && rep_getter != null) {
                    R rep = clazz.getAnnotation(rep_type);
                    if (rep != null)
                        for (A a : rep_getter.apply(rep))
                            runner.accept(new MethodAccess<>(clazz, m), a);
                }
            }
    }
}
