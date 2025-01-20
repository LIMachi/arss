package com.limachi.arss.neoforge;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.reflect.AnnotationExtractor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.OnlyIns;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

@net.neoforged.fml.common.Mod("arss")
public final class MainNeo {
    public MainNeo() {
        ModBase.init(new AnnotationExtractor(ModBase.class, MainNeo::skipInvalidEnv));
    }

    static class CheckDistVisitor extends ClassVisitor {
        final String onlyin = OnlyIn.class.descriptorString();
        final String onlyins = OnlyIns.class.descriptorString();
        final String dist = Dist.class.descriptorString();
        boolean skip = false;
        protected CheckDistVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (descriptor.equals(onlyin))
                return new OnlyInVisitor();
            return super.visitAnnotation(descriptor, visible);
        }

        class OnlyInVisitor extends AnnotationVisitor {
            protected OnlyInVisitor() {
                super(Opcodes.ASM9);
            }

            @Override
            public void visitEnum(String name, String descriptor, String value) {
                if (descriptor.equals(dist) && name.equals("value") && FMLEnvironment.dist != Dist.valueOf(value))
                        skip = true;
                super.visitEnum(name, descriptor, value);
            }
        }
    }

    public static boolean skipInvalidEnv(ClassReader cr) {
        var check = new CheckDistVisitor();
        cr.accept(check, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE);
        return check.skip;
    }
}
