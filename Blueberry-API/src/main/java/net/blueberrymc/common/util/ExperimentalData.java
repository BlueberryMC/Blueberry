package net.blueberrymc.common.util;

import net.blueberrymc.common.Experimental;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedElement;

public record ExperimentalData(boolean experimental) {
    public static final ExperimentalData NOT_EXPERIMENTAL = new ExperimentalData(false);

    @Contract("_ -> new")
    @NotNull
    public static ExperimentalData of(@NotNull AnnotatedElement element) {
        // Unfortunately, ApiStatus$Experimental is not visible from reflection.
        boolean actuallyExperimental = element.isAnnotationPresent(ApiStatus.Experimental.class) || element.isAnnotationPresent(Experimental.class);
        return new ExperimentalData(actuallyExperimental);
    }
}
