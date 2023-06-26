package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RefInstanceAwareField<T, R>(@NotNull RefField<T> field, @Nullable T instance) {
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
