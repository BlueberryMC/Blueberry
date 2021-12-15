package net.blueberrymc.common.util;

import net.minecraft.launchwrapper.IClassTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: unused class
public class BlueberryClassTransformer implements IClassTransformer {
    @Override
    public byte@Nullable[] transform(@NotNull String className, @NotNull String transformedName, byte@Nullable[] bytes) {
        if (bytes == null) return null; // we are not class generator
        if (className.startsWith("net.blueberrymc.common.util.BlueberryEvil")) return bytes;
        return BlueberryEvil.convert(className, bytes);
    }
}
