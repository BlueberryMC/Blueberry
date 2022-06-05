package net.blueberrymc.common;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface CrashReport {
    @Contract(value = "_, _ -> new", pure = true)
    @NotNull
    static CrashReport forThrowable(@NotNull Throwable throwable, @NotNull String message) {
        return (CrashReport) ImplGetter.byMethod("forThrowable", Throwable.class, String.class).apply(throwable, message);
    }
}
