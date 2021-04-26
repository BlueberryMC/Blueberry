package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class TypeUtil {
    @SuppressWarnings("unchecked")
    @Contract(value = "_ -> param1", pure = true)
    @Nullable
    public static <T, R extends T> R cast(@Nullable T value) {
        try {
            return (R) value;
        } catch (ClassCastException ex) {
            return null;
        }
    }
}
