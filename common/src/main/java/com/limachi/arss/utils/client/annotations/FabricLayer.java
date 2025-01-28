package com.limachi.arss.utils.client.annotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * only has an effect in fabric (forge uses the json key "render_type" inside the models)
 * expected to be put on the same field as @RegisterBlock / a field of type {@code Supplier<Block>} / a method that return a {@code Collection<Supplier<Block>>} / a class that extends block
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface FabricLayer {
    enum Layers {
        SOLID("solid"),
        CUTOUT("cutout"),
        CUT_MIP("cutout_mipped"),
        TRANSLUCENT("translucent"),
        TRIPWIRE("tripwire");

        final String layer;

        Layers(String layer) { this.layer = layer; }

        @Override
        public String toString() { return layer; }

        public static Layers fromString(String string) {
            return switch (string) {
                case "solid" -> SOLID;
                case "cutout" -> CUTOUT;
                case "cutout_mipped" -> CUT_MIP;
                case "translucent" -> TRANSLUCENT;
                case "tripwire" -> TRIPWIRE;
                default -> null;
            };
        }

        @Environment(EnvType.CLIENT)
        public RenderType renderType() {
            return switch (this) {
                case SOLID -> RenderType.solid();
                case CUTOUT -> RenderType.cutout();
                case CUT_MIP -> RenderType.cutoutMipped();
                case TRANSLUCENT -> RenderType.translucent();
                case TRIPWIRE -> RenderType.tripwire();
            };
        }
    }

    public String value() default "solid";
}
