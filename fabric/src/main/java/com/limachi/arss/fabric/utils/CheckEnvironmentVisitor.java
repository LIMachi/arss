package com.limachi.arss.fabric.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;

public class CheckEnvironmentVisitor extends ClassVisitor {
    final String onlyIn = Environment.class.descriptorString();
    final String dist = EnvType.class.descriptorString();
    final String mixin = Mixin.class.descriptorString();
    boolean skip = false;

    protected CheckEnvironmentVisitor() { super(Opcodes.ASM9); }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals(onlyIn))
            return new OnlyInVisitor();
        else if (descriptor.equals(mixin))
            skip = true;
        return super.visitAnnotation(descriptor, visible);
    }

    protected class OnlyInVisitor extends AnnotationVisitor {
        protected OnlyInVisitor() { super(Opcodes.ASM9); }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if (descriptor.equals(dist) && name.equals("value") && FabricLoader.getInstance().getEnvironmentType() != EnvType.valueOf(value))
                skip = true;
            super.visitEnum(name, descriptor, value);
        }
    }

    public static boolean skipInvalidEnv(ClassReader cr) {
        var check = new CheckEnvironmentVisitor();
        cr.accept(check, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE);
        return check.skip;
    }
}
