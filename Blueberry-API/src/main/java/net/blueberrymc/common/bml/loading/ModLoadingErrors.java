package net.blueberrymc.common.bml.loading;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.DeprecatedReason;
import net.blueberrymc.common.event.mod.ModLoadingErrorAddEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ModLoadingErrors {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<ModLoadingError> ERRORS = Collections.synchronizedList(new ArrayList<>());

    /**
     * @deprecated Use {@link ModLoadingErrorAddEvent} instead
     */
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use ModLoadingErrorAddEvent instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Nullable
    public static Consumer<ModLoadingError> hook = null;

    public static void clear() {
        ERRORS.clear();
    }

    @Contract(pure = true)
    @NotNull
    public static List<ModLoadingError> getErrors() {
        return ERRORS;
    }

    @Contract(pure = true)
    public static boolean hasErrors() {
        for (ModLoadingError error : getErrors()) {
            if (!error.isWarning) return true;
        }
        return false;
    }

    public static void add(@NotNull("modLoadingError") ModLoadingError error) {
        Preconditions.checkNotNull(error, "modLoadingError cannot be null");
        new ModLoadingErrorAddEvent(error).callEvent();
        if (error.isWarning) {
            LOGGER.warn("", error.throwable);
        } else {
            LOGGER.error("", error.throwable);
        }
        ERRORS.add(error);
    }

    public static boolean hasErrorOrWarning() {
        return !ERRORS.isEmpty();
    }
}
