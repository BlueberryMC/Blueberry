package net.blueberrymc.common.bml.config;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class DoubleVisualConfig extends NumberVisualConfig<Double> {
    private final double min;
    private final double max;

    public DoubleVisualConfig(@Nullable Component component, double min, double max) {
        this(component, null, min, min, max);
    }

    public DoubleVisualConfig(@Nullable Component component, @Nullable Double initialValue, @Nullable Double defaultValue, double min, double max) {
        super(component, initialValue, defaultValue);
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public void setPercentage(@Range(from = 0, to = 1) double percentage) {
        set(min + (max - min) * percentage);
    }

    @Override
    public @Range(from = 0, to = 1) double getPercentage0() {
        return (Objects.requireNonNull(get()) - min) / (max - min);
    }

    @NotNull
    @Override
    public Double getMinAsNumber() {
        return min;
    }

    @NotNull
    @Override
    public Double getMaxAsNumber() {
        return max;
    }
}
