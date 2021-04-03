package net.blueberrymc.common.util;

import net.minecraft.launchwrapper.IClassTransformer;
import org.jetbrains.annotations.NotNull;

public class BlueberryClassTransformer implements IClassTransformer {
    @Override
    public byte[] transform(@NotNull String className, String s1, byte[] bytes) {
        if (className.startsWith("net.blueberrymc.common.util.BlueberryEvil")) return bytes;
        return BlueberryEvil.convert(className, bytes);
    }
}
