package com.limachi.arss.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class ClassExtractor {
    public final List<Class<?>> classes = new ArrayList<>();

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

    public ClassExtractor() { extractClasses(null, null, false); }
    public ClassExtractor(String matcher) { extractClasses(null, matcher, false); }
    public ClassExtractor(Class<?> clazz) { extractClasses(clazz, null, false); }
    public ClassExtractor(Class<?> clazz, String matcher) { extractClasses(clazz, matcher, false); }

    public <T extends ClassExtractor> T extractClasses() { extractClasses(null, null, true); return (T)this; }
    public <T extends ClassExtractor> T extractClasses(String matcher) { extractClasses(null, matcher, true); return (T)this; }
    public <T extends ClassExtractor> T extractClasses(Class<?> clazz, String matcher, boolean clear) {
        JarFile jarFile;
        Pattern pattern;
        if (clazz == null)
            clazz = ClassExtractor.class;
        if (clear)
            classes.clear();
        if (matcher == null || matcher.isBlank())
            pattern = Pattern.compile(".*");
        else
            pattern = Pattern.compile(matcher);
        try {
            jarFile = new JarFile(Objects.requireNonNull(getJarPath(clazz)));
        } catch (IOException | NullPointerException ignore) {
            return (T)this;
        }
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.endsWith(".class") && !entry.isDirectory() && pattern.matcher(entryName).find())
                try {
                    classes.add(clazz.getClassLoader().loadClass(entryName.replace("/", ".").replace(".class", "")));
                } catch (Throwable ignore) {}
        }
        try {
            jarFile.close();
        } catch (IOException ignore) {}
        return (T)this;
    }
}
