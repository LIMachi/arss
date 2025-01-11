package com.limachi.arss.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * the type and default value of this config is extracted from the field
 *
 * all of those values are facultative and in string format (except for valid which is an array of strings)
 * min -> string representation of minimal value of a range (will be ignored if the field is not instanceof Compare)
 * max -> string representation of maximal value of a range (will be ignored if the field is not instanceof Compare)
 * valid -> an array of string representation of valid values that this field can accept (regex)
 * cmt -> a string comment (will be ignored if null or empty)
 * path -> override the path of this variable with this path, helps readability
 * name -> override the name of this variable with this name, helps readability
 *
 * there is also 2 values that will define when and how this config will be used
 * side -> in which file this config should be stored, defaults to COMMON
 * reload -> can this config be updated on reload, by default (false) you need to reload the game entirely
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {
    String min() default "";
    String max() default "";
    String[] valid() default {};
    String cmt() default "";
    String path() default "";
    String name() default "";
//    ModConfig.Type side() default ModConfig.Type.COMMON;
    boolean reload() default false;
}