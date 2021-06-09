package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RefInstanceAwareField<T, R> {
    @NotNull private final RefField<T> field;
    @Nullable private final T instance;

    public RefInstanceAwareField(@NotNull RefField<T> field, @Nullable T instance) {
        this.field = field;
        this.instance = instance;
    }

    @NotNull
    public RefField<T> getField() {
        return field;
    }

    @Nullable
    public T getInstance() {
        return this.instance;
    }

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    public R get() {
        return (R) this.field.get(this.instance);
    }

    @Contract("_ -> param1")
    public R set(@Nullable R value) {
        this.field.set(this.instance, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    public R getAndSet(@Nullable R value) {
        R oldValue = (R) this.field.get(this.instance);
        this.field.set(this.instance, value);
        return oldValue;
    }
}
