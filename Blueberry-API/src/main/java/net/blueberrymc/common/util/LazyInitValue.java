package net.blueberrymc.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Similar to LazyValue, but this one just stores value without WeakRef.
 */
public class LazyInitValue<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T value = null;

    public LazyInitValue(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @NotNull
    @Override
    public T get() {
        if (value == null) value = supplier.get();
        return Objects.requireNonNull(value);
    }
}
