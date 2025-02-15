package com.limachi.arss.neoforge.utils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.OnlyIns;

import net.neoforged.fml.loading.FMLEnvironment;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;

public class CheckDistVisitor extends ClassVisitor {
    final String onlyIn = OnlyIn.class.descriptorString();
    final String onlyIns = OnlyIns.class.descriptorString();
    final String dist = Dist.class.descriptorString();
    final String mixin = Mixin.class.descriptorString();
    boolean skip = false;
    protected CheckDistVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals(onlyIn))
            return new OnlyInVisitor();
        else if (descriptor.equals(onlyIns))
            return new OnlyInsVisitor();
        else if (descriptor.equals(mixin))
            skip = true;
        return super.visitAnnotation(descriptor, visible);
    }

    protected class OnlyInVisitor extends AnnotationVisitor {
        protected OnlyInVisitor() { super(Opcodes.ASM9); }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if (descriptor.equals(dist) && name.equals("value") && FMLEnvironment.dist != Dist.valueOf(value))
                skip = true;
            super.visitEnum(name, descriptor, value);
        }
    }

    protected class OnlyInsVisitor extends AnnotationVisitor {
        protected OnlyInsVisitor() { super(Opcodes.ASM9); }

        @Override
        public AnnotationVisitor visitArray(String name) {
            if (name.equals("value"))
                return new OnlyInArrayVisitor();
            return super.visitArray(name);
        }
    }

    protected class OnlyInArrayVisitor extends AnnotationVisitor {
        protected OnlyInArrayVisitor() { super(Opcodes.ASM9); }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            if (descriptor.equals(onlyIn))
                return new OnlyInVisitor();
            return super.visitAnnotation(name, descriptor);
        }
    }

    public static boolean skipInvalidEnv(ClassReader cr) {
        var check = new CheckDistVisitor();
        cr.accept(check, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE);
        return check.skip;
    }
}
