package net.blueberrymc.common.util.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class DelegatingThrowableSupplier<T> implements ThrowableSupplier<T> {
    @Nullable private Map.Entry<T, Throwable> result;

    @NotNull
    protected abstract ThrowableSupplier<T> delegate();

    @Nullable
    @Override
    public final T get() {
        return result == null ? (result = ThrowableSupplier.super.entry()).getKey() : result.getKey();
    }

    @NotNull
    @Override
    public final Map.Entry<T, Throwable> entry() {
        return result == null ? result = ThrowableSupplier.super.entry() : result;
    }

    @Nullable
    @Override
    public final T evaluate() throws Throwable { return delegate().evaluate(); }

    public final void unregister() { removeCache(this.delegate()); }

    @NotNull
    private static final Map<ThrowableSupplier<?>, DelegatingThrowableSupplier<?>> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> DelegatingThrowableSupplier<T> getInstance(@NotNull ThrowableSupplier<T> supplier) {
        if (cache.containsKey(supplier)) return (DelegatingThrowableSupplier<T>) cache.get(supplier);
        DelegatingThrowableSupplier<T> instance = new DelegatingThrowableSupplier<T>() {
            @Override
            protected @NotNull ThrowableSupplier<T> delegate() {
                return supplier;
            }
        };
        cache.put(supplier, instance);
        return instance;
    }

    public static void removeCache(@NotNull ThrowableSupplier<?> supplier) {
        cache.remove(supplier);
    }
}
