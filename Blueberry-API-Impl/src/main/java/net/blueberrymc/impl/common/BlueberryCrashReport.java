package net.blueberrymc.impl.common;

import net.blueberrymc.util.Reflected;
import net.minecraft.CrashReport;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record BlueberryCrashReport(@NotNull CrashReport handle) implements net.blueberrymc.common.CrashReport {
    @Reflected
    @Contract("_, _ -> new")
    @NotNull
    static BlueberryCrashReport forThrowable(@NotNull Throwable throwable, @NotNull String message) {
        return new BlueberryCrashReport(CrashReport.forThrowable(throwable, message));
    }

    @Override
    public @NotNull String getTitle() {
        return Objects.requireNonNull(handle.getTitle(), "handle.getTitle()");
    }

    @Override
    public @NotNull String getDetails() {
        return handle.getDetails();
    }

    @Override
    public @NotNull Throwable getException() {
        return Objects.requireNonNull(handle.getException(), "handle.getException()");
    }

    @Override
    public @NotNull String getEntireReport() {
        return handle.getFriendlyReport();
    }
}
