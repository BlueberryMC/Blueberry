package net.minecraftforge.common.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NonNullFunction<T, R> {
    @NotNull
    R apply(@NotNull T t);
}
