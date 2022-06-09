package net.blueberrymc.common.util.reflect;

import net.blueberrymc.common.DeprecatedReason;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RefInstanceAwareField<T, R>(@NotNull RefField<T> field, @Nullable T instance) {
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #field() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @NotNull
    public RefField<T> getField() {
        return field;
    }

    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #instance() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
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
