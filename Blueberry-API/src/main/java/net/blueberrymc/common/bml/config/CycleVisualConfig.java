package net.blueberrymc.common.bml.config;

import net.blueberrymc.util.ComponentGetter;
import net.blueberrymc.util.NameGetter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * VisualConfig that can be clicked to cycle through the options.
 */
public class CycleVisualConfig<T> extends VisualConfig<T> {
    private final List<T> list;
    private int index;
    private boolean reverse = false;

    public CycleVisualConfig(@Nullable Component component, @NotNull List<T> values) {
        this(component, values, 0);
    }

    public CycleVisualConfig(@Nullable Component component, @NotNull List<T> values, int index) {
        this(component, values, index, null, values.get(index));
    }

    private CycleVisualConfig(@Nullable Component component, @NotNull List<T> values, int index, @Nullable T initialValue, @Nullable T defaultValue) {
        super(component, initialValue, defaultValue);
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
        return new CycleVisualConfig<>(component, list, Math.max(0, list.indexOf(defaultValue)));
    }

    @Contract("_, _, _, _ -> new")
    @NotNull
    public static <E extends Enum<E>> CycleVisualConfig<E> fromEnum(@Nullable Component component, @NotNull Class<E> clazz, @Nullable E initialValue, @Nullable E defaultValue) {
        List<E> list = Arrays.asList(clazz.getEnumConstants());
        return new CycleVisualConfig<>(component, list, Math.max(0, list.indexOf(initialValue)), initialValue, defaultValue);
    }

    @SuppressWarnings({ "SuspiciousMethodCalls", "unchecked", "rawtypes" })
    @Contract("_, _, _, _ -> new")
    @NotNull
    public static <E extends Enum<E>> CycleVisualConfig<E> fromEnumUnchecked(@Nullable Component component, @NotNull Class<?> clazz, @Nullable Object initialValue, @Nullable Object defaultValue) {
        List<E> list = (List<E>) Arrays.asList(clazz.getEnumConstants());
        return new CycleVisualConfig(component, list, Math.max(0, list.indexOf(initialValue)), initialValue, defaultValue);
    }

    @NotNull
    @Override
    public T get() {
        return list.get(index % list.size());
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
    public String getPreviousName() {
        return getName(previous());
    }

    @NotNull
    private String getName(T value) {
        if (value == null) return "null";
        if (value instanceof ComponentGetter cg) {
            return cg.getComponent().getString();
        } else if (value instanceof NameGetter ng) {
            return ng.getName();
        } else if (value instanceof Enum<?> e) {
            return e.name();
        } else {
            return value.toString();
        }
    }

    @Override
    public void set(@Nullable T value) {
        index = list.indexOf(value);
    }

    @NotNull
    public T next() {
        if (++index >= list.size()) {
            index = 0;
        }
        return list.get(index);
    }

    @NotNull
    public T peekNext() {
        int nextIndex = index + 1;
        if (nextIndex >= list.size()) {
            nextIndex = 0;
        }
        return list.get(nextIndex);
    }

    @NotNull
    public T previous() {
        if (--index < 0) {
            index = list.size() - 1;
        }
        return list.get(index);
    }

    @NotNull
    public T peekPrevious() {
        int prevIndex = index - 1;
        if (prevIndex < 0) {
            prevIndex = list.size() - 1;
        }
        return list.get(prevIndex);
    }

    public void setIndex(int value) {
        index = value;
    }

    public int size() {
        return list.size();
    }

    public boolean isReverse() {
        return reverse;
    }

    @NotNull
    public CycleVisualConfig<T> reverse(boolean flag) {
        this.reverse = flag;
        return this;
    }

    @NotNull
    public CycleVisualConfig<T> reverse() {
        return reverse(!reverse);
    }
}
