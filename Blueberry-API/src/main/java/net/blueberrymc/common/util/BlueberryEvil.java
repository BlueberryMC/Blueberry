package net.blueberrymc.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class BlueberryEvil {
    private static final Logger LOGGER = LogManager.getLogger();

    private static byte[] convert(byte[] b) {
        ClassReader cr = new ClassReader(b);
        ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ClassVisitor(Opcodes.ASM8, cw) {
        }, 0);
        return cw.toByteArray();
    }

    public static byte[] processClass(String path, byte[] b) {
        try {
            b = convert(b);
        } catch (Exception ex) {
            LOGGER.error("Could not convert " + path);
        }
        return b;
    }
}
