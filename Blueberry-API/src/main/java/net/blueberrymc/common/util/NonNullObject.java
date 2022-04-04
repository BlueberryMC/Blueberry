package net.blueberrymc.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NonNullObject<T> {
    private T object;

    public NonNullObject(@NotNull T object) {
        this.object = Objects.requireNonNull(object);
    }

    /**
     * Gets the object.
     * @return the object
     */
    @NotNull
    public T get() {
        return Objects.requireNonNull(this.object, "object is null");
    }

    /**
     * Sets the object.
     * @param object the object
     */
    public void set(@NotNull T object) {
        this.object = Objects.requireNonNull(object, "object cannot be null");
    }
}
