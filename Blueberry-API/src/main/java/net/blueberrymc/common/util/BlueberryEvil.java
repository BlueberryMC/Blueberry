package net.blueberrymc.common.util;

import net.blueberrymc.server.main.ServerMain;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Objects;

public class BlueberryEvil {
    private static final String SIDE = (String) Objects.requireNonNull(ServerMain.blackboard.get("side"), "side is null");

    public static byte@NotNull[] convert(byte@NotNull [] b) throws WrongSideException {
        return convert(null, b);
    }

    /**
     * Converts the class with current environment.
     * @param className The name of the class.
     * @param b The class bytes.
     * @return The converted class bytes.
     * @throws WrongSideException If the class is not available on the current side.
     */
    public static byte@NotNull[] convert(@Nullable String className, byte@NotNull [] b) throws WrongSideException {
        ClassReader cr = new ClassReader(b);
        AnnotationRecorder.AnnotationVisitResult result = AnnotationRecorder.visitAnnotations(cr);
        if (isServer() && isClientOnly(result)) {
            throw new WrongSideException("Attempted to convert client-only class " + className + " on the server-side");
        }
        ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                AnnotationRecorder.AnnotatedMember member = result.findMember(AnnotationRecorder.Type.METHOD, name, descriptor);
                if (isServer() && isClientOnly(member)) {
                    return null;
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);
        return cw.toByteArray();
    }

    private static boolean isServer() {
        return !SIDE.equals("CLIENT");
    }

    @Contract(value = "null -> false", pure = true)
    private static boolean isClientOnly(@Nullable AnnotationRecorder.IAnnotatedMember member) {
        if (member == null || !member.isAnnotated("Lnet/blueberrymc/common/SideOnly;")) {
            return false;
        }
        AnnotationRecorder.AnnotationData data = Objects.requireNonNull(member.findAnnotation("Lnet/blueberrymc/common/SideOnly;"));
        AnnotationRecorder.AnnotationParameter param = Objects.requireNonNull(data.findParameter("value"));
        return param.value() instanceof AnnotationRecorder.AnnotationParameterEnum e && e.value().equals("CLIENT");
    }

    public static class WrongSideException extends RuntimeException {
        public WrongSideException(String message) {
            super(message);
        }
    }
}
