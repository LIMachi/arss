package com.limachi.arss.utils;

import com.google.common.reflect.Reflection;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class StaticInitializer {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    public @interface Static {
    }

    public static void initialize() {
        Type type = Type.getType(Static.class);
        for (ModFileScanData.AnnotationData data : ModList.get().getAllScanData().stream().map(ModFileScanData::getAnnotations).flatMap(Collection::stream).filter(a-> type.equals(a.annotationType())).collect(Collectors.toList())) {
            try {
                Reflection.initialize(Class.forName(data.clazz().getClassName()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}