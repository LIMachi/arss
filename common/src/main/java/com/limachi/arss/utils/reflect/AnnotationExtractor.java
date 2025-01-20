package com.limachi.arss.utils.reflect;

import org.objectweb.asm.ClassReader;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "unused"})
public class AnnotationExtractor extends ClassExtractor {

    protected record RepeatableAnnotation(Class<?> a, Class<?> r, Function<Object, Object[]> getter){}

    protected HashMap<Class<? extends Annotation>, HashMap<Class<?>, ClassAnnotations<?, ?>>> annotations;
    protected HashMap<Class<? extends Annotation>, HashMap<Class<?>, RepeatableAnnotation>> repeaters;

    public AnnotationExtractor() { super(null, null, null); }
    public AnnotationExtractor(String matcher) { super(null, matcher, null); }
    public AnnotationExtractor(String matcher, Predicate<ClassReader> skip) { super(null, matcher, skip); }
    public AnnotationExtractor(Predicate<ClassReader> skip) { super(null, null, skip); }
    public AnnotationExtractor(Class<?> clazz) { super(clazz, null, null); }
    public AnnotationExtractor(Class<?> clazz, String matcher) { super(clazz, matcher, null); }
    public AnnotationExtractor(Class<?> clazz, String matcher, Predicate<ClassReader> skip) { super(clazz, matcher, skip); }
    public AnnotationExtractor(Class<?> clazz, Predicate<ClassReader> skip) { super(clazz, null, skip); }

    protected <C, A extends Annotation> void insert(Class<C> clazz, A annotation, Field isField, Method isMethod) {
        if (annotation instanceof Repeatable rep && clazz.isAnnotation()) {
            var repeatable = (Class<? extends Annotation>) clazz;
            var repeater = rep.value();
            var yielder = MethodAccess.of(repeater, repeatable.arrayType(), "value");
            if (yielder != null) {
                repeaters.computeIfAbsent(repeatable, k -> new HashMap<>());
                repeaters.get(repeatable).put(repeater, new RepeatableAnnotation(repeatable, repeater, (Function<Object, Object[]>) (Object) yielder));
            }
        }
        annotations.computeIfAbsent(annotation.annotationType(), k -> new HashMap<>());
        var cah = annotations.get(annotation.annotationType());
        ClassAnnotations<C, A> i = (ClassAnnotations<C, A>) cah.get(clazz);
        if (i == null)
            cah.put(clazz, new ClassAnnotations<>(clazz, annotation, isField, isMethod));
        else
            i.setAuto(annotation, isField, isMethod);
    }

    @Override
    public <T extends ClassExtractor> T extract(Class<?> clazz, String matcher, Predicate<ClassReader> skip, boolean clear) {
        if (annotations == null)
            annotations = new HashMap<>();
        if (repeaters == null)
            repeaters = new HashMap<>();
        super.extract(clazz, matcher, skip, clear);
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
    public <A extends Annotation> void runOnFields(Class<A> type, BiConsumer<FieldAccess<?, ?>, A> run) { runAnnotations(type, null, run, null); }
    public <A extends Annotation> void runOnMethods(Class<A> type, BiConsumer<MethodAccess<?, ?>, A> run) { runAnnotations(type, null, null, run); }
    public <A extends Annotation> void runAnnotations(Class<A> type, BiConsumer<Class<?>, A> runOnClasses, BiConsumer<FieldAccess<?, ?>, A> runOnFields, BiConsumer<MethodAccess<?, ?>, A> runOnMethods) {
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
                        if (runOnClasses != null)
                            e.getValue().classLevelAnnotation().ifPresent(
                                ca->{
                                    for (var a : er.getValue().getter.apply(ca))
                                        runOnClasses.accept(e.getKey(), (A)a);
                                }
                            );
                        if (runOnFields != null)
                            for (var f : e.getValue().fieldAnnotations().entrySet())
                                for (var a : er.getValue().getter.apply(f.getValue()))
                                    runOnFields.accept(f.getKey(), (A)a);
                        if (runOnMethods != null)
                            for (var m : e.getValue().methodAnnotations().entrySet())
                                for (var a : er.getValue().getter.apply(m.getValue()))
                                    runOnMethods.accept(m.getKey(), (A)a);
                    }

            }
    }
}
