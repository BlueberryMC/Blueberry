package net.blueberrymc.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Function;

/**
 * An iterator that can transform the type of the values it returns.
 * @param handle The underlying iterator.
 * @param func The function to transform the values.
 * @param <T> The type of the pre-transform values.
 * @param <R> The type of the transformed values.
 */
public record RetypedIterator<T, R>(@NotNull Iterator<T> handle, @NotNull Function<T, R> func) implements Iterator<R> {
    @Override
    public boolean hasNext() {
        return handle.hasNext();
    }

    @Override
    public R next() {
        return func.apply(handle.next());
    }

    @Override
    public void remove() {
        handle.remove();
    }
}
