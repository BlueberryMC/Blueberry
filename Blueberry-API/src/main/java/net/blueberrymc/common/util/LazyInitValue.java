package net.blueberrymc.common.util;

import java.util.function.Supplier;

public class LazyInitValue<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T value = null;

    public LazyInitValue(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (value == null) value = supplier.get();
        return value;
    }
}
