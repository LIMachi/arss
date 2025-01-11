package com.limachi.arss.utils.annotations;

import com.limachi.arss.utils.Stage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StaticInit {
    Stage value() default Stage.FIRST;
    java.lang.String skip() default ""; //only for ElementType.METHOD (as static block will always be called). Work like skip for registries (will call a static method to test if this should be executed, the name provided will be the name of the annotated method)
}