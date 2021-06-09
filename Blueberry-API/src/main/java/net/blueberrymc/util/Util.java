package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class Util {
    @Contract(value = "null, null -> null; null, _ -> param2; _, !null -> !null", pure = true)
    public static <T> T getOrDefault(@Nullable T value, @Nullable T def) {
        if (value != null) return value;
        return def;
    }

    @SafeVarargs
    @Contract(value = "null -> null", pure = true)
    public static <T> T getOrDefault(@Nullable T@Nullable... values) {
        if (values == null) return null;
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }
}
