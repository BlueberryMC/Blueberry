package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectableVisualConfig<T> extends VisualConfig<T> {
    private final List<T> list;
    private int index;

    public SelectableVisualConfig(Component component, List<T> values) {
        this(component, values, 0);
    }

    public SelectableVisualConfig(Component component, List<T> values, int index) {
        super(component);
        this.list = values;
        this.index = index;
    }

    @Override
    public T get() {
        return list.get(index);
    }

    @Override
    public void set(T value) {
        AtomicInteger current = new AtomicInteger();
        for (T t : list) {
            if (t.equals(value)) {
                index = current.get();
                break;
            }
            current.getAndIncrement();
        }
    }

    public T next() {
        if (++index >= list.size()) {
            index = 0;
            return list.get(0);
        } else {
            return list.get(index);
        }
    }

    public void setIndex(int value) {
        index = value;
    }
}
