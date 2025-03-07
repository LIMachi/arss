package com.limachi.arss.utils.client.annotations;

import com.limachi.arss.utils.client.ClientEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Environment(EnvType.CLIENT)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterClientEventListener {
    ClientEvents value();
}
