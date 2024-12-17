package com.limachi.arss.utils.annotations;

import com.mojang.brigadier.arguments.StringArgumentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CmdArg {
    String value();
    String min() default "";
    String max() default "";
    StringArgumentType.StringType matcher() default StringArgumentType.StringType.QUOTABLE_PHRASE;
}
