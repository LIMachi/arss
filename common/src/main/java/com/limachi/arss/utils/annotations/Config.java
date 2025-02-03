package com.limachi.arss.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * the type and default value of this config is extracted from the field
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {
    /**
     * minimum value of this field (only works with numbers)
     * represented as string to circumvent casting issues
     * will clamp the value(s) of this field to be at least the given number
     */
    String min() default "";
    /**
     * maximum value of this field (only works with numbers)
     * represented as string to circumvent casting issues
     * will clamp the value(s) of this field to be at most the given number
     */
    String max() default "";

    /**
     * white/black list of values (works with any type)
     * if the field is of type String/String[], then the listed Strings will be treated as Regex
     * if invalid values are found, the field will not be changed by the config
     */
    String[] list() default {};

    /**
     * set to turn list to a whitelist (will be a blacklist by default)
     */
    boolean whiteList() default false;

    /**
     * commentary that will show before the default value, clamping and list commentaries
     */
    String cmt() default "";

    /**
     * set to override the default path (the path is the package + class name that contains the field by default)
     */
    String path() default "<auto>";

    /**
     * set to override the default name (by default uses the name of the field)
     */
    String name() default "";

    /**
     * can this value be reloaded at run time or should it be read only once at startup
     */
    boolean reload() default false;
}
