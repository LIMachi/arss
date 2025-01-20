package com.limachi.arss.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiConsumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ClassAnnotations<C, A extends Annotation> {
    private final Class<C> clazz;
    private A annotation = null;
    private final HashMap<FieldAccess<C, ?>, A> annotatedFields = new HashMap<>();
    private final HashMap<MethodAccess<C, ?>, A> annotatedMethods = new HashMap<>();

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

    public ClassAnnotations<C, A> setClassLevelAnnotation(A annotation) {
        this.annotation = annotation;
        return this;
    }

    public ClassAnnotations<C, A> setFieldAnnotation(Field field, A annotation) {
        annotatedFields.put(new FieldAccess<>(clazz, field), annotation);
        return this;
    }

    public ClassAnnotations<C, A> setMethodAnnotation(Method method, A annotation) {
        annotatedMethods.put(new MethodAccess<>(clazz, method), annotation);
        return this;
    }

    public ClassAnnotations<C, A> setAuto(A annotation, Field isField, Method isMethod) {
        if (isField == null && isMethod == null)
            this.annotation = annotation;
        if (isField != null)
            setFieldAnnotation(isField, annotation);
        if (isMethod != null)
            setMethodAnnotation(isMethod, annotation);
        return this;
    }

    public Optional<A> classLevelAnnotation() { return Optional.ofNullable(annotation); }
    public HashMap<FieldAccess<C, ?>, A> fieldAnnotations() { return annotatedFields; }
    public HashMap<MethodAccess<C, ?>, A> methodAnnotations() { return annotatedMethods; }

    public void run(BiConsumer<Class<?>, A> runOnClass, BiConsumer<FieldAccess<?, ?>, A> runOnFields, BiConsumer<MethodAccess<?, ?>, A> runOnMethods) {
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
