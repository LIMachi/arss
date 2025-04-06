package com.limachi.arss.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * similar to StaticInit, used to do modification on registries/utils before the standard registration is run (ex: declare a new type of arg for commands, declare a new codec, etc)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreRegistries {}
