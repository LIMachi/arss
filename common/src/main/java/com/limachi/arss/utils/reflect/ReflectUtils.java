package com.limachi.arss.utils.reflect;

import java.lang.reflect.*;

import sun.misc.Unsafe;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;

import static com.limachi.arss.utils.StringUtils.subString;

@SuppressWarnings({"unchecked", "unused"})
public class ReflectUtils {
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

    /**
     * if the given class is an interface and declares a single abstract method, treat this method has a functional interface
     */
    public static Optional<Method> functionalInterface(Class<?> clazz) {
        if (!clazz.isInterface())
            return Optional.empty();
        Optional<Method> abstractFound = Optional.empty();
        for (Method method : clazz.getDeclaredMethods())
            if (Modifier.isAbstract(method.getModifiers()))
                if (abstractFound.isPresent())
                    return Optional.empty();
                else
                    abstractFound = Optional.of(method);
        return abstractFound;
    }

    /**
     * test if this object is an instance of a functional interface, and if true, return the runnable method
     */
    public static Optional<Method> functionalInterface(Object obj) {
        if (obj instanceof Class<?> c)
            return functionalInterface(c);
        Optional<Method> found = Optional.empty();
        for (Class<?> i : obj.getClass().getInterfaces()) {
            Optional<Method> abstractFound = functionalInterface(i);
            if (abstractFound.isPresent()) {
                if (found.isPresent())
                    return Optional.empty();
                found = abstractFound;
            }
        }
        return found;
    }

    /**
     * return a descriptor of the object, applying a few assumptions: <br>
     * Parameters and Type objects are transformed to Class object.<br>
     * if the object is a method or functional interface, prepend the type declaring it.<br>
     * if the class or object is a functional interface, see it as a lambda and return the underlying description.<br>
     * (ex: `(Supplier)()->0` will return "Ljava/util/function/Supplier; ()Ljava/lang/Object;" instead of {@code  "L<class>$$Lambda.<address>;"}
     */
    public static String opinionatedDescriptor(Object obj) { return descriptor(obj, true, true); }

    /**
     * return a descriptor of the object, no assumptions made (see {@link ReflectUtils#opinionatedDescriptor})
     */
    public static String trueDescriptor(Object obj) { return descriptor(obj, false, false); }

    /**
     * same as {@link ReflectUtils#opinionatedDescriptor} but will return the objects descriptors concatenated inside parenthesis, without the prepending the class if the object is a functional interface or method
     */
    public static String opinionatedTupleDescriptor(Object ... objs) { return tupleDescriptor(true, objs); }

    /**
     * same as {@link ReflectUtils#trueDescriptor} but will return the objects descriptors concatenated inside parenthesis
     */
    public static String trueTupleDescriptor(Object ... objs) { return tupleDescriptor(false, objs); }

    private static String tupleDescriptor(boolean op, Object ... objs) {
        StringBuilder out = new StringBuilder();
        out.append('(');
        for (var obj : objs)
            out.append(descriptor(obj, op, false));
        return out.append(')').toString();
    }

    private static String descriptor(Object obj, boolean op, boolean prependClass) {
        if (obj == null)
            return op ? "V" : "null";
        if (op) {
            if (obj instanceof Parameter p)
                obj = p.getType();
            if (obj instanceof Type t)
                try { obj = Class.forName(t.getTypeName()); } catch (ClassNotFoundException ignore) {}
            if (obj instanceof Field f)
                obj = f.getType();
        }
        if (obj instanceof Class<?> c)
            return op ? functionalInterface(c).map(ReflectUtils::opinionatedDescriptor).orElseGet(c::descriptorString) : c.descriptorString();
        if (obj instanceof Method m)
            return (prependClass ? trueDescriptor(m.getDeclaringClass()) + " " : "") + tupleDescriptor(op, (Object[])m.getParameters()) + descriptor(m.getReturnType(), op, false);
        else {
            final Class<?> c = obj.getClass();
            return op ? functionalInterface(obj).map(ReflectUtils::opinionatedDescriptor).orElseGet(() -> opinionatedDescriptor(c)) : trueDescriptor(c);
        }
    }

