package net.blueberrymc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TinyTime {
    private static final Logger LOGGER = LogManager.getLogger();

    @Contract
    public static <T> T measureTime(@NotNull String name, @NotNull Supplier<T> supplier) {
        if (!LOGGER.isDebugEnabled()) return supplier.get();
        long start = System.currentTimeMillis();
        T value = supplier.get();
        long time = System.currentTimeMillis() - start;
        LOGGER.debug("'{}' took {} ms", name, time);
        return value;
    }
}
