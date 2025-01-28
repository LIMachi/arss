package com.limachi.arss.utils.reflect;

import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked", "unused", "UnusedReturnValue"})
public class ClassExtractor {
    protected final List<Class<?>> classes = new ArrayList<>();

    public static String decodePath(Class<?> clazz) {
        return URLDecoder.decode(clazz.getProtectionDomain().getCodeSource().getLocation().getFile(), StandardCharsets.UTF_8);
    }

    public static JarFile getJarFile(String path) {
        String[] t = path.split("\\.jar");
        if (t.length > 1)
            path = t[0] + ".jar";
        try {
            File jarFile = new File(path);
            if (jarFile.exists() && jarFile.getName().endsWith(".jar"))
                return new JarFile(jarFile);
        } catch (Throwable ignore) {}
        return null;
    }

    public static File getFolder(String path) {
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory())
            return folder;
        return null;
    }

    public ClassExtractor() { extract(null, null, null, false); }
    public ClassExtractor(String matcher) { extract(null, matcher, null, false); }
    public ClassExtractor(String matcher, Predicate<ClassReader> skip) { extract(null, matcher, skip, false); }
    public ClassExtractor(Predicate<ClassReader> skip) { extract(null, null, skip, false); }
    public ClassExtractor(Class<?> clazz) { extract(clazz, null, null, false); }
    public ClassExtractor(Class<?> clazz, String matcher) { extract(clazz, matcher, null, false); }
    public ClassExtractor(Class<?> clazz, String matcher, Predicate<ClassReader> skip) { extract(clazz, matcher, skip, false); }
    public ClassExtractor(Class<?> clazz, Predicate<ClassReader> skip) { extract(clazz, null, skip, false); }

    private static final Pattern DEFAULT_MATCHER = Pattern.compile(".*");
    private Pattern pattern = DEFAULT_MATCHER;
    private Path topFolder = null;
    private Class<?> clazz = ClassExtractor.class;

    public <T extends ClassExtractor> T extract(Class<?> clazz, String matcher, Predicate<ClassReader> skip, boolean clear) {
        Pattern pattern;
        if (clazz == null)
            clazz = ClassExtractor.class;
        if (clear)
            classes.clear();
        if (matcher == null || matcher.isBlank())
            pattern = Pattern.compile(".*");
        else
            pattern = Pattern.compile(matcher);
        String path = decodePath(clazz);
        JarFile jarFile = getJarFile(path);
        if (jarFile != null) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.endsWith(".class") && !entry.isDirectory() && pattern.matcher(entryName).find())
                    try {
                        if (skip != null && skip.test(new ClassReader(jarFile.getInputStream(entry))))
                            continue;
                        classes.add(clazz.getClassLoader().loadClass(entryName.replace("/", ".").replace(".class", "")));
                    } catch (Throwable ignore) {
                        System.err.println(ignore);
                    }
            }
            try {
                jarFile.close();
            } catch (IOException ignore) {}
        } else {
            File folder = getFolder(path);
            if (folder != null) {
                this.pattern = pattern;
                topFolder = folder.toPath();
                this.clazz = clazz;
                recursiveExtractor(folder);
            }
        }
        return (T)this;
    }

    protected void recursiveExtractor(File folder) {
        for (File file : Objects.requireNonNull(folder.listFiles()))
            if (file.isDirectory())
                recursiveExtractor(file);
            else if (file.getName().endsWith(".class")) {
                var p = topFolder.relativize(file.toPath());
                String name = p.toString();
                if (pattern.matcher(name).find()) {
                    try {
                        classes.add(clazz.getClassLoader().loadClass(name.replace("/", ".").replace(".class", "")));
                    } catch (Throwable ignore) {
                        System.err.println(ignore);
                    }
                }
            }
    }
}
