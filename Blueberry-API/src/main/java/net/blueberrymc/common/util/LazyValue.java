package net.blueberrymc.common.util;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Similar to LazyInitValue, but this one uses WeakReference to store a value.
 */
public class LazyValue<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private WeakReference<T> ref = null;

    public LazyValue(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @NotNull
    @Override
    public T get() {
        T value = ref != null ? ref.get() : null;
        if (value == null) ref = new WeakReference<>(value = supplier.get());
        return Objects.requireNonNull(value);
    }
}
