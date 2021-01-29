package net.blueberrymc.common.util;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

public class LazyValue<T> {
    private final Supplier<T> supplier;
    private WeakReference<T> ref = null;

    public LazyValue(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        T value = ref != null ? ref.get() : null;
        if (value == null) ref = new WeakReference<>(value = supplier.get());
        return value;
    }
}
