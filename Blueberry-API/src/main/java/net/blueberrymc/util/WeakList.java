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

    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation).  Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     *         is not supported by this list
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws NullPointerException if the specified element is null and
     *         this list does not permit null elements
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
    @Override
    public void add(int index, @Nullable T element) {
        if (element == null) return; // do nothing
        refs.add(index, new WeakReference<>(element));
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws UnsupportedOperationException if the <tt>set</tt> operation
     *         is not supported by this list
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws NullPointerException if the specified element is null and
     *         this list does not permit null elements
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
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
