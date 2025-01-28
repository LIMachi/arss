package com.limachi.arss.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * Register this class block and set the annotated field to be a {@code RegistryObject<? extends Block>}
 * leave name to default if you want it to be generated from the class name
 * (will transform camel case names to snake case).
 * If you want to register an automatic BlockItem, use @RegisterBlockItem in the same class.
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegisterBlockEntity {
    String value() default ""; //registry name of the object
    String skip() default ""; //use this method path to make this registry optional (method should be static, of the format `boolean method(Class<?> annotation, String name)`, returning true if the object should be skipped, aka not registered)
    String[] blocks() default {}; //registry name of the blocks used to generate this entity, if not set will use the snake class name, swapping `_block_entity` for `_block`)
}
