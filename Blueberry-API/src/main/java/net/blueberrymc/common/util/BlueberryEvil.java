package net.blueberrymc.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlueberryEvil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static byte@NotNull[] convert(byte@NotNull [] b) {
        return convert(null, b);
    }

    public static byte@NotNull[] convert(@Nullable String className, byte@NotNull [] b) {
        return b;
        /*
        ClassReader cr = new ClassReader(b);
        ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
        }, 0);
        return cw.toByteArray();
        */
    }
}
