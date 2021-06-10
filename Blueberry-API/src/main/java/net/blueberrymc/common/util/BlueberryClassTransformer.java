package net.blueberrymc.common.util;

import net.minecraft.launchwrapper.IClassTransformer;
import org.jetbrains.annotations.NotNull;

public class BlueberryClassTransformer implements IClassTransformer {
    @Override
    public byte@NotNull[] transform(@NotNull String className, @NotNull String transformedName, byte@NotNull[] bytes) {
        if (className.startsWith("net.blueberrymc.common.util.BlueberryEvil")) return bytes;
        return BlueberryEvil.convert(className, bytes);
    }
}
