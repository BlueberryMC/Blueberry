package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record IntPair(int first, int second) {
    @Contract("_, _ -> new")
    @NotNull
    public static IntPair of(int first, int second) {
        return new IntPair(first, second);
    }
}
