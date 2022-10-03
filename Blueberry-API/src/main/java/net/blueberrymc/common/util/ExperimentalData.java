package net.blueberrymc.common.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedElement;

public record ExperimentalData(boolean experimental) {
    public static final ExperimentalData NOT_EXPERIMENTAL = new ExperimentalData(false);

    @Contract("_ -> new")
    @NotNull
    public static ExperimentalData of(@NotNull AnnotatedElement element) {
        boolean actuallyExperimental = element.isAnnotationPresent(ApiStatus.Experimental.class);
        return new ExperimentalData(actuallyExperimental);
    }
}
