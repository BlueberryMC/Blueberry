package net.blueberrymc.common.bml.config;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class ShortVisualConfig extends NumberVisualConfig<Short> {
    private final short min;
    private final short max;

    public ShortVisualConfig(@Nullable Component component, short min, short max) {
        this(component, null, (short) 0, min, max);
    }

    public ShortVisualConfig(@Nullable Component component, @Nullable Short initialValue, @Nullable Short defaultValue, short min, short max) {
        super(component, initialValue, defaultValue);
        this.min = min;
        this.max = max;
    }

    public short getMin() {
        return min;
    }

    public short getMax() {
        return max;
    }

    @Override
    public void setPercentage(@Range(from = 0, to = 1) double percentage) {
        set((short) (min + ((double) max - min) * percentage));
    }

    @Override
    protected @Range(from = 0, to = 1) double getPercentage0() {
        return ((double) Objects.requireNonNull(get()) - min) / ((double) max - min);
    }

    @NotNull
    @Override
    public Short getMinAsNumber() {
        return min;
    }

    @NotNull
    @Override
    public Short getMaxAsNumber() {
        return max;
    }
}
