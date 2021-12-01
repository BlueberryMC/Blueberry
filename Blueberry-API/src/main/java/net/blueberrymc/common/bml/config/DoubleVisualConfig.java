package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoubleVisualConfig extends NumberVisualConfig<Double> {
    private final double min;
    private final double max;

    public DoubleVisualConfig(@Nullable Component component, double min, double max) {
        this(component, null, 0.0, min, max);
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
