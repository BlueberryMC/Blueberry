package net.blueberrymc.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BlueberryEvil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static byte@NotNull[] convert(byte@NotNull [] b) {
        return convert(null, b);
    }

    public static byte@NotNull[] convert(@Nullable String className, byte@NotNull [] b) {
        ClassReader cr = new ClassReader(b);
        ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            @NotNull
            @Override
            public FieldVisitor visitField(int access, @NotNull String name, @NotNull String descriptor, @Nullable String signature, @Nullable Object value) {
                // https://bugs.openjdk.java.net/browse/JDK-8145051
                String newName = name.endsWith("$this") && name.contains(".") ? "_____this_____" : name;
                return super.visitField(access, newName, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        // https://bugs.openjdk.java.net/browse/JDK-8145051
                        String newName = !name.equals("$this") && name.endsWith("$this") && name.contains(".") ? "_____this_____" : name;
                        super.visitLocalVariable(newName, descriptor, signature, start, end, index);
                    }
                };
            }
        }, 0);
        return cw.toByteArray();
    }
}
