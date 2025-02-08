package com.limachi.arss.utils.annotations;

import net.minecraft.server.packs.PackType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * valid method: <br> {@code public static CompletableFuture<Void> method(PreparationBarrier, ResourceManager, ProfilerFiller, ProfilerFiller, Executor, Executor) }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReloadListener {
    String value() default "";
    PackType type() default PackType.SERVER_DATA;
}
