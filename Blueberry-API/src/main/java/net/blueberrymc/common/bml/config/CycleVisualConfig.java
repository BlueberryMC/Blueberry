package net.blueberrymc.common.bml.config;

import net.blueberrymc.util.NameGetter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CycleVisualConfig<T> extends VisualConfig<T> {
    private final List<T> list;
    private int index;

    public CycleVisualConfig(@Nullable Component component, @NotNull List<T> values) {
        this(component, values, 0);
    }

    public CycleVisualConfig(@Nullable Component component, @NotNull List<T> values, int index) {
        this(component, values, index, values.get(index));
    }

    public CycleVisualConfig(@Nullable Component component, @NotNull List<T> values, int index, @Nullable T defaultValue) {
        super(component, null, defaultValue);
        this.list = values;
        this.index = index;
    }

    @Contract("_, _ -> new")
    @NotNull
    public static <E extends Enum<E>> CycleVisualConfig<E> fromEnum(@Nullable Component component, @NotNull Class<E> clazz) {
        return new CycleVisualConfig<>(component, Arrays.asList(clazz.getEnumConstants()));
    }

    @Contract("_, _, _ -> new")
    @NotNull
    public static <E extends Enum<E>> CycleVisualConfig<E> fromEnum(@Nullable Component component, @NotNull Class<E> clazz, @Nullable E defaultValue) {
        List<E> list = Arrays.asList(clazz.getEnumConstants());
        return new CycleVisualConfig<>(component, list, Math.max(0, list.indexOf(defaultValue)), defaultValue);
    }

    @NotNull
    @Override
    public T get() {
        return list.get(index);
    }

    @NotNull
    public String getCurrentName() {
        return getName(get());
    }

    @NotNull
    public String getNextName() {
        return getName(next());
    }

    @NotNull
    private String getName(T value) {
        if (value == null) return "null";
        if (value instanceof NameGetter) {
            return ((NameGetter) value).getName();
        } else if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        } else {
            return value.toString();
        }
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