    public enum AccessWidening {
        READ, //classes/fields -> public, method -> public (and final if private)
        WRITE, //classes -> public -final, field -> -final, method -> protected -final
        READ_WRITE, //all: public -final
        READ_TRANSITIVE,
        WRITE_TRANSITIVE,
        READ_WRITE_TRANSITIVE;
        private final String[] NONE = new String[0];
        private final String[] ACCESSIBLE = new String[]{"accessible"};
        private final String[] ACCESSIBLE_TRANSITIVE = new String[]{"transitive-accessible"};
        private final String[] EXTENDABLE = new String[]{"extendable"};
        private final String[] EXTENDABLE_TRANSITIVE = new String[]{"transitive-extendable"};
        private final String[] ACCESSIBLE_EXTENDABLE = new String[]{"accessible", "extendable"};
        private final String[] ACCESSIBLE_EXTENDABLE_TRANSITIVE = new String[]{"transitive-accessible", "transitive-extendable"};
        private final String[] MUTABLE = new String[]{"mutable"};
        private final String[] MUTABLE_TRANSITIVE = new String[]{"transitive-mutable"};
        private final String[] ACCESSIBLE_MUTABLE = new String[]{"accessible", "mutable"};
        private final String[] ACCESSIBLE_MUTABLE_TRANSITIVE = new String[]{"transitive-accessible", "transitive-mutable"};

        public String[] getClassWideners(int modifiers) {
            return switch (this) {
                case READ -> Modifier.isPublic(modifiers) ? NONE : ACCESSIBLE;
                case READ_TRANSITIVE -> Modifier.isPublic(modifiers) ? NONE : ACCESSIBLE_TRANSITIVE;
                case WRITE, READ_WRITE -> Modifier.isPublic(modifiers) && !Modifier.isFinal(modifiers) ? NONE : EXTENDABLE;
                case WRITE_TRANSITIVE, READ_WRITE_TRANSITIVE -> Modifier.isPublic(modifiers) && !Modifier.isFinal(modifiers) ? NONE : EXTENDABLE_TRANSITIVE;
            };
        }

        public String[] getFieldWideners(int modifiers) {
            return switch (this) {
                case READ -> Modifier.isPublic(modifiers) ? NONE : ACCESSIBLE;
                case READ_TRANSITIVE -> Modifier.isPublic( modifiers) ? NONE : ACCESSIBLE_TRANSITIVE;
                case WRITE, READ_WRITE -> Modifier.isPublic(modifiers) ? Modifier.isFinal(modifiers) ? MUTABLE : NONE : Modifier.isFinal(modifiers) ? ACCESSIBLE_MUTABLE : ACCESSIBLE;
                case WRITE_TRANSITIVE, READ_WRITE_TRANSITIVE -> Modifier.isPublic(modifiers) ? Modifier.isFinal(modifiers) ? MUTABLE_TRANSITIVE : NONE : Modifier.isFinal(modifiers) ? ACCESSIBLE_MUTABLE_TRANSITIVE : ACCESSIBLE_TRANSITIVE;
            };
        }

