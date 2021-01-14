package net.blueberrymc.common.util;

import java.lang.ref.WeakReference;

public class UnloadedObject<T> {
    private final WeakReference<T> object;

    public UnloadedObject(T object) {
        this.object = new WeakReference<>(object);
    }

    public T getObject() {
        T t = object.get();
        if (t == null) throw new IllegalArgumentException("This object have been garbage collected");
        return t;
    }
}
