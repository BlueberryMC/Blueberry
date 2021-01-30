package net.blueberrymc.common.util.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowableSupplier<T> extends Supplier<T> {
    /**
     * Gets a result of this supplier. May be null if exception was thrown.
     * @return a result
     */
    @Nullable
    @Override
    default T get() {
        try {
            return this.evaluate();
        } catch (Throwable throwable) {
            return null;
        }
    }

    /**
     * Gets a result of this supplier as entry. Key <b>may</b> be null if exception was thrown, and the value will be null
     * if it was run successfully.
     * @return a result as entry
     */
    @NotNull
    default Map.Entry<T, Throwable> entry() {
        try {
            return new AbstractMap.SimpleImmutableEntry<>(this.evaluate(), null);
        } catch (Throwable throwable) {
            return new AbstractMap.SimpleImmutableEntry<>(null, throwable);
        }
    }

    /**
     * Gets a result.
     * @return a result
     */
    T evaluate() throws Throwable;
}
