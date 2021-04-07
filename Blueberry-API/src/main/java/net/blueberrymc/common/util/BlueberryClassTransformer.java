package net.blueberrymc.common.util;

import net.minecraft.launchwrapper.IClassTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused") // invoked by launch wrapper
public class BlueberryClassTransformer implements IClassTransformer {
    @SuppressWarnings("NullableProblems")
    @Override
    @NotNull
    public byte@NotNull[] transform(@NotNull String className, @Nullable String s1, @NotNull byte@NotNull[] bytes) {
        if (className.startsWith("net.blueberrymc.common.util.BlueberryEvil")) return bytes;
        return BlueberryEvil.convert(className, bytes);
    }
}
