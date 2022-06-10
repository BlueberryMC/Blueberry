package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class FloatVisualConfig extends NumberVisualConfig<Float> {
    private final float min;
    private final float max;

    public FloatVisualConfig(@Nullable Component component, float min, float max) {
        this(component, null, 0F, min, max);
    }

    public FloatVisualConfig(@Nullable Component component, @Nullable Float initialValue, @Nullable Float defaultValue, float min, float max) {
        super(component, initialValue, defaultValue);
        this.min = min;
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    @Override
    public void setPercentage(@Range(from = 0, to = 1) double percentage) {
        set((float) (min + (max - min) * percentage));
    }

    @Override
    protected @Range(from = 0, to = 1) double getPercentage0() {
        return (double) (Objects.requireNonNull(get()) - min) / (max - min);
    }

    @NotNull
    @Override
    public Float getMinAsNumber() {
        return min;
    }

    @NotNull
    @Override
    public Float getMaxAsNumber() {
        return max;
    }
}
