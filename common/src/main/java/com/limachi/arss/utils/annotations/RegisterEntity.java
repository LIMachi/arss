package com.limachi.arss.utils.annotations;

import net.minecraft.world.entity.MobCategory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * Register this class entity and set the annotated field to be a {@code RegistryObject<EntityType<? extends Entity>>}.
 * Leave name to default if you want it to be generated from the class name
 * (will transform camel case names to snake case).
 * Don't forget to declare the entity attributes, you can do so by using @EntityAttributeBuilder.
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegisterEntity {
    String value() default ""; //registry name of the object
    String skip() default ""; //use this method path to make this registry optional (method should be static, of the format `boolean method(Class<?> annotation, String name)`, returning true if the object should be skipped, aka not registered)
    MobCategory category() default MobCategory.MISC;
    float width() default 1f; //in ratio of block
    float height() default 1f; //in ratio of block
}
