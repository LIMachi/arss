package com.limachi.arss.utils.client.annotations;

import com.limachi.arss.utils.client.ClientStage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StaticInitClient {
    ClientStage value() default ClientStage.FIRST;
    boolean before() default false;
    String skip() default ""; //only for ElementType.METHOD (as static block will always be called). Work like skip for registries (will call a static method to test if this should be executed, the name provided will be the name of the annotated method)
}