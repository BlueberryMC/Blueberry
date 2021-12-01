package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegerVisualConfig extends NumberVisualConfig<Integer> {
    private final int min;
    private final int max;

    public IntegerVisualConfig(@Nullable Component component, int min, int max) {
        this(component, null, 0, min, max);
    }

    public IntegerVisualConfig(@Nullable Component component, @Nullable Integer initialValue, @Nullable Integer defaultValue, int min, int max) {
        super(component, initialValue, defaultValue);
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @NotNull
    @Override
    public Integer getMinAsNumber() {
        return min;
    }

    @NotNull
    @Override
    public Integer getMaxAsNumber() {
        return max;
    }
}
