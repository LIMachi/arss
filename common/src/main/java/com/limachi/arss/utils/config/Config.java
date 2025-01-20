package com.limachi.arss.utils.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <pre>
 * the type and default value of this config is extracted from the field
 *
 * all of those values are facultative and in string format (except for list which is an array of strings and whiteList/reload which are booleans)
 * min -> string representation of minimal value of a range (will be ignored if the field is not instanceof Compare)
 * max -> string representation of maximal value of a range (will be ignored if the field is not instanceof Compare)
 * list -> an array of string representation of values that this field can accept/reject (regex)
 * whiteList -> tell if 'list' should be used as a white list (as opposed to the default of black list)
 * cmt -> a string comment (will be ignored if null or empty)
 * path -> override the path of this variable with this path, helps readability (by default extracted from the package path)
 * name -> override the name of this variable with this name, helps readability (by default the name of the variable annotated)
 * reload -> can this config be updated on reload, by default (false) you need to reload the game entirely
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {
    String min() default "";
    String max() default "";
    String[] list() default {};
    boolean whiteList() default false;
    String cmt() default "";
    String path() default "";
    String name() default "";
    boolean reload() default false;
}
