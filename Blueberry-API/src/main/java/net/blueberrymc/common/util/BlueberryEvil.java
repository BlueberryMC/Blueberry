package net.blueberrymc.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public class BlueberryEvil {
    private static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("NullableProblems")
    @NotNull
    public static byte@NotNull[] convert(@NotNull byte@NotNull [] b) {
        ClassReader cr = new ClassReader(b);
        ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ClassVisitor(Opcodes.ASM8, cw) {
            @NotNull
            @Override
            public FieldVisitor visitField(int access, @NotNull String name, @NotNull String descriptor, @NotNull String signature, @NotNull Object value) {
                // https://bugs.openjdk.java.net/browse/JDK-8145051
                String newName = name.endsWith("this") ? "_____this_____" : name;
                return super.visitField(access, newName, descriptor, signature, value);
            }
        }, 0);
        return cw.toByteArray();
    }
}
