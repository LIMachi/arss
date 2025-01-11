package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.RegisterCommand;
import com.limachi.arss.utils.commands.CommandManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AnnotationExtractor extends ClassExtractor {

    protected record RepeatableAnnotation(Class<?> a, Class<?> r, Function<Object, Object[]> getter){}

    public static class ClassAnnotations<C, A extends Annotation> {
        private final Class<C> clazz;
        private A annotation = null;
        private final HashMap<FieldAccess<C>, A> annotatedFields = new HashMap<>();
        private final HashMap<MethodAccess<C>, A> annotatedMethods = new HashMap<>();

        public ClassAnnotations(Class<C> clazz, A annotation) {
            this.clazz = clazz;
            this.annotation = annotation;
        }

        public ClassAnnotations(Class<C> clazz, Field field, A annotation) {
            this.clazz = clazz;
            annotatedFields.put(new FieldAccess<>(clazz, field), annotation);
        }

        public ClassAnnotations(Class<C> clazz, Method method, A annotation) {
            this.clazz = clazz;
            annotatedMethods.put(new MethodAccess<>(clazz, method), annotation);
        }

        public ClassAnnotations(Class<C> clazz, A annotation, Field isField, Method isMethod) {
            this.clazz = clazz;
            setAuto(annotation, isField, isMethod);
        }

        public ClassAnnotations setClassLevelAnnotation(A annotation) {
            this.annotation = annotation;
            return this;
        }

        public ClassAnnotations setFieldAnnotation(Field field, A annotation) {
            annotatedFields.put(new FieldAccess<>(clazz, field), annotation);
            return this;
        }

        public ClassAnnotations setMethodAnnotation(Method method, A annotation) {
            annotatedMethods.put(new MethodAccess<>(clazz, method), annotation);
            return this;
        }

        public ClassAnnotations setAuto(A annotation, Field isField, Method isMethod) {
            if (isField == null && isMethod == null)
                this.annotation = annotation;
            if (isField != null)
                setFieldAnnotation(isField, annotation);
            if (isMethod != null)
                setMethodAnnotation(isMethod, annotation);
            return this;
        }

        public Optional<A> classLevelAnnotation() { return Optional.ofNullable(annotation); }
        public HashMap<FieldAccess<C>, A> fieldAnnotations() { return annotatedFields; }
        public HashMap<MethodAccess<C>, A> methodAnnotations() { return annotatedMethods; }

        public void run(BiConsumer<Class<?>, A> runOnClass, BiConsumer<FieldAccess<?>, A> runOnFields, BiConsumer<MethodAccess<?>, A> runOnMethods) {
            if (annotation != null && runOnClass != null)
                runOnClass.accept(clazz, annotation);
            if (runOnFields != null)
                for (var e : annotatedFields.entrySet())
                    runOnFields.accept(e.getKey(), e.getValue());
            if (runOnMethods != null)
                for (var e : annotatedMethods.entrySet())
                    runOnMethods.accept(e.getKey(), e.getValue());
        }
    }

    protected HashMap<Class<? extends Annotation>, HashMap<Class<?>, ClassAnnotations<?, ?>>> annotations;
    protected static HashMap<Class<? extends Annotation>, HashMap<Class<?>, RepeatableAnnotation>> repeaters = new HashMap<>();

    protected <C, A extends Annotation> void insert(Class<C> clazz, A annotation, Field isField, Method isMethod) {
        annotations.computeIfAbsent(annotation.annotationType(), k->new HashMap<>());
        var cah = annotations.get(annotation.annotationType());
        ClassAnnotations<C, A> i = (ClassAnnotations<C, A>)cah.get(clazz);
        if (i == null)
            cah.put(clazz, new ClassAnnotations<>(clazz, annotation, isField, isMethod));
        else
            i.setAuto(annotation, isField, isMethod);
    }

    public static <A extends Annotation, R extends Annotation> void registerRepeatableAnnotation(Class<A> repeatable, Class<R> from, Function<R, A[]> yield) {
        repeaters.computeIfAbsent(repeatable, k->new HashMap<>());
        repeaters.get(repeatable).put(from, new RepeatableAnnotation(repeatable, from, (Function<Object, Object[]>)(Object)yield));
    }

    @Override
    public <T extends ClassExtractor> T extractClasses(Class<?> clazz, String matcher, boolean clear) {
        if (annotations == null)
            annotations = new HashMap<>();
        super.extractClasses(clazz, matcher, clear);
        if (clear)
            annotations.clear();
        for (Class<?> c : classes) {
            for (Annotation a : c.getAnnotations())
                insert(c, a, null, null);
            for (Field f : c.getDeclaredFields())
                for (Annotation a : f.getAnnotations())
                    insert(c, a, f, null);
            for (Method m : c.getDeclaredMethods())
                for (Annotation a : m.getAnnotations())
                    insert(c, a, null, m);
        }
        return (T)this;
    }

    public <A extends Annotation> void runOnClasses(Class<A> type, BiConsumer<Class<?>, A> run) { runAnnotations(type, run, null, null); }
    public <A extends Annotation> void runOnFields(Class<A> type, BiConsumer<FieldAccess<?>, A> run) { runAnnotations(type, null, run, null); }
    public <A extends Annotation> void runOnMethods(Class<A> type, BiConsumer<MethodAccess<?>, A> run) { runAnnotations(type, null, null, run); }
    public <A extends Annotation> void runAnnotations(Class<A> type, BiConsumer<Class<?>, A> runOnClasses, BiConsumer<FieldAccess<?>, A> runOnFields, BiConsumer<MethodAccess<?>, A> runOnMethods) {
        var cah = annotations.get(type);
        if (cah != null)
            for (var e : cah.entrySet())
                ((ClassAnnotations<?, A>)e.getValue()).run(runOnClasses, runOnFields, runOnMethods);
        var re = repeaters.get(type);
        if (re != null)
            for (var er : re.entrySet()) {
                cah = annotations.get(er.getKey());
                if (cah != null)
                    for (var e : cah.entrySet()) {
                        var ca = e.getValue().annotation;
                        if (ca != null && runOnClasses != null)
                            for (var a : er.getValue().getter.apply(ca))
                                runOnClasses.accept(e.getKey(), (A)a);
                        if (runOnFields != null)
                            for (var f : e.getValue().annotatedFields.entrySet())
                                for (var a : er.getValue().getter.apply(f.getValue()))
                                    runOnFields.accept(f.getKey(), (A)a);
                        if (runOnMethods != null)
                            for (var m : e.getValue().annotatedMethods.entrySet())
                                for (var a : er.getValue().getter.apply(m.getValue()))
                                    runOnMethods.accept(m.getKey(), (A)a);
                    }

            }
    }

    static {
        registerRepeatableAnnotation(RegisterCommand.class, CommandManager.RegisterCommands.class, CommandManager.RegisterCommands::value);
    }
}
