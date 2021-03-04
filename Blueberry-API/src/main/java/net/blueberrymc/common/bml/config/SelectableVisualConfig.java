package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: implement it in ModConfigScreen
public class SelectableVisualConfig<T> extends VisualConfig<T> {
    private final List<T> list;
    private int index;

    public SelectableVisualConfig(@Nullable Component component, @NotNull List<T> values) {
        this(component, values, 0);
    }

    public SelectableVisualConfig(@Nullable Component component, @NotNull List<T> values, int index) {
        super(component);
        this.list = values;
        this.index = index;
    }

    @NotNull
    @Override
    public T get() {
        return list.get(index);
    }

    @Override
    public void set(@Nullable T value) {
        AtomicInteger current = new AtomicInteger();
        for (T t : list) {
            if (t.equals(value)) {
                index = current.get();
                break;
            }
            current.getAndIncrement();
        }
    }

    @NotNull
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
