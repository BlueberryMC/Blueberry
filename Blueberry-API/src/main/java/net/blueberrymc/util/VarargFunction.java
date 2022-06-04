package net.blueberrymc.util;

import java.util.function.Function;

@FunctionalInterface
public interface VarargFunction<T, R> extends Function<T[], R> {
    @SuppressWarnings("unchecked")
    @Override
    R apply(T... ts);
}
