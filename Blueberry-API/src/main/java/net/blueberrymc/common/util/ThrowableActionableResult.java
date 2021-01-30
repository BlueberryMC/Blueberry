package net.blueberrymc.common.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.blueberrymc.common.util.function.DelegatingThrowableSupplier;
import net.blueberrymc.common.util.function.ThrowableSupplier;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ThrowableActionableResult<T> extends ActionableResult<T> {
    @SuppressWarnings("rawtypes")
    @NotNull
    private static final ThrowableActionableResult EMPTY = new ThrowableActionableResult();

    private final Throwable throwable;

    protected ThrowableActionableResult() {
        super();
        this.throwable = null;
    }

    protected ThrowableActionableResult(@Nullable T value) {
        super(value);
        this.throwable = null;
    }

    protected ThrowableActionableResult(@Nullable T value, @Nullable Throwable throwable) {
        super(value);
        this.throwable = throwable;
    }

    /**
     * @deprecated less efficient method, consumes more resources than {@link #of(ThrowableSupplier)}
     */
    @Deprecated
    protected ThrowableActionableResult(@NotNull ThrowableSupplier<T> supplier) {
        this(DelegatingThrowableSupplier.getInstance(supplier).entry().getKey(), DelegatingThrowableSupplier.getInstance(supplier).entry().getValue());
        DelegatingThrowableSupplier.removeCache(supplier); // remove from cache
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> ThrowableActionableResult<T> empty() { return EMPTY; }

    @NotNull
    public static <T> ThrowableActionableResult<T> of(@NotNull ThrowableSupplier<T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier cannot be null");
        Map.Entry<T, Throwable> entry = supplier.entry();
        return new ThrowableActionableResult<>(entry.getKey(), entry.getValue());
    }

    @NotNull
    public static <T> ThrowableActionableResult<T> of(@Nullable T value, @Nullable Throwable throwable) {
        return new ThrowableActionableResult<>(value, throwable);
    }

    @NotNull
    public static <T> ThrowableActionableResult<T> of(@NotNull T value) {
        Preconditions.checkNotNull(value, "value cannot be null (Use #ofNullable or #of(T, Throwable))");
        return new ThrowableActionableResult<>(value);
    }

    @NotNull
    public static <T> ThrowableActionableResult<T> of() { return empty(); }

    @NotNull
    public static <T> ThrowableActionableResult<T> ofNullable(@Nullable T value) {
        return value == null ? empty() : of(value);
    }

    @NotNull
    public static <V> ThrowableActionableResult<V> of(@NotNull Supplier<V> supplier) { return of(supplier.get()); }

    @NotNull
    public static <V> ThrowableActionableResult<V> ofNullable(@NotNull Supplier<V> supplier) { return ofNullable(supplier.get()); }

    @Contract(pure = true)
    @NotNull
    public static <V> ThrowableActionableResult<V> success(V value) { return ofNullable(value); }

    @Contract(pure = true)
    @NotNull
    public static <V> ThrowableActionableResult<V> failure(@NotNull Throwable throwable) {
        Preconditions.checkNotNull(throwable, "throwable cannot be null");
        return new ThrowableActionableResult<>(null, throwable);
    }

    /**
     * Throws exception (sneaky) if the exception was thrown when evaluating the result.
     */
    @NotNull
    public final ThrowableActionableResult<T> throwIfAny() {
        if (throwable != null) throw new RuntimeException(throwable);
        return this;
    }

    @Override
    public @NotNull <U> ThrowableActionableResult<U> map(@NotNull Function<? super T, ? extends U> function) {
        return new ThrowableActionableResult<>(super.map(function).value, throwable);
    }

    @Override
    public @NotNull <U> ThrowableActionableResult<U> flatMap(@NotNull Function<? super T, ActionableResult<U>> function) {
        return new ThrowableActionableResult<>(super.flatMap(function).value, throwable);
    }

    @Override
    public @NotNull ThrowableActionableResult<T> filter(@NotNull Predicate<? super T> predicate) {
        return new ThrowableActionableResult<>(super.filter(predicate).value, throwable);
    }

    @Override
    public @NotNull ThrowableActionableResult<T> then(@NotNull Consumer<? super T> action) {
        super.then(action);
        return this;
    }

    @Override
    public @NotNull ThrowableActionableResult<T> ifPresent(@NotNull Consumer<? super T> action) {
        super.ifPresent(action);
        return this;
    }

    @NotNull
    public ThrowableActionableResult<T> ifNotPresent(@NotNull Runnable action) {
        super.ifNotPresent(action);
        return this;
    }

    @Override
    public @NotNull <U> ThrowableActionableResult<U> swap(@NotNull Supplier<U> supplier) { return of(supplier.get(), throwable); }

    @Override
    public @NotNull <U> ThrowableActionableResult<U> swap(@Nullable U value) { return of(value, throwable); }

    /**
     * Gets throwable for this result. May be null.
     * @return a throwable
     */
    @Nullable
    public Throwable getThrowable() { return throwable; }
}