        public String[] getMethodWideners(int modifiers) {
            return switch (this) {
                case READ -> Modifier.isPublic(modifiers) ? NONE : ACCESSIBLE;
                case READ_TRANSITIVE -> Modifier.isPublic(modifiers) ? NONE : ACCESSIBLE_TRANSITIVE;
                case WRITE -> Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers) ? EXTENDABLE : NONE;
                case WRITE_TRANSITIVE -> Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers) ? EXTENDABLE_TRANSITIVE : NONE;
                case READ_WRITE -> {
                    boolean f = Modifier.isFinal(modifiers);
                    if (Modifier.isPublic(modifiers))
                        yield f ? EXTENDABLE : NONE;
                    else
                        yield f || Modifier.isPrivate(modifiers) ? ACCESSIBLE_EXTENDABLE : ACCESSIBLE;
                }
                case READ_WRITE_TRANSITIVE -> {
                    boolean f = Modifier.isFinal(modifiers);
                    if (Modifier.isPublic(modifiers))
                        yield f ? EXTENDABLE_TRANSITIVE : NONE;
                    else
                        yield f || Modifier.isPrivate(modifiers) ? ACCESSIBLE_EXTENDABLE_TRANSITIVE : ACCESSIBLE_TRANSITIVE;
                }
            };
        }
    }

    /**
     * generate a generic access widener for the given class/field/method
     */
    public static String accessWidener(Object obj) {
        if (obj instanceof Class<?> c)
            return "<access> class " + subString(trueDescriptor(c), 1, -1);
        else if (obj instanceof Field f)
            return "<access> field " + subString(trueDescriptor(f.getDeclaringClass()), 1, -1) + ' ' + f.getName() + ' ' + descriptor(f, true, false);
        else if (obj instanceof Method m)
            return "<access> method " + subString(trueDescriptor(m.getDeclaringClass()), 1, -1) + ' ' + m.getName() + ' ' + descriptor(m, true, false);
        return "";
    }

    public static void accessWidener(StringBuilder builder, Object obj, AccessWidening widener, boolean withComment) {
        boolean ran = false;
        if (obj instanceof Class<?> c)
            for (String pre : widener.getClassWideners(c.getModifiers())) {
                if (withComment) {
                    builder.append('#').append(c.getSimpleName()).append('\n');
                    withComment = false;
                }
                builder.append(pre).append(" class ").append(subString(trueDescriptor(c), 1, -1)).append('\n');
                ran = true;
            }
        if (obj instanceof Field f)
            for (String pre : widener.getFieldWideners(f.getModifiers())) {
                if (withComment) {
                    builder.append('#').append(f.getDeclaringClass().getSimpleName()).append('#').append(f.getName()).append('\n');
                    withComment = false;
                }
                builder.append(pre).append(" field ").append(subString(trueDescriptor(f.getDeclaringClass()), 1, -1)).append(' ').append(f.getName()).append(' ').append(descriptor(f, true, false)).append('\n');
                ran = true;
            }
        if (obj instanceof Method m)
            for (String pre : widener.getMethodWideners(m.getModifiers())) {
                if (withComment) {
                    builder.append('#').append(m.getDeclaringClass().getSimpleName()).append('#').append(m.getName()).append("(...)").append('\n');
                    withComment = false;
                }
                builder.append(pre).append(" method ").append(subString(trueDescriptor(m.getDeclaringClass()), 1, -1)).append(' ').append(m.getName()).append(' ').append(descriptor(m, true, false)).append('\n');
                ran = true;
            }
        if (ran)
            builder.append('\n');
    }

    public static String declaredAccessWidener(Class<?> clazz, AccessWidening widener, boolean withComment, String ... names) {
        if (names.length == 0)
            return "";
        StringBuilder out = new StringBuilder();
        var n = new HashSet<>(Arrays.asList(names));
        if (n.contains("this"))
            accessWidener(out, clazz, widener, withComment);
        for (Field f : clazz.getDeclaredFields())
            if (n.contains(f.getName()))
                accessWidener(out, f, widener, withComment);
        for (Method m : clazz.getDeclaredMethods())
            if (n.contains(m.getName()))
                accessWidener(out, m, widener, withComment);
        return out.toString();
    }

    /**
     * generate a list of access wideners for the class and all it's fields and methods
     */
    public static String fullClassWidener(Class<?> clazz) {
        StringBuilder out = new StringBuilder();
        accessWidener(out, clazz, AccessWidening.READ_WRITE, true);
        for (Field field : clazz.getDeclaredFields())
            accessWidener(out, field, AccessWidening.READ_WRITE, true);
        for (Method method : clazz.getDeclaredMethods())
            accessWidener(out, method, AccessWidening.READ_WRITE, true);
        return out.toString();
    }
}
