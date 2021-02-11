package net.minecraftforge.common.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class LazyOptional<T> {
    private final NonNullSupplier<T> supplier;
    private AtomicReference<T> resolved;
    private Set<NonNullConsumer<LazyOptional<T>>> listeners = new HashSet<>();
    private boolean isValid = true;

    @NotNull private static final LazyOptional<Void> EMPTY = new LazyOptional<>(null);
    private static final Logger LOGGER = LogManager.getLogger();

    public static <T> LazyOptional<T> of(@Nullable NonNullSupplier<T> supplier) {
        return supplier == null ? empty() : new LazyOptional<>(supplier);
    }

    public static <T> LazyOptional<T> empty() {
        return EMPTY.cast();
    }

    @SuppressWarnings("unchecked")
    public <X> LazyOptional<X> cast() {
        return (LazyOptional<X>) this;
    }

    private LazyOptional(@Nullable NonNullSupplier<T> supplier) {
        this.supplier = supplier;
    }

    @Nullable
    private T getValue() {
        if (!isValid) return null;
        if (resolved != null) return resolved.get();
        if (supplier != null) {
            resolved = new AtomicReference<>(null);
            T temp = supplier.get();
            //noinspection ConstantConditions
            if (temp == null) {
                LOGGER.catching(Level.WARN, new NullPointerException("Supplier should not return null value"));
                return null;
            }
            resolved.set(temp);
            return resolved.get();
        }
        return null;
    }

    @NotNull
    private T getValueUnsafe() {
        T ret = getValue();
        if (ret == null) {
            throw new IllegalStateException("LazyOptional is empty or otherwise returned null from getValue() unexpectedly");
        }
        return ret;
    }

    public boolean isPresent() {
        return supplier != null && isValid;
    }

    public void ifPresent(@NotNull NonNullConsumer<? super T> consumer) {
        Objects.requireNonNull(consumer);
        T val = getValue();
        if (isValid && val != null) {
            consumer.accept(val);
        }
    }

    public <U> LazyOptional<U> lazyMap(@NotNull NonNullFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return isPresent() ? of(() -> mapper.apply(getValueUnsafe())) : empty();
    }

    public <U> Optional<U> map(@NotNull NonNullFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return isPresent() ? Optional.of(mapper.apply(getValueUnsafe())) : Optional.empty();
    }

    public Optional<T> filter(@NotNull NonNullPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        final T value = getValue();
        return value != null && predicate.test(value) ? Optional.of(value) : Optional.empty();
    }

    public Optional<T> resolve() {
        return isPresent() ? Optional.of(getValueUnsafe()) : Optional.empty();
    }

    public T orElse(@Nullable T other) {
        T val = getValue();
        return val != null ? val : other;
    }

    public T orElseGet(@NotNull NonNullSupplier<? extends T> other) {
        Objects.requireNonNull(other);
        T val = getValue();
        return val != null ? val : other.get();
    }

    public <X extends Throwable> T orElseThrow(@NotNull NonNullSupplier<? extends X> supplier) throws X {
        Objects.requireNonNull(supplier);
        T val = getValue();
        if (val != null)
            return val;
        throw supplier.get();
    }

    public void addListener(@NotNull NonNullConsumer<LazyOptional<T>> listener) {
        Objects.requireNonNull(listener);
        if (isPresent()) {
            this.listeners.add(listener);
        } else {
            listener.accept(this);
        }
    }

    public void invalidate() {
        if (this.isValid) {
            this.isValid = false;
            this.listeners.forEach(e -> e.accept(this));
        }
    }
}
