package net.blueberrymc.common.bml.loading;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ModLoadingErrors {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<ModLoadingError> errors = Collections.synchronizedList(new ArrayList<>());
    @Nullable public static Consumer<ModLoadingError> hook = null;

    public static void clear() {
        errors.clear();
    }

    @Contract(pure = true)
    @NotNull
    public static List<ModLoadingError> getErrors() {
        return errors;
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
        if (errors.isEmpty() && hook != null) hook.accept(error);
        LOGGER.error(error.throwable);
        errors.add(error);
    }
}
