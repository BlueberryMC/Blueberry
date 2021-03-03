package net.blueberrymc.common.util;

import org.jetbrains.annotations.NotNull;

public interface ThrowableConsumer<T> {
    void accept(@NotNull T t) throws Throwable;
}
