package net.blueberrymc.impl.common;

import net.blueberrymc.util.Reflected;
import net.minecraft.CrashReport;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record BlueberryCrashReport(@NotNull CrashReport handle) implements net.blueberrymc.common.CrashReport {
    @Reflected
    @Contract("_, _ -> new")
    @NotNull
    static BlueberryCrashReport forThrowable(@NotNull Throwable throwable, @NotNull String message) {
        return new BlueberryCrashReport(CrashReport.forThrowable(throwable, message));
    }
}
