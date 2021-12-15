package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeakList<T> extends AbstractList<T> {
    private final List<WeakReference<T>> refs;

    public WeakList() {
        this(true);
    }

    public WeakList(boolean synchronizedList) {
        if (synchronizedList) {
            this.refs = Collections.synchronizedList(new ArrayList<>());
        } else {
            this.refs = new ArrayList<>();
        }
    }

    /**
     * Removes all unreachable objects.
     */
    @Contract
    @NotNull
    public WeakList<T> bake() {
        List<WeakReference<T>> toRemove = new ArrayList<>();
        for (WeakReference<T> ref : refs) {
            if (ref.get() == null) toRemove.add(ref);
        }
        for (WeakReference<T> ref : toRemove) {
            refs.remove(ref);
        }
        return this;
    }

    @Override
    public void add(int index, @Nullable T element) {
        if (element == null) return; // do nothing
        refs.add(index, new WeakReference<>(element));
    }

    @Nullable
    @Override
    public T set(int index, @Nullable T element) {
        if (element == null) return null; // do nothing
        WeakReference<T> ref = refs.set(index, new WeakReference<>(element));
        return ref != null ? ref.get() : null;
    }

    @Contract(mutates = "this")
    @Override
    public T remove(int index) {
        WeakReference<T> ref = refs.remove(index);
        return ref != null ? ref.get() : null;
    }

    @Override
    public void clear() {
        refs.clear();
    }

    @Override
    public int hashCode() {
        return refs.hashCode();
    }

    @Contract(pure = true)
    @Override
    public T get(int index) {
        WeakReference<T> ref = refs.get(index);
        T value = ref.get();
        if (value == null) {
            refs.remove(ref);
        }
        return value;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean contains(@Nullable Object o) {
        if (o == null) return false;
        for (WeakReference<T> ref : refs) {
            T value = ref.get();
            if (o.equals(value)) return true;
        }
        return false;
    }

    @Override
    public int size() {
        return refs.size();
    }
}
